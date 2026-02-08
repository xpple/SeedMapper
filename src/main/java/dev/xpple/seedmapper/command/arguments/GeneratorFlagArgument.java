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

public class GeneratorFlagArgument implements ArgumentType<Integer> {

    private static final Collection<String> EXAMPLES = Arrays.asList("overworld", "the_nether", "the_end");

    public static final Map<String, Integer> GENERATOR_FLAGS = ImmutableMap.<String, Integer>builder()
        .put("large_biomes", Cubiomes.LARGE_BIOMES())
        .put("no_beta_ocean", Cubiomes.NO_BETA_OCEAN())
        .put("force_ocean_variants", Cubiomes.FORCE_OCEAN_VARIANTS())
        .build();

    public static GeneratorFlagArgument generatorFlag() {
        return new GeneratorFlagArgument();
    }

    public static int getGeneratorFlag(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, Integer.class);
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String generatorFlagString = reader.readUnquotedString();
        Integer generatorFlag = GENERATOR_FLAGS.get(generatorFlagString);
        if (generatorFlag == null) {
            reader.setCursor(cursor);
            throw CommandExceptions.UNKNOWN_GENERATOR_FLAG_EXCEPTION.create(generatorFlagString);
        }
        return generatorFlag;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(GENERATOR_FLAGS.keySet(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
