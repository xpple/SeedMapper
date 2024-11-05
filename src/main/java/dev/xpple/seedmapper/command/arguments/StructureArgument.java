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

public class StructureArgument implements ArgumentType<Integer> {

    private static final Collection<String> EXAMPLES = Arrays.asList("overworld", "the_nether", "the_end");

    //<editor-fold defaultstate="collapsed" desc="private static final Map<String, Integer> STRUCTURES;">
    private static final Map<String, Integer> STRUCTURES = ImmutableMap.<String, Integer>builder()
        .put("feature", CubiomesHeaders.Feature())
        .put("desert_pyramid", CubiomesHeaders.Desert_Pyramid())
        .put("jungle_pyramid", CubiomesHeaders.Jungle_Pyramid())
        .put("swamp_hut", CubiomesHeaders.Swamp_Hut())
        .put("igloo", CubiomesHeaders.Igloo())
        .put("village", CubiomesHeaders.Village())
        .put("ocean_ruin", CubiomesHeaders.Ocean_Ruin())
        .put("shipwreck", CubiomesHeaders.Shipwreck())
        .put("monument", CubiomesHeaders.Monument())
        .put("mansion", CubiomesHeaders.Mansion())
        .put("pillager_outpost", CubiomesHeaders.Outpost())
        .put("ruined_portal", CubiomesHeaders.Ruined_Portal())
        .put("ruined_portal_nether", CubiomesHeaders.Ruined_Portal_N())
        .put("ancient_city", CubiomesHeaders.Ancient_City())
        .put("buried_treasure", CubiomesHeaders.Treasure())
        .put("mineshaft", CubiomesHeaders.Mineshaft())
        .put("desert_well", CubiomesHeaders.Desert_Well())
        .put("geode", CubiomesHeaders.Geode())
        .put("fortress", CubiomesHeaders.Fortress())
        .put("bastion_remnant", CubiomesHeaders.Bastion())
        .put("end_city", CubiomesHeaders.End_City())
        .put("end_gateway", CubiomesHeaders.End_Gateway())
        .put("end_island", CubiomesHeaders.End_Island())
        .put("trail_ruins", CubiomesHeaders.Trail_Ruins())
        .put("trial_chambers", CubiomesHeaders.Trial_Chambers())
        .build();
    //</editor-fold>

    public static StructureArgument structure() {
        return new StructureArgument();
    }

    public static int getStructure(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, Integer.class);
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String structureString = reader.readUnquotedString();
        Integer structure = STRUCTURES.get(structureString);
        if (structure == null) {
            reader.setCursor(cursor);
            throw CommandExceptions.UNKNOWN_STRUCTURE_EXCEPTION.create(structureString);
        }
        return structure;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(STRUCTURES.keySet(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
