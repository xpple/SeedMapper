package dev.xpple.seedmapper.command.commands;

import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.LootTableContext;
import com.github.cubiomes.RandomSource;
import com.github.cubiomes.RandomState;
import com.github.cubiomes.Xoroshiro;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import dev.xpple.seedmapper.command.CommandExceptions;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.config.Configs;
import dev.xpple.seedmapper.util.ComponentUtils;
import dev.xpple.seedmapper.util.CubiomesHelper;
import dev.xpple.seedmapper.util.SeedIdentifier;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.levelgen.RandomSupport;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.mojang.brigadier.arguments.BoolArgumentType.*;
import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static dev.xpple.seedmapper.util.ChatBuilder.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.*;

public class VaultCommand {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final SimpleCommandExceptionType ALREADY_DETECTING_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("command.vault.alreadyDetecting"));
    private static final SimpleCommandExceptionType MALFORMED_STATE_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("command.vault.malformedState"));

    private static final long REWARD_MD5_LO = 0x102ea793e31f23ffL; // md5("minecraft:chests/trial_chambers/reward")
    private static final long REWARD_MD5_HI = 0xd7c231952cddadf1L;

    private static final long REWARD_OMINOUS_MD5_LO = 0x05a13d5ce5edaab3L; // md5("minecraft:chests/trial_chambers/reward_ominous")
    private static final long REWARD_OMINOUS_MD5_HI = 0x1a3950a30a86bc23L;

    public static @Nullable VaultPredictor predictor = null;

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("sm:vault")
            .then(literal("detect")
                .executes(ctx -> detectDrops(CustomClientCommandSource.of(ctx.getSource())))
                .then(argument("start", integer(0))
                    .executes(ctx -> detectDrops(CustomClientCommandSource.of(ctx.getSource()), getInteger(ctx, "start")))))
            .then(literal("predict")
                .then(literal("offset")
                    .then(argument("offset", integer(0))
                        .then(argument("ominous", bool())
                            .executes(ctx -> predictLoot(CustomClientCommandSource.of(ctx.getSource()), getInteger(ctx, "offset"), getBool(ctx, "ominous")))
                            .then(argument("amount", integer(1))
                                .executes(ctx -> predictLoot(CustomClientCommandSource.of(ctx.getSource()), getInteger(ctx, "offset"), getBool(ctx, "ominous"), getInteger(ctx, "amount")))))))
                .then(literal("state")
                    .then(argument("state", word())
                        .then(argument("ominous", bool())
                            .executes(ctx -> predictLoot(CustomClientCommandSource.of(ctx.getSource()), getString(ctx, "state"), getBool(ctx, "ominous")))
                            .then(argument("amount", integer(1))
                                .executes(ctx -> predictLoot(CustomClientCommandSource.of(ctx.getSource()), getString(ctx, "state"), getBool(ctx, "ominous"), getInteger(ctx, "amount")))))))));
    }

    private static int detectDrops(FabricClientCommandSource source) throws CommandSyntaxException {
        return detectDrops(source, 0);
    }

    private static int detectDrops(FabricClientCommandSource source, int start) throws CommandSyntaxException {
        if (predictor != null) {
            throw ALREADY_DETECTING_EXCEPTION.create();
        }
        predictor = new VaultPredictor(start);
        source.sendFeedback(Component.translatable("command.vault.startedDetecting"));
        return Command.SINGLE_SUCCESS;
    }

    private static int predictLoot(CustomClientCommandSource source, int offset, boolean isOminous) throws CommandSyntaxException {
        return predictLoot(source, offset, isOminous, 1);
    }

    private static int predictLoot(CustomClientCommandSource source, int offset, boolean isOminous, int amount) throws CommandSyntaxException {
        SeedIdentifier seedIdentifier = source.getSeed().getSecond();
        long seed = seedIdentifier.seed();
        int version = source.getVersion();
        RandomSupport.Seed128bit vaultSeed = getVaultSeed(isOminous, seed);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment state = allocRandomState(arena, vaultSeed);
            MemorySegment lootTableContext = getLootTableContext(arena, isOminous, version);
            if (lootTableContext == null) {
                throw CommandExceptions.INCOMPATIBLE_PARAMETERS_EXCEPTION.create();
            }
            Cubiomes.set_loot_prng_type(lootTableContext, Cubiomes.XOROSHIRO());
            Cubiomes.set_internal_loot_seed(lootTableContext, state);

            for (int i = 0; i < offset; i++) {
                Cubiomes.generate_loot(lootTableContext);
            }

            showLoot(source, lootTableContext, amount);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int predictLoot(CustomClientCommandSource source, String stateString, boolean isOminous) throws CommandSyntaxException {
        return predictLoot(source, stateString, isOminous, 1);
    }

    private static int predictLoot(CustomClientCommandSource source, String stateString, boolean isOminous, int amount) throws CommandSyntaxException {
        int version = source.getVersion();

        if (!stateString.matches("[0-9a-fA-F]{32}")) {
            throw MALFORMED_STATE_EXCEPTION.create();
        }

        long hi = Long.parseUnsignedLong(stateString.substring(0, 16), 16);
        long lo = Long.parseUnsignedLong(stateString.substring(16, 32), 16);

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment state = RandomState.allocate(arena);
            MemorySegment xr = Xoroshiro.allocate(arena);
            Xoroshiro.lo(xr, lo);
            Xoroshiro.hi(xr, hi);
            RandomState.xr(state, xr);

            MemorySegment lootTableContext = getLootTableContext(arena, isOminous, version);
            if (lootTableContext == null) {
                throw CommandExceptions.INCOMPATIBLE_PARAMETERS_EXCEPTION.create();
            }
            Cubiomes.set_loot_prng_type(lootTableContext, Cubiomes.XOROSHIRO());
            Cubiomes.set_internal_loot_seed(lootTableContext, state);

            showLoot(source, lootTableContext, amount);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static void showLoot(CustomClientCommandSource source, MemorySegment lootTableContext, int amount) {
        Optional<Registry<Enchantment>> optionalEnchantmentRegistry = source.getPlayer().registryAccess().lookup(Registries.ENCHANTMENT);
        if (optionalEnchantmentRegistry.isEmpty()) {
            return;
        }
        Registry<Enchantment> enchantmentRegistry = optionalEnchantmentRegistry.get();
        Optional<Registry<MobEffect>> optionalMobEffectRegistry = source.getPlayer().registryAccess().lookup(Registries.MOB_EFFECT);
        if (optionalMobEffectRegistry.isEmpty()) {
            return;
        }
        Registry<MobEffect> mobEffectRegistry = optionalMobEffectRegistry.get();
        for (int i = 0; i < amount; i++) {
            List<ItemStack> itemStacks = generateLoot(lootTableContext, enchantmentRegistry, mobEffectRegistry);
            MutableComponent component = net.minecraft.network.chat.ComponentUtils.formatList(itemStacks, Component.literal(", "), ItemStack::getDisplayName);
            source.sendFeedback(Component.literal("%d. ".formatted(i + 1)).append(component));
        }
    }

    private static RandomSupport.Seed128bit getVaultSeed(boolean isOminous, long seed) {
        RandomSupport.Seed128bit seed128bit = RandomSupport.upgradeSeedTo128bitUnmixed(seed);
        if (isOminous) {
            seed128bit = seed128bit.xor(REWARD_OMINOUS_MD5_LO, REWARD_OMINOUS_MD5_HI);
        } else {
            seed128bit = seed128bit.xor(REWARD_MD5_LO, REWARD_MD5_HI);
        }
        seed128bit = seed128bit.mixed();
        return seed128bit;
    }

    private static MemorySegment allocRandomState(Arena arena, RandomSupport.Seed128bit seed128bit) {
        MemorySegment state = RandomState.allocate(arena);
        MemorySegment xr = Xoroshiro.allocate(arena);
        Xoroshiro.lo(xr, seed128bit.seedLo());
        Xoroshiro.hi(xr, seed128bit.seedHi());
        RandomState.xr(state, xr);
        return state;
    }

    private static @Nullable MemorySegment getLootTableContext(Arena arena, boolean isOminous, int version) {
        MemorySegment ltcPtr = arena.allocate(Cubiomes.C_POINTER);
        if (isOminous) {
            if (Cubiomes.init_reward_ominous(ltcPtr, version) == 0) {
                LOGGER.warn("Could not initialise reward ominous loot table!");
                return null;
            }
        } else {
            if (Cubiomes.init_reward(ltcPtr, version) == 0) {
                LOGGER.warn("Could not initialise reward loot table!");
                return null;
            }
        }
        return ltcPtr.get(ValueLayout.ADDRESS, 0).reinterpret(LootTableContext.sizeof());
    }

    private static List<ItemStack> generateLoot(MemorySegment lootTableContext, Registry<Enchantment> enchantmentRegistry, @Nullable Registry<MobEffect> mobEffectRegistry) {
        Cubiomes.generate_loot(lootTableContext);
        int lootCount = LootTableContext.generated_item_count(lootTableContext);
        List<ItemStack> generatedLoot = new ArrayList<>(lootCount);
        for (int lootIdx = 0; lootIdx < lootCount; lootIdx++) {
            MemorySegment itemStackInternal = com.github.cubiomes.ItemStack.asSlice(LootTableContext.generated_items(lootTableContext), lootIdx);
            ItemStack itemStack = CubiomesHelper.convertItemStack(lootTableContext, itemStackInternal, enchantmentRegistry);
            if (mobEffectRegistry != null) {
                CubiomesHelper.setMobEffectAsLore(itemStack, itemStackInternal, mobEffectRegistry);
            }
            generatedLoot.add(itemStack);
        }
        return generatedLoot;
    }

    public static class VaultPredictor {
        private final int startingOffset;

        private final Set<Integer> ejectedItemIds = new HashSet<>();
        private final List<ItemStack> ejectedItems = new ArrayList<>();

        public VaultPredictor(int startingOffset) {
            this.startingOffset = startingOffset;
        }

        public void predictLoot(boolean isOminous) {
            CustomClientCommandSource source = CustomClientCommandSource.makeFakeCommandSource();
            if (source == null) {
                LOGGER.warn("Loot prediction could not succeed because source was null!");
                return;
            }
            Optional<Registry<Enchantment>> optionalEnchantmentRegistry = source.getPlayer().registryAccess().lookup(Registries.ENCHANTMENT);
            if (optionalEnchantmentRegistry.isEmpty()) {
                return;
            }
            Registry<Enchantment> enchantmentRegistry = optionalEnchantmentRegistry.get();
            try (Arena arena = Arena.ofConfined()) {
                SeedIdentifier seedIdentifier = source.getSeed().getSecond();
                long seed = seedIdentifier.seed();
                int version = source.getVersion();
                RandomSupport.Seed128bit seed128bit = getVaultSeed(isOminous, seed);
                MemorySegment state = allocRandomState(arena, seed128bit);

                MemorySegment lootTableContext = getLootTableContext(arena, isOminous, version);
                if (lootTableContext == null) {
                    return;
                }
                Cubiomes.set_loot_prng_type(lootTableContext, Cubiomes.XOROSHIRO());
                Cubiomes.set_internal_loot_seed(lootTableContext, state);

                offsetLoop:
                for (int offset = this.startingOffset; offset < Configs.MaxVaultAttempts; offset++) {
                    List<ItemStack> generatedLoot = generateLoot(lootTableContext, enchantmentRegistry, null);
                    List<ItemStack> reversed = ejectedItems.reversed();
                    for (int i = 0; i < reversed.size(); i++) {
                        ItemStack ejectedItem = reversed.get(i);
                        ItemStack generatedItem = generatedLoot.get(i);
                        if (!ItemStack.isSameItem(ejectedItem, generatedItem)) {
                            continue offsetLoop;
                        }
                        if (ejectedItem.count() != generatedItem.count()) {
                            continue offsetLoop;
                        }
                        // only compare enchantments for now
                        ItemEnchantments ejectedItemEnchantments = ejectedItem.get(DataComponents.ENCHANTMENTS);
                        ItemEnchantments generatedItemEnchantments = generatedItem.get(DataComponents.ENCHANTMENTS);
                        if (!Objects.equals(ejectedItemEnchantments, generatedItemEnchantments)) {
                            continue offsetLoop;
                        }
                        ejectedItemEnchantments = ejectedItem.get(DataComponents.STORED_ENCHANTMENTS);
                        generatedItemEnchantments = generatedItem.get(DataComponents.STORED_ENCHANTMENTS);
                        if (!Objects.equals(ejectedItemEnchantments, generatedItemEnchantments)) {
                            continue offsetLoop;
                        }
                    }

                    source.sendFeedback(Component.translatable("command.vault.computedOffset", ComponentUtils.formatNumber(offset + 1)));
                    source.sendFeedback(Component.translatable("command.vault.sequenceState", formatState(RandomSource.xr(LootTableContext.prng_state(lootTableContext)))));
                    return;
                }

                source.sendError(Component.translatable("command.vault.failed", Configs.MaxVaultAttempts));
            } catch (CommandSyntaxException e) {
                source.sendError(error((MutableComponent) e.getRawMessage()));
            }
        }

        private static MutableComponent formatState(MemorySegment xr) {
            String formatted = "%016x%016x".formatted(Xoroshiro.hi(xr), Xoroshiro.lo(xr));
            return copy(
                hover(
                    accent(formatted),
                    base(Component.translatable("chat.copy.click"))
                ),
                formatted
            );
        }

        public Set<Integer> getEjectedItemIds() {
            return this.ejectedItemIds;
        }

        public List<ItemStack> getEjectedItems() {
            return this.ejectedItems;
        }
    }
}
