package dev.xpple.seedmapper.command.arguments;

import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.EnchantInstance;
import com.github.cubiomes.ItemStack;
import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import dev.xpple.seedmapper.command.CommandExceptions;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ItemAndEnchantmentsPredicateArgument implements ArgumentType<ItemAndEnchantmentsPredicateArgument.EnchantedItem> {

    private static final Collection<String> EXAMPLES = Arrays.asList("apple", "diamond_pickaxe", "tnt");

    private static final SimpleCommandExceptionType EXPECTED_WITH_WITHOUT_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.exceptions.expectedWithWithout"));

    private static final Map<String, Integer> ITEMS = IntStream.range(0, Cubiomes.NUM_ITEMS()).boxed()
        .collect(Collectors.toUnmodifiableMap(item -> {
            String name = Cubiomes.global_id2item_name(item, Cubiomes.MC_NEWEST()).getString(0);
            String prefix = Identifier.DEFAULT_NAMESPACE + ':';
            return name.startsWith(prefix) ? name.substring(prefix.length()) : name;
        }, item -> item));

    public static final Map<Integer, Item> ITEM_ID_TO_MC = IntStream.range(0, Cubiomes.NUM_ITEMS()).boxed()
        .collect(Collectors.toUnmodifiableMap(item -> item, item -> {
            String name = Cubiomes.global_id2item_name(item, Cubiomes.MC_NEWEST()).getString(0);
            Identifier identifier = Identifier.parse(name);
            Optional<Item> optionalItem = BuiltInRegistries.ITEM.getOptional(identifier);
            return optionalItem.orElseThrow();
        }));

    //<editor-fold defaultstate="collapsed" desc="private static final Map<String, Integer> ENCHANTMENTS;">
    private static final Map<String, Integer> ENCHANTMENTS = ImmutableMap.<String, Integer>builder()
        .put("protection", Cubiomes.PROTECTION())
        .put("fire_protection", Cubiomes.FIRE_PROTECTION())
        .put("blast_protection", Cubiomes.BLAST_PROTECTION())
        .put("projectile_protection", Cubiomes.PROJECTILE_PROTECTION())
        .put("respiration", Cubiomes.RESPIRATION())
        .put("aqua_affinity", Cubiomes.AQUA_AFFINITY())
        .put("thorns", Cubiomes.THORNS())
        .put("swift_sneak", Cubiomes.SWIFT_SNEAK())
        .put("feather_falling", Cubiomes.FEATHER_FALLING())
        .put("depth_strider", Cubiomes.DEPTH_STRIDER())
        .put("frost_walker", Cubiomes.FROST_WALKER())
        .put("soul_speed", Cubiomes.SOUL_SPEED())
        .put("sharpness", Cubiomes.SHARPNESS())
        .put("smite", Cubiomes.SMITE())
        .put("bane_of_arthropods", Cubiomes.BANE_OF_ARTHROPODS())
        .put("knockback", Cubiomes.KNOCKBACK())
        .put("fire_aspect", Cubiomes.FIRE_ASPECT())
        .put("looting", Cubiomes.LOOTING())
        .put("sweeping_edge", Cubiomes.SWEEPING_EDGE())
        .put("efficiency", Cubiomes.EFFICIENCY())
        .put("silk_touch", Cubiomes.SILK_TOUCH())
        .put("fortune", Cubiomes.FORTUNE())
        .put("luck_of_the_sea", Cubiomes.LUCK_OF_THE_SEA())
        .put("lunge", Cubiomes.LUNGE())
        .put("lure", Cubiomes.LURE())
        .put("power", Cubiomes.POWER())
        .put("punch", Cubiomes.PUNCH())
        .put("flame", Cubiomes.FLAME())
        .put("infinity", Cubiomes.INFINITY_ENCHANTMENT())
        .put("quick_charge", Cubiomes.QUICK_CHARGE())
        .put("multishot", Cubiomes.MULTISHOT())
        .put("piercing", Cubiomes.PIERCING())
        .put("impaling", Cubiomes.IMPALING())
        .put("riptide", Cubiomes.RIPTIDE())
        .put("loyalty", Cubiomes.LOYALTY())
        .put("channeling", Cubiomes.CHANNELING())
        .put("density", Cubiomes.DENSITY())
        .put("breach", Cubiomes.BREACH())
        .put("wind_burst", Cubiomes.WIND_BURST())
        .put("mending", Cubiomes.MENDING())
        .put("unbreaking", Cubiomes.UNBREAKING())
        .put("curse_of_vanishing", Cubiomes.CURSE_OF_VANISHING())
        .put("curse_of_binding", Cubiomes.CURSE_OF_BINDING())
        .build();
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="public static final Map<Integer, ResourceKey<Enchantment>> ENCHANTMENT_ID_TO_MC;">
    public static final Map<Integer, ResourceKey<Enchantment>> ENCHANTMENT_ID_TO_MC = ImmutableMap.<Integer, ResourceKey<Enchantment>>builder()
        .put(Cubiomes.PROTECTION(), Enchantments.PROTECTION)
        .put(Cubiomes.FIRE_PROTECTION(), Enchantments.FIRE_PROTECTION)
        .put(Cubiomes.BLAST_PROTECTION(), Enchantments.BLAST_PROTECTION)
        .put(Cubiomes.PROJECTILE_PROTECTION(), Enchantments.PROJECTILE_PROTECTION)
        .put(Cubiomes.RESPIRATION(), Enchantments.RESPIRATION)
        .put(Cubiomes.AQUA_AFFINITY(), Enchantments.AQUA_AFFINITY)
        .put(Cubiomes.THORNS(), Enchantments.THORNS)
        .put(Cubiomes.SWIFT_SNEAK(), Enchantments.SWIFT_SNEAK)
        .put(Cubiomes.FEATHER_FALLING(), Enchantments.FEATHER_FALLING)
        .put(Cubiomes.DEPTH_STRIDER(), Enchantments.DEPTH_STRIDER)
        .put(Cubiomes.FROST_WALKER(), Enchantments.FROST_WALKER)
        .put(Cubiomes.SOUL_SPEED(), Enchantments.SOUL_SPEED)
        .put(Cubiomes.SHARPNESS(), Enchantments.SHARPNESS)
        .put(Cubiomes.SMITE(), Enchantments.SMITE)
        .put(Cubiomes.BANE_OF_ARTHROPODS(), Enchantments.BANE_OF_ARTHROPODS)
        .put(Cubiomes.KNOCKBACK(), Enchantments.KNOCKBACK)
        .put(Cubiomes.FIRE_ASPECT(), Enchantments.FIRE_ASPECT)
        .put(Cubiomes.LOOTING(), Enchantments.LOOTING)
        .put(Cubiomes.SWEEPING_EDGE(), Enchantments.SWEEPING_EDGE)
        .put(Cubiomes.EFFICIENCY(), Enchantments.EFFICIENCY)
        .put(Cubiomes.SILK_TOUCH(), Enchantments.SILK_TOUCH)
        .put(Cubiomes.FORTUNE(), Enchantments.FORTUNE)
        .put(Cubiomes.LUCK_OF_THE_SEA(), Enchantments.LUCK_OF_THE_SEA)
        .put(Cubiomes.LUNGE(), Enchantments.LUNGE)
        .put(Cubiomes.LURE(), Enchantments.LURE)
        .put(Cubiomes.POWER(), Enchantments.POWER)
        .put(Cubiomes.PUNCH(), Enchantments.PUNCH)
        .put(Cubiomes.FLAME(), Enchantments.FLAME)
        .put(Cubiomes.INFINITY_ENCHANTMENT(), Enchantments.INFINITY)
        .put(Cubiomes.QUICK_CHARGE(), Enchantments.QUICK_CHARGE)
        .put(Cubiomes.MULTISHOT(), Enchantments.MULTISHOT)
        .put(Cubiomes.PIERCING(), Enchantments.PIERCING)
        .put(Cubiomes.IMPALING(), Enchantments.IMPALING)
        .put(Cubiomes.RIPTIDE(), Enchantments.RIPTIDE)
        .put(Cubiomes.LOYALTY(), Enchantments.LOYALTY)
        .put(Cubiomes.CHANNELING(), Enchantments.CHANNELING)
        .put(Cubiomes.DENSITY(), Enchantments.DENSITY)
        .put(Cubiomes.BREACH(), Enchantments.BREACH)
        .put(Cubiomes.WIND_BURST(), Enchantments.WIND_BURST)
        .put(Cubiomes.MENDING(), Enchantments.MENDING)
        .put(Cubiomes.UNBREAKING(), Enchantments.UNBREAKING)
        .put(Cubiomes.CURSE_OF_VANISHING(), Enchantments.VANISHING_CURSE)
        .put(Cubiomes.CURSE_OF_BINDING(), Enchantments.BINDING_CURSE)
        .build();
    //</editor-fold>

    public static ItemAndEnchantmentsPredicateArgument itemAndEnchantments() {
        return new ItemAndEnchantmentsPredicateArgument();
    }

    public static EnchantedItem getItemAndEnchantments(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, EnchantedItem.class);
    }

    @Override
    public EnchantedItem parse(StringReader reader) throws CommandSyntaxException {
        return new Parser(reader).parse();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        StringReader reader = new StringReader(builder.getInput());
        reader.setCursor(builder.getStart());

        Parser parser = new Parser(reader);

        try {
            parser.parse();
        } catch (CommandSyntaxException ignored) {
        }

        if (parser.suggestor != null) {
            parser.suggestor.accept(builder);
        }

        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    private static class Parser {
        private final StringReader reader;
        private Consumer<SuggestionsBuilder> suggestor;

        private Parser(StringReader reader) {
            this.reader = reader;
        }

        private EnchantedItem parse() throws CommandSyntaxException {
            int item = parseItem();
            // Predicate<ItemStack>
            Predicate<MemorySegment> predicate = _ -> true;
            if (!reader.canRead()) {
                return new EnchantedItem(item, predicate);
            }
            while (true) {
                readWhitespace();
                int cursor = reader.getCursor();
                suggestor = suggestions -> {
                    SuggestionsBuilder builder = suggestions.createOffset(cursor);
                    SharedSuggestionProvider.suggest(new String[]{"with", "without"}, builder);
                    suggestions.add(builder);
                };
                if (!reader.canRead()) {
                    break;
                }
                boolean with = parseWithWithout();
                readWhitespace();
                int enchantment = parseEnchantment();
                readWhitespace();
                int enchantmentLevel = parseLevel();
                predicate = predicate.and(itemStack -> {
                    MemorySegment enchantments = ItemStack.enchantments(itemStack);
                    int enchantmentCount = ItemStack.enchantment_count(itemStack);
                    for (int i = 0; i < enchantmentCount; i++) {
                        MemorySegment enchantInstance = EnchantInstance.asSlice(enchantments, i);
                        int itemEnchantment = EnchantInstance.enchantment(enchantInstance);
                        if (itemEnchantment != enchantment) {
                            continue;
                        }
                        if (enchantmentLevel == -1) {
                            return with;
                        }
                        int itemEnchantmentLevel = EnchantInstance.level(enchantInstance);
                        return with == itemEnchantmentLevel >= enchantmentLevel;
                    }
                    return !with;
                });
                if (!reader.canRead()) {
                    break;
                }
            }

            return new EnchantedItem(item, predicate);
        }

        private int parseItem() throws CommandSyntaxException {
            int cursor = reader.getCursor();
            suggestor = suggestions -> {
                SuggestionsBuilder builder = suggestions.createOffset(cursor);
                SharedSuggestionProvider.suggest(ITEMS.keySet(), builder);
                suggestions.add(builder);
            };
            String itemString = reader.readUnquotedString();
            Integer item = ITEMS.get(itemString);
            if (item == null) {
                reader.setCursor(cursor);
                throw CommandExceptions.UNKNOWN_ITEM_EXCEPTION.create(itemString);
            }
            return item;
        }

        private boolean parseWithWithout() throws CommandSyntaxException {
            String string = reader.readUnquotedString();
            return switch (string) {
                case "with" -> true;
                case "without" -> false;
                default -> throw EXPECTED_WITH_WITHOUT_EXCEPTION.create();
            };
        }

        private int parseEnchantment() throws CommandSyntaxException {
            int cursor = reader.getCursor();
            suggestor = suggestions -> {
                SuggestionsBuilder builder = suggestions.createOffset(cursor);
                SharedSuggestionProvider.suggest(ENCHANTMENTS.keySet(), builder);
                suggestions.add(builder);
            };
            String enchantmentString = reader.readUnquotedString();
            Integer enchantment = ENCHANTMENTS.get(enchantmentString);
            if (enchantment == null) {
                reader.setCursor(cursor);
                throw CommandExceptions.UNKNOWN_ENCHANTMENT_EXCEPTION.create(enchantmentString);
            }
            return enchantment;
        }

        private int parseLevel() throws CommandSyntaxException {
            int cursor = reader.getCursor();
            suggestor = suggestions -> {
                SuggestionsBuilder builder = suggestions.createOffset(cursor);
                builder.suggest("*");
                suggestions.add(builder);
            };
            if (reader.canRead() && reader.peek() == '*') {
                reader.skip();
                return -1;
            }
            return reader.readInt();
        }

        private void readWhitespace() throws CommandSyntaxException {
            if (!reader.canRead() || reader.peek() != CommandDispatcher.ARGUMENT_SEPARATOR_CHAR) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherExpectedArgumentSeparator().createWithContext(reader);
            }
            reader.skipWhitespace();
        }
    }

    public record EnchantedItem(int item, Predicate<MemorySegment> enchantmensPredicate) {
    }
}
