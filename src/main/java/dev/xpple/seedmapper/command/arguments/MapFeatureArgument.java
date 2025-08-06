package dev.xpple.seedmapper.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.xpple.seedmapper.command.CommandExceptions;
import dev.xpple.seedmapper.seedmap.MapFeature;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class MapFeatureArgument implements ArgumentType<MapFeature> {

    private static final Collection<String> EXAMPLES = Arrays.asList("ruined_portal", "iron_ore_vein", "stronghold");

    public static MapFeatureArgument mapFeature() {
        return new MapFeatureArgument();
    }

    public static MapFeature getMapFeature(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, MapFeature.class);
    }

    @Override
    public MapFeature parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String featureString = reader.readUnquotedString();
        MapFeature feature = MapFeature.BY_NAME.get(featureString);
        if (feature == null) {
            reader.setCursor(cursor);
            throw CommandExceptions.UNKNOWN_MAP_FEATURE_EXCEPTION.create(featureString);
        }
        return feature;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(MapFeature.BY_NAME.keySet(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
