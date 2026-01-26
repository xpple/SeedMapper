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

public class CaveCarverArgument implements ArgumentType<Integer> {

    private static final Collection<String> EXAMPLES = Arrays.asList("cave", "nether_cave");

    private static final Map<String, Integer> CAVE_CARVERS = ImmutableMap.<String, Integer>builder()
        .put("cave", Cubiomes.CAVE_CARVER())
        .put("cave_extra_underground", Cubiomes.CAVE_EXTRA_UNDERGROUND_CARVER())
        .put("ocean_cave", Cubiomes.OCEAN_CAVE_CARVER())
        .put("underwater_cave", Cubiomes.UNDERWATER_CAVE_CARVER())
        .put("nether_cave", Cubiomes.NETHER_CAVE_CARVER())
        .build();

    public static CaveCarverArgument caveCarver() {
        return new CaveCarverArgument();
    }

    public static int getCaveCarver(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, Integer.class);
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String caveCarverString = reader.readUnquotedString();
        Integer caveCarver = CAVE_CARVERS.get(caveCarverString);
        if (caveCarver == null) {
            reader.setCursor(cursor);
            throw CommandExceptions.UNKNOWN_CAVE_CARVER_EXCEPTION.create(caveCarverString);
        }
        return caveCarver;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(CAVE_CARVERS.keySet(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
