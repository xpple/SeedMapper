package dev.xpple.seedmapper.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.xpple.seedmapper.command.CommandExceptions;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class BlockArgument implements ArgumentType<Block> {

    private BlockArgument() {
    }

    private static final Collection<String> EXAMPLES = Arrays.asList("stone", "command_block", "minecraft:emerald_ore");

    public static BlockArgument block() {
        return new BlockArgument();
    }

    public static Block getBlock(CommandContext<? extends SharedSuggestionProvider> context, String name) {
        return context.getArgument(name, Block.class);
    }

    @Override
    public Block parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        ResourceLocation resourceLocation = ResourceLocation.read(reader);
        if (!BuiltInRegistries.BLOCK.containsKey(resourceLocation)) {
            reader.setCursor(cursor);
            throw CommandExceptions.UNKNOWN_BLOCK_EXCEPTION.createWithContext(reader, resourceLocation);
        }
        return BuiltInRegistries.BLOCK.getValue(resourceLocation);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(BuiltInRegistries.BLOCK.keySet(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
