package dev.xpple.seedmapper.command.arguments;

import com.github.cubiomes.Cubiomes;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.xpple.seedmapper.command.CommandExceptions;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OreArgument implements ArgumentType<Integer> {

    private static final Collection<String> EXAMPLES = List.of("ore_quartz_deltas", "ore_redstone_lower", "ore_tuff");

    public static final Map<String, Integer> ORES = IntStream.range(0, Cubiomes.ORE_NUM())
        .boxed()
        .collect(Collectors.toUnmodifiableMap(ore -> Cubiomes.ore2str(ore).getString(0), ore -> ore));

    public static OreArgument ore() {
        return new OreArgument();
    }

    public static Integer getOre(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, Integer.class);
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String oreString = reader.readUnquotedString();
        Integer ore = ORES.get(oreString);
        if (ore == null) {
            reader.setCursor(cursor);
            throw CommandExceptions.UNKNOWN_ORE_EXCEPTION.create(oreString);
        }
        return ore;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(ORES.keySet(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
