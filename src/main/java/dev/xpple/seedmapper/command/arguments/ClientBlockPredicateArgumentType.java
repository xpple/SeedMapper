package dev.xpple.seedmapper.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class ClientBlockPredicateArgumentType implements ArgumentType<ClientBlockPredicateArgumentType.ParseResult> {
    private final RegistryWrapper<Block> registryWrapper;
    private boolean allowNbt = true;
    private boolean allowTags = true;

    private ClientBlockPredicateArgumentType(CommandRegistryAccess registryAccess) {
        this.registryWrapper = registryAccess.createWrapper(RegistryKeys.BLOCK);
    }

    public static ClientBlockPredicateArgumentType blockPredicate(CommandRegistryAccess registryAccess) {
        return new ClientBlockPredicateArgumentType(registryAccess);
    }

    public ClientBlockPredicateArgumentType disallowNbt() {
        this.allowNbt = false;
        return this;
    }

    public ClientBlockPredicateArgumentType disallowTags() {
        this.allowTags = false;
        return this;
    }

    @Override
    public ParseResult parse(StringReader stringReader) throws CommandSyntaxException {
        var result = BlockArgumentParser.blockOrTag(this.registryWrapper, stringReader, this.allowNbt);
        return new ParseResult(result, this.registryWrapper);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return BlockArgumentParser.getSuggestions(this.registryWrapper, builder, this.allowTags, this.allowNbt);
    }

    public static Either<BlockArgumentParser.BlockResult, BlockArgumentParser.TagResult> getParseResult(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, ParseResult.class).result;
    }

    public static ClientBlockPredicate getBlockPredicate(Either<BlockArgumentParser.BlockResult, BlockArgumentParser.TagResult> result) {
        ClientBlockPredicate predicate = (blockView, pos) -> createPredicate(result).test(blockView.getBlockState(pos));
        NbtCompound nbtData = result.map(BlockArgumentParser.BlockResult::nbt, BlockArgumentParser.TagResult::nbt);
        if (nbtData == null) {
            return predicate;
        }

        return (blockView, pos) -> {
            if (!predicate.test(blockView, pos)) {
                return false;
            }
            BlockEntity be = blockView.getBlockEntity(pos);
            return be != null && NbtHelper.matches(nbtData, be.createNbt(), true);
        };
    }

    private static Predicate<BlockState> createPredicate(Either<BlockArgumentParser.BlockResult, BlockArgumentParser.TagResult> result) {
        return result.map(
            blockResult -> state -> {
                if (!state.isOf(blockResult.blockState().getBlock())) {
                    return false;
                }
                Map<Property<?>, Comparable<?>> properties = blockResult.properties();
                for (Map.Entry<Property<?>, Comparable<?>> entry : properties.entrySet()) {
                    if (state.get(entry.getKey()) != entry.getValue()) {
                        return false;
                    }
                }
                return true;
            },
            tagResult -> state -> {
                if (!state.isIn(tagResult.tag())) {
                    return false;
                }
                Map<String, String> properties = tagResult.vagueProperties();
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    Property<?> prop = state.getBlock().getStateManager().getProperty(entry.getKey());
                    if (prop == null) {
                        return false;
                    }
                    Comparable<?> expectedValue = prop.parse(entry.getValue()).orElse(null);
                    if (expectedValue == null) {
                        return false;
                    }
                    if (state.get(prop) != expectedValue) {
                        return false;
                    }
                }
                return true;
            }
        );
    }

    public record ParseResult(Either<BlockArgumentParser.BlockResult, BlockArgumentParser.TagResult> result, RegistryWrapper<Block> registryWrapper) {
    }

    @FunctionalInterface
    public interface ClientBlockPredicate {
        boolean test(BlockView blockView, BlockPos pos);
    }
}
