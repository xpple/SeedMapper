package dev.xpple.seedmapper.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.seedfinding.mcfeature.structure.Structure;
import dev.xpple.seedmapper.command.CommandExceptions;
import dev.xpple.seedmapper.util.features.FeatureFactory;
import dev.xpple.seedmapper.util.features.Features;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class StructureFactoryArgument implements ArgumentType<FeatureFactory<? extends Structure<?, ?>>> {

    private static final Collection<String> EXAMPLES = Arrays.asList("desert_temple", "bastion", "end_city");

    public static StructureFactoryArgument structureFactory() {
        return new StructureFactoryArgument();
    }

    @SuppressWarnings("unchecked")
    public static FeatureFactory<? extends Structure<?, ?>> getStructureFactory(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, FeatureFactory.class);
    }

    @Override
    public FeatureFactory<? extends Structure<?, ?>> parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String structureString = reader.readUnquotedString();
        FeatureFactory<? extends Structure<?, ?>> factory = Features.STRUCTURE_REGISTRY.get(structureString);
        if (factory == null) {
            reader.setCursor(cursor);
            throw CommandExceptions.UNKNOWN_STRUCTURE_EXCEPTION.create(structureString);
        }
        return factory;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(Features.STRUCTURE_REGISTRY.keySet(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
