package dev.xpple.seedmapper.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class BlockArgumentType implements ArgumentType<Block> {

    private static final Collection<String> EXAMPLES = Arrays.asList("minecraft:stone", "stone");

    public BlockArgumentType() {
    }

    public static BlockArgumentType block() {
        return new BlockArgumentType();
    }

    public static Block getBlock(CommandContext<ServerCommandSource> context, String name) {
        return context.getArgument(name, Block.class);
    }

    @Override
    public Block parse(StringReader stringReader) throws CommandSyntaxException {
        BlockArgumentParser blockArgumentParser = (new BlockArgumentParser(stringReader, false)).parse(false);
        BlockState parsedBlockState = blockArgumentParser.getBlockState();
        if (parsedBlockState == null) {
            if (Registry.BLOCK.getOrEmpty(blockArgumentParser.getTagId()).isPresent()) {
                return Registry.BLOCK.getOrEmpty(blockArgumentParser.getTagId()).get();
            } else {
                throw BlockArgumentParser.INVALID_BLOCK_ID_EXCEPTION.create(blockArgumentParser.getTagId().toString());
            }
        } else {
            return parsedBlockState.getBlock();
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        StringReader stringReader = new StringReader(builder.getInput());
        stringReader.setCursor(builder.getStart());
        BlockArgumentParser blockArgumentParser = new BlockArgumentParser(stringReader, false);

        try {
            blockArgumentParser.parse(false);
        } catch (CommandSyntaxException ignored) {
        }

        return blockArgumentParser.getSuggestions(builder, BlockTags.getTagGroup());
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
