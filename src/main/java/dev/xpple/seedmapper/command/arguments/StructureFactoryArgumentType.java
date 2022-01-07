package dev.xpple.seedmapper.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.seedfinding.mcfeature.structure.Structure;
import dev.xpple.seedmapper.command.SharedHelpers;
import dev.xpple.seedmapper.util.features.FeatureFactory;
import dev.xpple.seedmapper.util.features.Features;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.command.CommandSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class StructureFactoryArgumentType implements ArgumentType<FeatureFactory<? extends Structure<?, ?>>>, SharedHelpers.Exceptions {

    private static final Collection<String> EXAMPLES = Arrays.asList("desert_temple", "bastion", "end_city");

    public static StructureFactoryArgumentType structureFactory() {
        return new StructureFactoryArgumentType();
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
            throw STRUCTURE_NOT_FOUND_EXCEPTION.create(structureString);
        }
        return factory;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(Features.STRUCTURE_REGISTRY.keySet(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
