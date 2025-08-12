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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ItemAndEnchantmentsPredicateArgument implements ArgumentType<ItemAndEnchantmentsPredicateArgument.EnchantedItem> {

    private static final Collection<String> EXAMPLES = Arrays.asList("apple", "diamond_pickaxe", "tnt");

    private static final SimpleCommandExceptionType EXPECTED_WITH_WITHOUT_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.exceptions.expectedWithWithout"));

    //<editor-fold defaultstate="collapsed" desc="private static final Map<String, Integer> ITEMS;">
    private static final Map<String, Integer> ITEMS = ImmutableMap.<String, Integer>builder()
        .put("ancient_debris", Cubiomes.ITEM_ANCIENT_DEBRIS())
        .put("apple", Cubiomes.ITEM_APPLE())
        .put("arrow", Cubiomes.ITEM_ARROW())
        .put("beetroot_seeds", Cubiomes.ITEM_BEETROOT_SEEDS())
        .put("bell", Cubiomes.ITEM_BELL())
        .put("bone", Cubiomes.ITEM_BONE())
        .put("bone_block", Cubiomes.ITEM_BONE_BLOCK())
        .put("book", Cubiomes.ITEM_BOOK())
        .put("carrot", Cubiomes.ITEM_CARROT())
        .put("chain", Cubiomes.ITEM_CHAIN())
        .put("clock", Cubiomes.ITEM_CLOCK())
        .put("coal", Cubiomes.ITEM_COAL())
        .put("cooked_cod", Cubiomes.ITEM_COOKED_COD())
        .put("cooked_porkchop", Cubiomes.ITEM_COOKED_PORKCHOP())
        .put("cooked_salmon", Cubiomes.ITEM_COOKED_SALMON())
        .put("crossbow", Cubiomes.ITEM_CROSSBOW())
        .put("crying_obsidian", Cubiomes.ITEM_CRYING_OBSIDIAN())
        .put("dark_oak_log", Cubiomes.ITEM_DARK_OAK_LOG())
        .put("diamond", Cubiomes.ITEM_DIAMOND())
        .put("diamond_boots", Cubiomes.ITEM_DIAMOND_BOOTS())
        .put("diamond_chestplate", Cubiomes.ITEM_DIAMOND_CHESTPLATE())
        .put("diamond_helmet", Cubiomes.ITEM_DIAMOND_HELMET())
        .put("diamond_horse_armor", Cubiomes.ITEM_DIAMOND_HORSE_ARMOR())
        .put("diamond_leggings", Cubiomes.ITEM_DIAMOND_LEGGINGS())
        .put("diamond_pickaxe", Cubiomes.ITEM_DIAMOND_PICKAXE())
        .put("diamond_shovel", Cubiomes.ITEM_DIAMOND_SHOVEL())
        .put("diamond_sword", Cubiomes.ITEM_DIAMOND_SWORD())
        .put("dune_armor_trim_smithing_template", Cubiomes.ITEM_DUNE_ARMOR_TRIM_SMITHING_TEMPLATE())
        .put("emerald", Cubiomes.ITEM_EMERALD())
        .put("enchanted_golden_apple", Cubiomes.ITEM_ENCHANTED_GOLDEN_APPLE())
        .put("experience_bottle", Cubiomes.ITEM_EXPERIENCE_BOTTLE())
        .put("fire_charge", Cubiomes.ITEM_FIRE_CHARGE())
        .put("flint", Cubiomes.ITEM_FLINT())
        .put("flint_and_steel", Cubiomes.ITEM_FLINT_AND_STEEL())
        .put("gilded_blackstone", Cubiomes.ITEM_GILDED_BLACKSTONE())
        .put("glistering_melon_slice", Cubiomes.ITEM_GLISTERING_MELON_SLICE())
        .put("goat_horn", Cubiomes.ITEM_GOAT_HORN())
        .put("golden_apple", Cubiomes.ITEM_GOLDEN_APPLE())
        .put("golden_axe", Cubiomes.ITEM_GOLDEN_AXE())
        .put("golden_boots", Cubiomes.ITEM_GOLDEN_BOOTS())
        .put("golden_carrot", Cubiomes.ITEM_GOLDEN_CARROT())
        .put("golden_chestplate", Cubiomes.ITEM_GOLDEN_CHESTPLATE())
        .put("golden_helmet", Cubiomes.ITEM_GOLDEN_HELMET())
        .put("golden_hoe", Cubiomes.ITEM_GOLDEN_HOE())
        .put("golden_horse_armor", Cubiomes.ITEM_GOLDEN_HORSE_ARMOR())
        .put("golden_leggings", Cubiomes.ITEM_GOLDEN_LEGGINGS())
        .put("golden_pickaxe", Cubiomes.ITEM_GOLDEN_PICKAXE())
        .put("golden_shovel", Cubiomes.ITEM_GOLDEN_SHOVEL())
        .put("golden_sword", Cubiomes.ITEM_GOLDEN_SWORD())
        .put("gold_block", Cubiomes.ITEM_GOLD_BLOCK())
        .put("gold_ingot", Cubiomes.ITEM_GOLD_INGOT())
        .put("gold_nugget", Cubiomes.ITEM_GOLD_NUGGET())
        .put("gunpowder", Cubiomes.ITEM_GUNPOWDER())
        .put("heart_of_the_sea", Cubiomes.ITEM_HEART_OF_THE_SEA())
        .put("iron_block", Cubiomes.ITEM_IRON_BLOCK())
        .put("iron_boots", Cubiomes.ITEM_IRON_BOOTS())
        .put("iron_chestplate", Cubiomes.ITEM_IRON_CHESTPLATE())
        .put("iron_helmet", Cubiomes.ITEM_IRON_HELMET())
        .put("iron_horse_armor", Cubiomes.ITEM_IRON_HORSE_ARMOR())
        .put("iron_ingot", Cubiomes.ITEM_IRON_INGOT())
        .put("iron_leggings", Cubiomes.ITEM_IRON_LEGGINGS())
        .put("iron_nugget", Cubiomes.ITEM_IRON_NUGGET())
        .put("iron_pickaxe", Cubiomes.ITEM_IRON_PICKAXE())
        .put("iron_shovel", Cubiomes.ITEM_IRON_SHOVEL())
        .put("iron_sword", Cubiomes.ITEM_IRON_SWORD())
        .put("leather", Cubiomes.ITEM_LEATHER())
        .put("leather_chestplate", Cubiomes.ITEM_LEATHER_CHESTPLATE())
        .put("light_weighted_pressure_plate", Cubiomes.ITEM_LIGHT_WEIGHTED_PRESSURE_PLATE())
        .put("lodestone", Cubiomes.ITEM_LODESTONE())
        .put("magma_cream", Cubiomes.ITEM_MAGMA_CREAM())
        .put("music_disc_pigstep", Cubiomes.ITEM_MUSIC_DISC_PIGSTEP())
        .put("netherite_scrap", Cubiomes.ITEM_NETHERITE_SCRAP())
        .put("netherite_upgrade_smithing_template", Cubiomes.ITEM_NETHERITE_UPGRADE_SMITHING_TEMPLATE())
        .put("nether_wart", Cubiomes.ITEM_NETHER_WART())
        .put("obsidian", Cubiomes.ITEM_OBSIDIAN())
        .put("piglin_banner_pattern", Cubiomes.ITEM_PIGLIN_BANNER_PATTERN())
        .put("potion", Cubiomes.ITEM_POTION())
        .put("potato", Cubiomes.ITEM_POTATO())
        .put("prismarine_crystals", Cubiomes.ITEM_PRISMARINE_CRYSTALS())
        .put("rib_armor_trim_smithing_template", Cubiomes.ITEM_RIB_ARMOR_TRIM_SMITHING_TEMPLATE())
        .put("rotten_flesh", Cubiomes.ITEM_ROTTEN_FLESH())
        .put("saddle", Cubiomes.ITEM_SADDLE())
        .put("sand", Cubiomes.ITEM_SAND())
        .put("sentry_armor_trim_smithing_template", Cubiomes.ITEM_SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE())
        .put("snout_armor_trim_smithing_template", Cubiomes.ITEM_SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE())
        .put("spectral_arrow", Cubiomes.ITEM_SPECTRAL_ARROW())
        .put("spider_eye", Cubiomes.ITEM_SPIDER_EYE())
        .put("spire_armor_trim_smithing_template", Cubiomes.ITEM_SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE())
        .put("stone_axe", Cubiomes.ITEM_STONE_AXE())
        .put("string", Cubiomes.ITEM_STRING())
        .put("tnt", Cubiomes.ITEM_TNT())
        .put("tripwire_hook", Cubiomes.ITEM_TRIPWIRE_HOOK())
        .put("wheat", Cubiomes.ITEM_WHEAT())
        .build();
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="public static final Map<Integer, Item> ID_TO_MC;">
    public static final Map<Integer, Item> ITEM_ID_TO_MC = ImmutableMap.<Integer, Item>builder()
        .put(Cubiomes.ITEM_ANCIENT_DEBRIS(), Items.ANCIENT_DEBRIS)
        .put(Cubiomes.ITEM_APPLE(), Items.APPLE)
        .put(Cubiomes.ITEM_ARROW(), Items.ARROW)
        .put(Cubiomes.ITEM_BEETROOT_SEEDS(), Items.BEETROOT_SEEDS)
        .put(Cubiomes.ITEM_BELL(), Items.BELL)
        .put(Cubiomes.ITEM_BONE(), Items.BONE)
        .put(Cubiomes.ITEM_BONE_BLOCK(), Items.BONE_BLOCK)
        .put(Cubiomes.ITEM_BOOK(), Items.BOOK)
        .put(Cubiomes.ITEM_CARROT(), Items.CARROT)
        .put(Cubiomes.ITEM_CHAIN(), Items.CHAIN)
        .put(Cubiomes.ITEM_CLOCK(), Items.CLOCK)
        .put(Cubiomes.ITEM_COAL(), Items.COAL)
        .put(Cubiomes.ITEM_COOKED_COD(), Items.COOKED_COD)
        .put(Cubiomes.ITEM_COOKED_PORKCHOP(), Items.COOKED_PORKCHOP)
        .put(Cubiomes.ITEM_COOKED_SALMON(), Items.COOKED_SALMON)
        .put(Cubiomes.ITEM_CROSSBOW(), Items.CROSSBOW)
        .put(Cubiomes.ITEM_CRYING_OBSIDIAN(), Items.CRYING_OBSIDIAN)
        .put(Cubiomes.ITEM_DARK_OAK_LOG(), Items.DARK_OAK_LOG)
        .put(Cubiomes.ITEM_DIAMOND(), Items.DIAMOND)
        .put(Cubiomes.ITEM_DIAMOND_BOOTS(), Items.DIAMOND_BOOTS)
        .put(Cubiomes.ITEM_DIAMOND_CHESTPLATE(), Items.DIAMOND_CHESTPLATE)
        .put(Cubiomes.ITEM_DIAMOND_HELMET(), Items.DIAMOND_HELMET)
        .put(Cubiomes.ITEM_DIAMOND_HORSE_ARMOR(), Items.DIAMOND_HORSE_ARMOR)
        .put(Cubiomes.ITEM_DIAMOND_LEGGINGS(), Items.DIAMOND_LEGGINGS)
        .put(Cubiomes.ITEM_DIAMOND_PICKAXE(), Items.DIAMOND_PICKAXE)
        .put(Cubiomes.ITEM_DIAMOND_SHOVEL(), Items.DIAMOND_SHOVEL)
        .put(Cubiomes.ITEM_DIAMOND_SWORD(), Items.DIAMOND_SWORD)
        .put(Cubiomes.ITEM_DUNE_ARMOR_TRIM_SMITHING_TEMPLATE(), Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE)
        .put(Cubiomes.ITEM_EMERALD(), Items.EMERALD)
        .put(Cubiomes.ITEM_ENCHANTED_GOLDEN_APPLE(), Items.ENCHANTED_GOLDEN_APPLE)
        .put(Cubiomes.ITEM_EXPERIENCE_BOTTLE(), Items.EXPERIENCE_BOTTLE)
        .put(Cubiomes.ITEM_FIRE_CHARGE(), Items.FIRE_CHARGE)
        .put(Cubiomes.ITEM_FLINT(), Items.FLINT)
        .put(Cubiomes.ITEM_FLINT_AND_STEEL(), Items.FLINT_AND_STEEL)
        .put(Cubiomes.ITEM_GILDED_BLACKSTONE(), Items.GILDED_BLACKSTONE)
        .put(Cubiomes.ITEM_GLISTERING_MELON_SLICE(), Items.GLISTERING_MELON_SLICE)
        .put(Cubiomes.ITEM_GOAT_HORN(), Items.GOAT_HORN)
        .put(Cubiomes.ITEM_GOLDEN_APPLE(), Items.GOLDEN_APPLE)
        .put(Cubiomes.ITEM_GOLDEN_AXE(), Items.GOLDEN_AXE)
        .put(Cubiomes.ITEM_GOLDEN_BOOTS(), Items.GOLDEN_BOOTS)
        .put(Cubiomes.ITEM_GOLDEN_CARROT(), Items.GOLDEN_CARROT)
        .put(Cubiomes.ITEM_GOLDEN_CHESTPLATE(), Items.GOLDEN_CHESTPLATE)
        .put(Cubiomes.ITEM_GOLDEN_HELMET(), Items.GOLDEN_HELMET)
        .put(Cubiomes.ITEM_GOLDEN_HOE(), Items.GOLDEN_HOE)
        .put(Cubiomes.ITEM_GOLDEN_HORSE_ARMOR(), Items.GOLDEN_HORSE_ARMOR)
        .put(Cubiomes.ITEM_GOLDEN_LEGGINGS(), Items.GOLDEN_LEGGINGS)
        .put(Cubiomes.ITEM_GOLDEN_PICKAXE(), Items.GOLDEN_PICKAXE)
        .put(Cubiomes.ITEM_GOLDEN_SHOVEL(), Items.GOLDEN_SHOVEL)
        .put(Cubiomes.ITEM_GOLDEN_SWORD(), Items.GOLDEN_SWORD)
        .put(Cubiomes.ITEM_GOLD_BLOCK(), Items.GOLD_BLOCK)
        .put(Cubiomes.ITEM_GOLD_INGOT(), Items.GOLD_INGOT)
        .put(Cubiomes.ITEM_GOLD_NUGGET(), Items.GOLD_NUGGET)
        .put(Cubiomes.ITEM_GUNPOWDER(), Items.GUNPOWDER)
        .put(Cubiomes.ITEM_HEART_OF_THE_SEA(), Items.HEART_OF_THE_SEA)
        .put(Cubiomes.ITEM_IRON_BLOCK(), Items.IRON_BLOCK)
        .put(Cubiomes.ITEM_IRON_BOOTS(), Items.IRON_BOOTS)
        .put(Cubiomes.ITEM_IRON_CHESTPLATE(), Items.IRON_CHESTPLATE)
        .put(Cubiomes.ITEM_IRON_HELMET(), Items.IRON_HELMET)
        .put(Cubiomes.ITEM_IRON_HORSE_ARMOR(), Items.IRON_HORSE_ARMOR)
        .put(Cubiomes.ITEM_IRON_INGOT(), Items.IRON_INGOT)
        .put(Cubiomes.ITEM_IRON_LEGGINGS(), Items.IRON_LEGGINGS)
        .put(Cubiomes.ITEM_IRON_NUGGET(), Items.IRON_NUGGET)
        .put(Cubiomes.ITEM_IRON_PICKAXE(), Items.IRON_PICKAXE)
        .put(Cubiomes.ITEM_IRON_SHOVEL(), Items.IRON_SHOVEL)
        .put(Cubiomes.ITEM_IRON_SWORD(), Items.IRON_SWORD)
        .put(Cubiomes.ITEM_LEATHER(), Items.LEATHER)
        .put(Cubiomes.ITEM_LEATHER_CHESTPLATE(), Items.LEATHER_CHESTPLATE)
        .put(Cubiomes.ITEM_LIGHT_WEIGHTED_PRESSURE_PLATE(), Items.LIGHT_WEIGHTED_PRESSURE_PLATE)
        .put(Cubiomes.ITEM_LODESTONE(), Items.LODESTONE)
        .put(Cubiomes.ITEM_MAGMA_CREAM(), Items.MAGMA_CREAM)
        .put(Cubiomes.ITEM_MUSIC_DISC_PIGSTEP(), Items.MUSIC_DISC_PIGSTEP)
        .put(Cubiomes.ITEM_NETHERITE_SCRAP(), Items.NETHERITE_SCRAP)
        .put(Cubiomes.ITEM_NETHERITE_UPGRADE_SMITHING_TEMPLATE(), Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
        .put(Cubiomes.ITEM_NETHER_WART(), Items.NETHER_WART)
        .put(Cubiomes.ITEM_OBSIDIAN(), Items.OBSIDIAN)
        .put(Cubiomes.ITEM_PIGLIN_BANNER_PATTERN(), Items.PIGLIN_BANNER_PATTERN)
        .put(Cubiomes.ITEM_POTION(), Items.POTION)
        .put(Cubiomes.ITEM_POTATO(), Items.POTATO)
        .put(Cubiomes.ITEM_PRISMARINE_CRYSTALS(), Items.PRISMARINE_CRYSTALS)
        .put(Cubiomes.ITEM_RIB_ARMOR_TRIM_SMITHING_TEMPLATE(), Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE)
        .put(Cubiomes.ITEM_ROTTEN_FLESH(), Items.ROTTEN_FLESH)
        .put(Cubiomes.ITEM_SADDLE(), Items.SADDLE)
        .put(Cubiomes.ITEM_SAND(), Items.SAND)
        .put(Cubiomes.ITEM_SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE(), Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE)
        .put(Cubiomes.ITEM_SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE(), Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE)
        .put(Cubiomes.ITEM_SPECTRAL_ARROW(), Items.SPECTRAL_ARROW)
        .put(Cubiomes.ITEM_SPIDER_EYE(), Items.SPIDER_EYE)
        .put(Cubiomes.ITEM_SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE(), Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE)
        .put(Cubiomes.ITEM_STONE_AXE(), Items.STONE_AXE)
        .put(Cubiomes.ITEM_STRING(), Items.STRING)
        .put(Cubiomes.ITEM_TNT(), Items.TNT)
        .put(Cubiomes.ITEM_TRIPWIRE_HOOK(), Items.TRIPWIRE_HOOK)
        .put(Cubiomes.ITEM_WHEAT(), Items.WHEAT)
        .build();
    //</editor-fold>

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
