package dev.xpple.seedmapper.util;

import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.EnchantInstance;
import com.github.cubiomes.ItemStack;
import com.github.cubiomes.MobEffect;
import com.github.cubiomes.MobEffectInstance;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import dev.xpple.seedmapper.command.arguments.ItemAndEnchantmentsPredicateArgument;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

import java.lang.foreign.MemorySegment;
import java.util.List;

public final class CubiomesHelper {
    private CubiomesHelper() {
    }

    private static final BiMap<Integer, ResourceKey<Level>> DIM_ID_TO_MC = ImmutableBiMap.of(
        Cubiomes.DIM_OVERWORLD(), Level.OVERWORLD,
        Cubiomes.DIM_NETHER(), Level.NETHER,
        Cubiomes.DIM_END(), Level.END
    );

    public static net.minecraft.world.item.ItemStack convertItemStack(MemorySegment lootTableContext, MemorySegment itemStackInternal, Registry<Enchantment> enchantmentsRegistry) {
        int itemId = Cubiomes.get_global_item_id(lootTableContext, ItemStack.item(itemStackInternal));
        Item item = ItemAndEnchantmentsPredicateArgument.ITEM_ID_TO_MC.get(itemId);
        var itemStack = new net.minecraft.world.item.ItemStack(item, ItemStack.count(itemStackInternal));
        MemorySegment enchantments = ItemStack.enchantments(itemStackInternal);
        int enchantmentCount = ItemStack.enchantment_count(itemStackInternal);
        if (itemStack.is(Items.BOOK) && enchantmentCount > 0) {
            itemStack = itemStack.transmuteCopy(Items.ENCHANTED_BOOK);
        }
        for (int enchantmentIdx = 0; enchantmentIdx < enchantmentCount; enchantmentIdx++) {
            MemorySegment enchantInstance = EnchantInstance.asSlice(enchantments, enchantmentIdx);
            int itemEnchantment = EnchantInstance.enchantment(enchantInstance);
            ResourceKey<Enchantment> enchantmentResourceKey = ItemAndEnchantmentsPredicateArgument.ENCHANTMENT_ID_TO_MC.get(itemEnchantment);
            Holder.Reference<Enchantment> enchantmentReference = enchantmentsRegistry.getOrThrow(enchantmentResourceKey);
            itemStack.enchant(enchantmentReference, EnchantInstance.level(enchantInstance));
        }
        return itemStack;
    }

    public static void setMobEffectAsLore(net.minecraft.world.item.ItemStack itemStack, MemorySegment itemStackInternal, Registry<net.minecraft.world.effect.MobEffect> mobEffectRegistry) {
        MemorySegment mobEffectInstance = ItemStack.mob_effect(itemStackInternal);
        if (MobEffectInstance.effect(mobEffectInstance) != -1) {
            MemorySegment mobEffectInternal = MobEffect.asSlice(Cubiomes.MOB_EFFECTS(), MobEffectInstance.effect(mobEffectInstance));
            var mobEffect = mobEffectRegistry.getOptional(Identifier.parse(MobEffect.effect_name(mobEffectInternal).getString(0))).orElse(null);
            if (mobEffect != null) {
                SuspiciousStewEffects.Entry entry = new SuspiciousStewEffects.Entry(Holder.direct(mobEffect), MobEffectInstance.duration(mobEffectInstance));
                var effectInstance = entry.createEffectInstance();
                MutableComponent description = PotionContents.getPotionDescription(effectInstance.getEffect(), effectInstance.getAmplifier());
                MutableComponent lore = Component.translatable("seedMap.chestLoot.stewEffect", description, (float) entry.duration() / SharedConstants.TICKS_PER_SECOND);
                itemStack.set(DataComponents.LORE, new ItemLore(List.of(lore)));
            }
        }
    }

    public static ResourceKey<Level> getMinecraftDimension(int dimension) {
        return DIM_ID_TO_MC.get(dimension);
    }

    public static int getCubiomesDimension(ResourceKey<Level> resourceKey) {
        return DIM_ID_TO_MC.inverse().get(resourceKey);
    }
}
