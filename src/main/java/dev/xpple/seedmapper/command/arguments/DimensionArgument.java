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

public class DimensionArgument implements ArgumentType<Integer> {

    private static final Collection<String> EXAMPLES = Arrays.asList("overworld", "the_nether", "the_end");

    private static final Map<String, Integer> DIMENSIONS = ImmutableMap.<String, Integer>builder()
        .put("overworld", Cubiomes.DIM_OVERWORLD())
        .put("the_nether", Cubiomes.DIM_NETHER())
        .put("the_end", Cubiomes.DIM_END())
        .build();

    public static DimensionArgument dimension() {
        return new DimensionArgument();
    }

    public static int getDimension(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, Integer.class);
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String dimensionString = reader.readUnquotedString();
        Integer dimension = DIMENSIONS.get(dimensionString);
        if (dimension == null) {
            reader.setCursor(cursor);
            throw CommandExceptions.UNKNOWN_DIMENSION_EXCEPTION.create(dimensionString);
        }
        return dimension;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(DIMENSIONS.keySet(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
