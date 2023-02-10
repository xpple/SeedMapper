package dev.xpple.seedmapper.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.xpple.seedmapper.command.SharedHelpers;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class BlockArgumentType implements ArgumentType<Block>, SharedHelpers.Exceptions {

    private BlockArgumentType() {
    }

    private static final Collection<String> EXAMPLES = Arrays.asList("stone", "command_block", "minecraft:emerald_ore");

    public static BlockArgumentType block() {
        return new BlockArgumentType();
    }

    public static Block getBlock(CommandContext<? extends CommandSource> context, String name) {
        return context.getArgument(name, Block.class);
    }

    @Override
    public Block parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        Identifier identifier = Identifier.fromCommandInput(reader);
        if (!Registries.BLOCK.containsId(identifier)) {
            reader.setCursor(cursor);
            throw BLOCK_NOT_FOUND_EXCEPTION.createWithContext(reader, identifier);
        }
        return Registries.BLOCK.get(identifier);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestIdentifiers(Registries.BLOCK.getIds(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
