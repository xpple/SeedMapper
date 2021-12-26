package dev.xpple.seedmapper.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.loot.enchantment.Enchantment;
import com.seedfinding.mcfeature.loot.enchantment.EnchantmentInstance;
import com.seedfinding.mcfeature.loot.enchantment.Enchantments;
import com.seedfinding.mcfeature.loot.item.Item;
import com.seedfinding.mcfeature.loot.item.Items;
import dev.xpple.seedmapper.command.SharedHelpers;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.text.TranslatableText;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class EnchantedItemPredicateArgumentType implements ArgumentType<Pair<String, Predicate<Item>>>, SharedHelpers.Exceptions {

    private static final Collection<String> EXAMPLES = Arrays.asList("iron_axe", "diamond_sword with sharpness 3", "diamond_chestplate with protection * without thorns *");
    private static final Stream<String> items = Items.getItems().values().stream().map(Item::getName);
    private static final String[] lootableItems = new String[]{"diamond_pickaxe", "diamond_sword", "golden_horse_armor", "string", "bell", "poisonous_potato", "gold_ingot", "iron_boots", "iron_leggings", "flint_and_steel", "beetroot_seeds", "carrot", "gold_block", "gold_nugget", "bamboo", "diamond_horse_armor", "paper", "golden_hoe", "gunpowder", "lapis_lazuli", "iron_horse_armor", "diamond_chestplate", "diamond_shovel", "golden_chestplate", "golden_leggings", "golden_carrot", "filled_map", "coal", "diamond_boots", "compass", "golden_boots", "cooked_salmon", "iron_shovel", "suspicious_stew", "golden_pickaxe", "emerald", "golden_shovel", "sand", "leather_boots", "heart_of_the_sea", "iron_nugget", "wheat", "golden_sword", "light_weighted_pressure_plate", "pumpkin", "iron_pickaxe", "flint", "golden_axe", "potato", "cooked_cod", "map", "feather", "leather_chestplate", "leather_leggings", "enchanted_book", "iron_chestplate", "moss_block", "book", "clock", "iron_sword", "golden_apple", "enchanted_golden_apple", "fire_charge", "spider_eye", "bone", "prismarine_crystals", "obsidian", "glistering_melon_slice", "rotten_flesh", "experience_bottle", "diamond", "golden_helmet", "iron_helmet", "diamond_helmet", "leather_helmet", "iron_ingot", "saddle", "tnt", "diamond_leggings", "diamond_pickaxe", "diamond_sword", "golden_horse_armor", "string", "bell", "poisonous_potato", "gold_ingot", "iron_boots", "iron_leggings", "flint_and_steel", "beetroot_seeds", "carrot", "gold_block", "gold_nugget", "bamboo", "diamond_horse_armor", "paper", "golden_hoe", "gunpowder", "lapis_lazuli", "iron_horse_armor", "diamond_chestplate", "diamond_shovel", "golden_chestplate", "golden_leggings", "golden_carrot", "filled_map", "coal", "diamond_boots", "compass", "golden_boots", "cooked_salmon", "iron_shovel", "suspicious_stew", "golden_pickaxe", "emerald", "golden_shovel", "sand", "leather_boots", "heart_of_the_sea", "iron_nugget", "wheat", "golden_sword", "light_weighted_pressure_plate", "pumpkin", "iron_pickaxe", "flint", "golden_axe", "potato", "cooked_cod", "map", "feather", "leather_chestplate", "leather_leggings", "enchanted_book", "iron_chestplate", "moss_block", "book", "clock", "iron_sword", "golden_apple", "enchanted_golden_apple", "fire_charge", "spider_eye", "bone", "prismarine_crystals", "obsidian", "glistering_melon_slice", "rotten_flesh", "experience_bottle", "diamond", "golden_helmet", "iron_helmet", "diamond_helmet", "leather_helmet", "iron_ingot", "saddle", "tnt", "diamond_leggings"};

    private static final SimpleCommandExceptionType EXPECTED_WITH_OR_WITHOUT_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.exceptions.expectedWithOrWithout"));
    private static final DynamicCommandExceptionType UNKNOWN_ENCHANTMENT_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.unknownEnchantment", arg));
    private static final SimpleCommandExceptionType LEVEL_OUT_OF_BOUNDS_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.exceptions.levelOutOfBounds"));
    private static final SimpleCommandExceptionType INCOMPATIBLE_ENCHANTMENT_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.exceptions.incompatibleEnchantment"));
    private static final SimpleCommandExceptionType INCOMPLETE_ARGUMENT_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.exceptions.incompleteArgument"));

    private boolean lootOnly = false;

    public static EnchantedItemPredicateArgumentType enchantedItem() {
        return new EnchantedItemPredicateArgumentType();
    }

    @SuppressWarnings("unchecked")
    public static Pair<String, Predicate<Item>> getEnchantedItem(CommandContext<FabricClientCommandSource> context, String name) {
        return (Pair<String, Predicate<Item>>) context.getArgument(name, Pair.class);
    }

    public EnchantedItemPredicateArgumentType loot() {
        this.lootOnly = true;
        return this;
    }

    @Override
    public Pair<String, Predicate<Item>> parse(StringReader reader) throws CommandSyntaxException {
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

    private class Parser {

        private final StringReader reader;
        private Consumer<SuggestionsBuilder> suggestor;

        private Parser(StringReader reader) {
            this.reader = reader;
        }

        private Pair<String, Predicate<Item>> parse() throws CommandSyntaxException {
            Item item = parseItemString();
            String itemName = item.getName();
            Predicate<Item> predicate = i -> i.getName().equals(itemName);

            while (this.reader.canRead()) {
                int cursor = this.reader.getCursor();
                if (this.reader.peek() != ' ') {
                    this.reader.setCursor(cursor);
                    throw INCOMPLETE_ARGUMENT_EXCEPTION.create();
                }
                this.reader.skipWhitespace();
                cursor = this.reader.getCursor();
                boolean includeEnchantment = parseWithWithout();
                if (!this.reader.canRead() || this.reader.peek() != ' ') {
                    this.reader.setCursor(cursor);
                    throw INCOMPLETE_ARGUMENT_EXCEPTION.create();
                }
                this.reader.skipWhitespace();
                EnchantmentInstance enchantmentInstance = parseEnchantmentInstance(item);
                predicate = predicate.and(i -> {
                    String enchantmentString = enchantmentInstance.getName();
                    int level = enchantmentInstance.getLevel();
                    if (level < 0) {
                        return includeEnchantment == i.getEnchantments().stream()
                                .anyMatch(pair -> pair.getFirst().equals(enchantmentString));
                    }
                    Pair<String, Integer> pair = new Pair<>(enchantmentString, level);
                    return includeEnchantment == i.getEnchantments().contains(pair);
                });
            }
            return new Pair<>(itemName, predicate);
        }

        private Item parseItemString() throws CommandSyntaxException {
            int cursor = this.reader.getCursor();
            this.suggestor = builder -> {
                SuggestionsBuilder newBuilder = builder.createOffset(cursor);
                if (lootOnly) {
                    CommandSource.suggestMatching(lootableItems, newBuilder);
                } else {
                    CommandSource.suggestMatching(items, builder);
                }

                builder.add(newBuilder);
            };

            String itemString = this.reader.readUnquotedString();
            Item item = Items.getItems().values().stream()
                    .filter(i -> i.getName().equals(itemString))
                    .findAny().orElseThrow(() -> {
                        this.reader.setCursor(cursor);
                        return ITEM_NOT_FOUND_EXCEPTION.create(itemString);
                    });
            if (lootOnly && Arrays.stream(lootableItems).noneMatch(i -> i.equals(item.getName()))) {
                this.reader.setCursor(cursor);
                throw LOOT_ITEM_NOT_FOUND_EXCEPTION.create(itemString);
            }
            return item;
        }

        private boolean parseWithWithout() throws CommandSyntaxException {
            int cursor = this.reader.getCursor();
            this.suggestor = builder -> {
                SuggestionsBuilder newBuilder = builder.createOffset(cursor);
                CommandSource.suggestMatching(Arrays.asList("with", "without"), newBuilder);
                builder.add(newBuilder);
            };

            String literal = this.reader.readUnquotedString();
            return switch (literal) {
                case "with" -> true;
                case "without" -> false;
                default -> {
                    this.reader.setCursor(cursor);
                    throw EXPECTED_WITH_OR_WITHOUT_EXCEPTION.create();
                }
            };
        }

        private EnchantmentInstance parseEnchantmentInstance(Item item) throws CommandSyntaxException {
            int cursor = this.reader.getCursor();
            this.suggestor = builder -> {
                SuggestionsBuilder newBuilder = builder.createOffset(cursor);
                Stream<String> enchantmentStrings = Enchantments.getFor(MCVersion.latest()).stream()
                        .map(Enchantment::getName);
                CommandSource.suggestMatching(enchantmentStrings, newBuilder);
                builder.add(newBuilder);
            };

            Enchantment enchantment = this.parseEnchantment();
            if (!enchantment.getCategory().contains(item.getName().toUpperCase(Locale.ROOT))) {
                this.reader.setCursor(cursor);
                throw INCOMPATIBLE_ENCHANTMENT_EXCEPTION.create();
            }
            // TODO: 28/08/2021 conflicting enchantments
            if (!this.reader.canRead() || reader.peek() != ' ') {
                this.reader.setCursor(cursor);
                throw INCOMPLETE_ARGUMENT_EXCEPTION.create();
            }
            this.reader.skipWhitespace();
            int level = this.parseLevel(enchantment);
            return new EnchantmentInstance(enchantment, level);
        }

        private Enchantment parseEnchantment() throws CommandSyntaxException {
            int cursor = this.reader.getCursor();
            String enchantmentString = this.reader.readUnquotedString();
            return Enchantments.getFor(MCVersion.latest()).stream()
                    .filter(e -> e.getName().equals(enchantmentString))
                    .findAny().orElseThrow(() -> {
                        this.reader.setCursor(cursor);
                        return UNKNOWN_ENCHANTMENT_EXCEPTION.create(enchantmentString);
                    });
        }

        private int parseLevel(Enchantment enchantment) throws CommandSyntaxException {
            int cursor = this.reader.getCursor();
            if (this.reader.canRead() && this.reader.peek() == '*') {
                this.reader.skip();
                return -1;
            }
            int level = this.reader.readInt();
            if (level < enchantment.getMinLevel() || level > enchantment.getMaxLevel()) {
                this.reader.setCursor(cursor);
                throw LEVEL_OUT_OF_BOUNDS_EXCEPTION.create();
            }
            return level;
        }
    }
}
