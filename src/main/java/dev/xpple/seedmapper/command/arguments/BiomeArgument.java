package dev.xpple.seedmapper.command.arguments;

import com.github.cubiomes.Cubiomes;
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

    //<editor-fold defaultstate="collapsed" desc="private static final Map<String, Integer> BIOMES;">
    private static final Map<String, Integer> BIOMES = ImmutableMap.<String, Integer>builder()
        .put("ocean", Cubiomes.ocean())
        .put("plains", Cubiomes.plains())
        .put("desert", Cubiomes.desert())
        .put("mountains", Cubiomes.mountains())
        .put("forest", Cubiomes.forest())
        .put("taiga", Cubiomes.taiga())
        .put("swamp", Cubiomes.swamp())
        .put("river", Cubiomes.river())
        .put("nether_wastes", Cubiomes.nether_wastes())
        .put("the_end", Cubiomes.the_end())
        .put("frozen_ocean", Cubiomes.frozen_ocean())
        .put("frozen_river", Cubiomes.frozen_river())
        .put("snowy_tundra", Cubiomes.snowy_tundra())
        .put("snowy_mountains", Cubiomes.snowy_mountains())
        .put("mushroom_fields", Cubiomes.mushroom_fields())
        .put("mushroom_field_shore", Cubiomes.mushroom_field_shore())
        .put("beach", Cubiomes.beach())
        .put("desert_hills", Cubiomes.desert_hills())
        .put("wooded_hills", Cubiomes.wooded_hills())
        .put("taiga_hills", Cubiomes.taiga_hills())
        .put("mountain_edge", Cubiomes.mountain_edge())
        .put("jungle", Cubiomes.jungle())
        .put("jungle_hills", Cubiomes.jungle_hills())
        .put("jungle_edge", Cubiomes.jungle_edge())
        .put("deep_ocean", Cubiomes.deep_ocean())
        .put("stone_shore", Cubiomes.stone_shore())
        .put("snowy_beach", Cubiomes.snowy_beach())
        .put("birch_forest", Cubiomes.birch_forest())
        .put("birch_forest_hills", Cubiomes.birch_forest_hills())
        .put("dark_forest", Cubiomes.dark_forest())
        .put("snowy_taiga", Cubiomes.snowy_taiga())
        .put("snowy_taiga_hills", Cubiomes.snowy_taiga_hills())
        .put("giant_tree_taiga", Cubiomes.giant_tree_taiga())
        .put("giant_tree_taiga_hills", Cubiomes.giant_tree_taiga_hills())
        .put("wooded_mountains", Cubiomes.wooded_mountains())
        .put("savanna", Cubiomes.savanna())
        .put("savanna_plateau", Cubiomes.savanna_plateau())
        .put("badlands", Cubiomes.badlands())
        .put("wooded_badlands_plateau", Cubiomes.wooded_badlands_plateau())
        .put("badlands_plateau", Cubiomes.badlands_plateau())
        .put("small_end_islands", Cubiomes.small_end_islands())
        .put("end_midlands", Cubiomes.end_midlands())
        .put("end_highlands", Cubiomes.end_highlands())
        .put("end_barrens", Cubiomes.end_barrens())
        .put("warm_ocean", Cubiomes.warm_ocean())
        .put("lukewarm_ocean", Cubiomes.lukewarm_ocean())
        .put("cold_ocean", Cubiomes.cold_ocean())
        .put("deep_warm_ocean", Cubiomes.deep_warm_ocean())
        .put("deep_lukewarm_ocean", Cubiomes.deep_lukewarm_ocean())
        .put("deep_cold_ocean", Cubiomes.deep_cold_ocean())
        .put("deep_frozen_ocean", Cubiomes.deep_frozen_ocean())
        .put("seasonal_forest", Cubiomes.seasonal_forest())
        .put("rainforest", Cubiomes.rainforest())
        .put("shrubland", Cubiomes.shrubland())
        .put("the_void", Cubiomes.the_void())
        .put("sunflower_plains", Cubiomes.sunflower_plains())
        .put("desert_lakes", Cubiomes.desert_lakes())
        .put("gravelly_mountains", Cubiomes.gravelly_mountains())
        .put("flower_forest", Cubiomes.flower_forest())
        .put("taiga_mountains", Cubiomes.taiga_mountains())
        .put("swamp_hills", Cubiomes.swamp_hills())
        .put("ice_spikes", Cubiomes.ice_spikes())
        .put("modified_jungle", Cubiomes.modified_jungle())
        .put("modified_jungle_edge", Cubiomes.modified_jungle_edge())
        .put("tall_birch_forest", Cubiomes.tall_birch_forest())
        .put("tall_birch_hills", Cubiomes.tall_birch_hills())
        .put("dark_forest_hills", Cubiomes.dark_forest_hills())
        .put("snowy_taiga_mountains", Cubiomes.snowy_taiga_mountains())
        .put("giant_spruce_taiga", Cubiomes.giant_spruce_taiga())
        .put("giant_spruce_taiga_hills", Cubiomes.giant_spruce_taiga_hills())
        .put("modified_gravelly_mountains", Cubiomes.modified_gravelly_mountains())
        .put("shattered_savanna", Cubiomes.shattered_savanna())
        .put("shattered_savanna_plateau", Cubiomes.shattered_savanna_plateau())
        .put("eroded_badlands", Cubiomes.eroded_badlands())
        .put("modified_wooded_badlands_plateau", Cubiomes.modified_wooded_badlands_plateau())
        .put("modified_badlands_plateau", Cubiomes.modified_badlands_plateau())
        .put("bamboo_jungle", Cubiomes.bamboo_jungle())
        .put("bamboo_jungle_hills", Cubiomes.bamboo_jungle_hills())
        .put("soul_sand_valley", Cubiomes.soul_sand_valley())
        .put("crimson_forest", Cubiomes.crimson_forest())
        .put("warped_forest", Cubiomes.warped_forest())
        .put("basalt_deltas", Cubiomes.basalt_deltas())
        .put("dripstone_caves", Cubiomes.dripstone_caves())
        .put("lush_caves", Cubiomes.lush_caves())
        .put("meadow", Cubiomes.meadow())
        .put("grove", Cubiomes.grove())
        .put("snowy_slopes", Cubiomes.snowy_slopes())
        .put("jagged_peaks", Cubiomes.jagged_peaks())
        .put("frozen_peaks", Cubiomes.frozen_peaks())
        .put("stony_peaks", Cubiomes.stony_peaks())
        .put("old_growth_birch_forest", Cubiomes.old_growth_birch_forest())
        .put("old_growth_pine_taiga", Cubiomes.old_growth_pine_taiga())
        .put("old_growth_spruce_taiga", Cubiomes.old_growth_spruce_taiga())
        .put("snowy_plains", Cubiomes.snowy_plains())
        .put("sparse_jungle", Cubiomes.sparse_jungle())
        .put("stony_shore", Cubiomes.stony_shore())
        .put("windswept_hills", Cubiomes.windswept_hills())
        .put("windswept_forest", Cubiomes.windswept_forest())
        .put("windswept_gravelly_hills", Cubiomes.windswept_gravelly_hills())
        .put("windswept_savanna", Cubiomes.windswept_savanna())
        .put("wooded_badlands", Cubiomes.wooded_badlands())
        .put("deep_dark", Cubiomes.deep_dark())
        .put("mangrove_swamp", Cubiomes.mangrove_swamp())
        .put("cherry_grove", Cubiomes.cherry_grove())
        .put("pale_garden", Cubiomes.pale_garden())
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
