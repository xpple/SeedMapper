package dev.xpple.seedmapper.command.arguments;

import com.github.cubiomes.CubiomesHeaders;
import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.xpple.seedmapper.command.CommandExceptions;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BiomeArgument implements ArgumentType<Integer> {

    private static final Collection<String> EXAMPLES = Arrays.asList("desert", "crimson_forest", "giant_spruce_taiga_hills");

    //<editor-fold defaultstate="collapsed" desc="BIOMES">
    private static final Map<String, Integer> BIOMES = ImmutableMap.<String, Integer>builder()
        .put("ocean", CubiomesHeaders.ocean())
        .put("plains", CubiomesHeaders.plains())
        .put("desert", CubiomesHeaders.desert())
        .put("mountains", CubiomesHeaders.mountains())
        .put("forest", CubiomesHeaders.forest())
        .put("taiga", CubiomesHeaders.taiga())
        .put("swamp", CubiomesHeaders.swamp())
        .put("river", CubiomesHeaders.river())
        .put("nether_wastes", CubiomesHeaders.nether_wastes())
        .put("the_end", CubiomesHeaders.the_end())
        .put("frozen_ocean", CubiomesHeaders.frozen_ocean())
        .put("frozen_river", CubiomesHeaders.frozen_river())
        .put("snowy_tundra", CubiomesHeaders.snowy_tundra())
        .put("snowy_mountains", CubiomesHeaders.snowy_mountains())
        .put("mushroom_fields", CubiomesHeaders.mushroom_fields())
        .put("mushroom_field_shore", CubiomesHeaders.mushroom_field_shore())
        .put("beach", CubiomesHeaders.beach())
        .put("desert_hills", CubiomesHeaders.desert_hills())
        .put("wooded_hills", CubiomesHeaders.wooded_hills())
        .put("taiga_hills", CubiomesHeaders.taiga_hills())
        .put("mountain_edge", CubiomesHeaders.mountain_edge())
        .put("jungle", CubiomesHeaders.jungle())
        .put("jungle_hills", CubiomesHeaders.jungle_hills())
        .put("jungle_edge", CubiomesHeaders.jungle_edge())
        .put("deep_ocean", CubiomesHeaders.deep_ocean())
        .put("stone_shore", CubiomesHeaders.stone_shore())
        .put("snowy_beach", CubiomesHeaders.snowy_beach())
        .put("birch_forest", CubiomesHeaders.birch_forest())
        .put("birch_forest_hills", CubiomesHeaders.birch_forest_hills())
        .put("dark_forest", CubiomesHeaders.dark_forest())
        .put("snowy_taiga", CubiomesHeaders.snowy_taiga())
        .put("snowy_taiga_hills", CubiomesHeaders.snowy_taiga_hills())
        .put("giant_tree_taiga", CubiomesHeaders.giant_tree_taiga())
        .put("giant_tree_taiga_hills", CubiomesHeaders.giant_tree_taiga_hills())
        .put("wooded_mountains", CubiomesHeaders.wooded_mountains())
        .put("savanna", CubiomesHeaders.savanna())
        .put("savanna_plateau", CubiomesHeaders.savanna_plateau())
        .put("badlands", CubiomesHeaders.badlands())
        .put("wooded_badlands_plateau", CubiomesHeaders.wooded_badlands_plateau())
        .put("badlands_plateau", CubiomesHeaders.badlands_plateau())
        .put("small_end_islands", CubiomesHeaders.small_end_islands())
        .put("end_midlands", CubiomesHeaders.end_midlands())
        .put("end_highlands", CubiomesHeaders.end_highlands())
        .put("end_barrens", CubiomesHeaders.end_barrens())
        .put("warm_ocean", CubiomesHeaders.warm_ocean())
        .put("lukewarm_ocean", CubiomesHeaders.lukewarm_ocean())
        .put("cold_ocean", CubiomesHeaders.cold_ocean())
        .put("deep_warm_ocean", CubiomesHeaders.deep_warm_ocean())
        .put("deep_lukewarm_ocean", CubiomesHeaders.deep_lukewarm_ocean())
        .put("deep_cold_ocean", CubiomesHeaders.deep_cold_ocean())
        .put("deep_frozen_ocean", CubiomesHeaders.deep_frozen_ocean())
        .put("seasonal_forest", CubiomesHeaders.seasonal_forest())
        .put("rainforest", CubiomesHeaders.rainforest())
        .put("shrubland", CubiomesHeaders.shrubland())
        .put("the_void", CubiomesHeaders.the_void())
        .put("sunflower_plains", CubiomesHeaders.sunflower_plains())
        .put("desert_lakes", CubiomesHeaders.desert_lakes())
        .put("gravelly_mountains", CubiomesHeaders.gravelly_mountains())
        .put("flower_forest", CubiomesHeaders.flower_forest())
        .put("taiga_mountains", CubiomesHeaders.taiga_mountains())
        .put("swamp_hills", CubiomesHeaders.swamp_hills())
        .put("ice_spikes", CubiomesHeaders.ice_spikes())
        .put("modified_jungle", CubiomesHeaders.modified_jungle())
        .put("modified_jungle_edge", CubiomesHeaders.modified_jungle_edge())
        .put("tall_birch_forest", CubiomesHeaders.tall_birch_forest())
        .put("tall_birch_hills", CubiomesHeaders.tall_birch_hills())
        .put("dark_forest_hills", CubiomesHeaders.dark_forest_hills())
        .put("snowy_taiga_mountains", CubiomesHeaders.snowy_taiga_mountains())
        .put("giant_spruce_taiga", CubiomesHeaders.giant_spruce_taiga())
        .put("giant_spruce_taiga_hills", CubiomesHeaders.giant_spruce_taiga_hills())
        .put("modified_gravelly_mountains", CubiomesHeaders.modified_gravelly_mountains())
        .put("shattered_savanna", CubiomesHeaders.shattered_savanna())
        .put("shattered_savanna_plateau", CubiomesHeaders.shattered_savanna_plateau())
        .put("eroded_badlands", CubiomesHeaders.eroded_badlands())
        .put("modified_wooded_badlands_plateau", CubiomesHeaders.modified_wooded_badlands_plateau())
        .put("modified_badlands_plateau", CubiomesHeaders.modified_badlands_plateau())
        .put("bamboo_jungle", CubiomesHeaders.bamboo_jungle())
        .put("bamboo_jungle_hills", CubiomesHeaders.bamboo_jungle_hills())
        .put("soul_sand_valley", CubiomesHeaders.soul_sand_valley())
        .put("crimson_forest", CubiomesHeaders.crimson_forest())
        .put("warped_forest", CubiomesHeaders.warped_forest())
        .put("basalt_deltas", CubiomesHeaders.basalt_deltas())
        .put("dripstone_caves", CubiomesHeaders.dripstone_caves())
        .put("lush_caves", CubiomesHeaders.lush_caves())
        .put("meadow", CubiomesHeaders.meadow())
        .put("grove", CubiomesHeaders.grove())
        .put("snowy_slopes", CubiomesHeaders.snowy_slopes())
        .put("jagged_peaks", CubiomesHeaders.jagged_peaks())
        .put("frozen_peaks", CubiomesHeaders.frozen_peaks())
        .put("stony_peaks", CubiomesHeaders.stony_peaks())
        .put("old_growth_birch_forest", CubiomesHeaders.old_growth_birch_forest())
        .put("old_growth_pine_taiga", CubiomesHeaders.old_growth_pine_taiga())
        .put("old_growth_spruce_taiga", CubiomesHeaders.old_growth_spruce_taiga())
        .put("snowy_plains", CubiomesHeaders.snowy_plains())
        .put("sparse_jungle", CubiomesHeaders.sparse_jungle())
        .put("stony_shore", CubiomesHeaders.stony_shore())
        .put("windswept_hills", CubiomesHeaders.windswept_hills())
        .put("windswept_forest", CubiomesHeaders.windswept_forest())
        .put("windswept_gravelly_hills", CubiomesHeaders.windswept_gravelly_hills())
        .put("windswept_savanna", CubiomesHeaders.windswept_savanna())
        .put("wooded_badlands", CubiomesHeaders.wooded_badlands())
        .put("deep_dark", CubiomesHeaders.deep_dark())
        .put("mangrove_swamp", CubiomesHeaders.mangrove_swamp())
        .put("cherry_grove", CubiomesHeaders.cherry_grove())
        .put("pale_garden", CubiomesHeaders.pale_garden())
        .build();
    //</editor-fold>

    public static BiomeArgument biome() {
        return new BiomeArgument();
    }

    public static int getBiome(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, Integer.class);
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String biomeString = reader.readUnquotedString();
        Integer biome = BIOMES.get(biomeString);
        if (biome == null) {
            reader.setCursor(cursor);
            throw CommandExceptions.UNKNOWN_BIOME_EXCEPTION.create(biomeString);
        }
        return biome;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(BIOMES.keySet(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
