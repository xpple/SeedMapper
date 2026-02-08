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

public class CanyonCarverArgument implements ArgumentType<Integer> {

    private static final Collection<String> EXAMPLES = Arrays.asList("canyon", "underwater_canyon");

    public static final Map<String, Integer> CANYON_CARVERS = ImmutableMap.<String, Integer>builder()
        .put("canyon", Cubiomes.CANYON_CARVER())
        .put("underwater_canyon", Cubiomes.UNDERWATER_CANYON_CARVER())
        .build();

    public static CanyonCarverArgument canyonCarver() {
        return new CanyonCarverArgument();
    }

    public static int getCanyonCarver(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, Integer.class);
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String canyonCarverString = reader.readUnquotedString();
        Integer canyonCarver = CANYON_CARVERS.get(canyonCarverString);
        if (canyonCarver == null) {
            reader.setCursor(cursor);
            throw CommandExceptions.UNKNOWN_CANYON_CARVER_EXCEPTION.create(canyonCarverString);
        }
        return canyonCarver;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(CANYON_CARVERS.keySet(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
