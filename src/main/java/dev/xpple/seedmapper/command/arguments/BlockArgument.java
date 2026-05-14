package dev.xpple.seedmapper.command.arguments;

import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.OreConfig;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.xpple.seedmapper.command.CommandExceptions;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.util.Util;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BlockArgument implements ArgumentType<Integer> {

    private static final Collection<String> EXAMPLES = Arrays.asList("diamond_ore", "gold_ore", "nether_quartz_ore");

    public static final Map<String, Integer> BLOCKS = IntStream.range(0, Cubiomes.BLOCK_NUM())
        .boxed()
        .collect(Collectors.toUnmodifiableMap(block -> Cubiomes.block2str(block).getString(0), block -> block));

    private static final Map<String, Integer> ORE_BLOCKS = Util.make(() -> {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment oreConfig = OreConfig.allocate(arena);
            return OreArgument.ORES.values().stream()
                .<Integer>mapMulti((ore, consumer) -> {
                    // range over all versions to get all possible configs
                    IntStream.range(Cubiomes.MC_1_13(), Cubiomes.MC_NEWEST()).forEach(mc -> {
                        // use -1 for biome as it does not affect the ore block
                        if (Cubiomes.getOreConfig(ore, mc, -1, oreConfig) == 0) {
                            return;
                        }
                        consumer.accept(OreConfig.oreBlock(oreConfig));
                    });
                })
                .distinct()
                .collect(Collectors.toUnmodifiableMap(block -> Cubiomes.block2str(block).getString(0), block -> block));
        }
    });

    private final Map<String, Integer> blocks;

    private BlockArgument(boolean oreBlocks) {
        this.blocks = oreBlocks ? ORE_BLOCKS : BLOCKS;
    }

    public static BlockArgument block() {
        return new BlockArgument(false);
    }

    public static BlockArgument oreBlock() {
        return new BlockArgument(true);
    }

    public static Integer getOreBlock(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, Integer.class);
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String blockString = reader.readUnquotedString();
        Integer block = this.blocks.get(blockString);
        if (block == null) {
            reader.setCursor(cursor);
            throw CommandExceptions.UNKNOWN_BLOCK_EXCEPTION.create(blockString);
        }
        return block;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(this.blocks.keySet(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
