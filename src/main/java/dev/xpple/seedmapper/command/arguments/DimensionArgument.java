package dev.xpple.seedmapper.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.seedfinding.mccore.state.Dimension;
import dev.xpple.seedmapper.command.CommandExceptions;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class DimensionArgument implements ArgumentType<Dimension> {

    private static final Collection<String> EXAMPLES = Arrays.asList("overworld", "the_nether", "the_end");

    public static DimensionArgument dimension() {
        return new DimensionArgument();
    }

    public static Dimension getDimension(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, Dimension.class);
    }

    @Override
    public Dimension parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String dimensionString = reader.readUnquotedString();
        Dimension dimension = Dimension.fromString(dimensionString);
        if (dimension == null) {
            reader.setCursor(cursor);
            throw CommandExceptions.UNKNOWN_DIMENSION_EXCEPTION.create(dimensionString);
        }
        return dimension;
    }

    @Override
    public CompletableFuture<Suggestions> listSuggestions(CommandContext context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(Arrays.stream(Dimension.values()).map(Dimension::getName), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
