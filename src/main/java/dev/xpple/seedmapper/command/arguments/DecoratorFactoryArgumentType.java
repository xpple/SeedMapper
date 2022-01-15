package dev.xpple.seedmapper.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.seedfinding.mcfeature.decorator.Decorator;
import dev.xpple.seedmapper.command.SharedHelpers;
import dev.xpple.seedmapper.util.features.FeatureFactory;
import dev.xpple.seedmapper.util.features.Features;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.command.CommandSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class DecoratorFactoryArgumentType implements ArgumentType<FeatureFactory<? extends Decorator<?, ?>>>, SharedHelpers.Exceptions {

    private static final Collection<String> EXAMPLES = Arrays.asList("desert_well", "end_gateway");

    public static DecoratorFactoryArgumentType decoratorFactory() {
        return new DecoratorFactoryArgumentType();
    }

    @SuppressWarnings("unchecked")
    public static FeatureFactory<? extends Decorator<?, ?>> getDecoratorFactory(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, FeatureFactory.class);
    }

    @Override
    public FeatureFactory<? extends Decorator<?, ?>> parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String decoratorString = reader.readUnquotedString();
        FeatureFactory<? extends Decorator<?, ?>> factory = Features.DECORATOR_REGISTRY.get(decoratorString);
        if (factory == null) {
            reader.setCursor(cursor);
            throw DECORATOR_NOT_FOUND_EXCEPTION.create(decoratorString);
        }
        return factory;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(Features.DECORATOR_REGISTRY.keySet(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
