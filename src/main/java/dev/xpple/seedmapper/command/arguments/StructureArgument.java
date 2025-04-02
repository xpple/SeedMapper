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

public class StructureArgument implements ArgumentType<Integer> {

    private static final Collection<String> EXAMPLES = Arrays.asList("pillager_outpost", "ruined_portal_nether", "trail_ruins");

    //<editor-fold defaultstate="collapsed" desc="private static final Map<String, Integer> STRUCTURES;">
    private static final Map<String, Integer> STRUCTURES = ImmutableMap.<String, Integer>builder()
        .put("feature", Cubiomes.Feature())
        .put("desert_pyramid", Cubiomes.Desert_Pyramid())
        .put("jungle_pyramid", Cubiomes.Jungle_Pyramid())
        .put("swamp_hut", Cubiomes.Swamp_Hut())
        .put("igloo", Cubiomes.Igloo())
        .put("village", Cubiomes.Village())
        .put("ocean_ruin", Cubiomes.Ocean_Ruin())
        .put("shipwreck", Cubiomes.Shipwreck())
        .put("monument", Cubiomes.Monument())
        .put("mansion", Cubiomes.Mansion())
        .put("pillager_outpost", Cubiomes.Outpost())
        .put("ruined_portal", Cubiomes.Ruined_Portal())
        .put("ruined_portal_nether", Cubiomes.Ruined_Portal_N())
        .put("ancient_city", Cubiomes.Ancient_City())
        .put("buried_treasure", Cubiomes.Treasure())
        .put("mineshaft", Cubiomes.Mineshaft())
        .put("desert_well", Cubiomes.Desert_Well())
        .put("geode", Cubiomes.Geode())
        .put("fortress", Cubiomes.Fortress())
        .put("bastion_remnant", Cubiomes.Bastion())
        .put("end_city", Cubiomes.End_City())
        .put("end_gateway", Cubiomes.End_Gateway())
        .put("end_island", Cubiomes.End_Island())
        .put("trail_ruins", Cubiomes.Trail_Ruins())
        .put("trial_chambers", Cubiomes.Trial_Chambers())
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
