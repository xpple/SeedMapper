package dev.xpple.seedmapper.command.commands;

import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.Generator;
import com.github.cubiomes.OreConfig;
import com.github.cubiomes.Pos3;
import com.github.cubiomes.Pos3List;
import com.github.cubiomes.SurfaceNoise;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.feature.OreTypes;
import dev.xpple.seedmapper.render.RenderManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static dev.xpple.seedmapper.command.arguments.BlockArgument.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class HighlightCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("sm:highlight")
            .then(literal("block")
                .then(argument("block", block())
                    .executes(ctx -> highlightBlock(CustomClientCommandSource.of(ctx.getSource()), getBlock(ctx, "block"))))));
    }

    private static int highlightBlock(CustomClientCommandSource source, Pair<Integer, Integer> blockPair) throws CommandSyntaxException {
        int version = source.getVersion();
        int dimension = source.getDimension();
        long seed = source.getSeed().getSecond();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment generator = Generator.allocate(arena);
            Cubiomes.setupGenerator(generator, version, 0);
            Cubiomes.applySeed(generator, dimension, seed);
            MemorySegment surfaceNoise = SurfaceNoise.allocate(arena);
            Cubiomes.initSurfaceNoise(surfaceNoise, dimension, seed);

            ChunkPos chunk = new ChunkPos(BlockPos.containing(source.getPosition()));
            int biome = Cubiomes.getBiomeForOreGen(generator, chunk.x, chunk.z);

            // TODO: multiple chunks
            Map<BlockPos, Integer> generatedOres = new HashMap<>();
            OreTypes.ORE_TYPES.stream()
                .<MemorySegment>mapMulti((oreType, consumer) -> {
                    MemorySegment oreConfig = OreConfig.allocate(arena);
                    if (Cubiomes.getOreConfig(oreType, version, biome, oreConfig) != 0) {
                        consumer.accept(oreConfig);
                    }
                })
                .filter(oreConfig -> Cubiomes.isViableOreBiome(version, OreConfig.oreType(oreConfig), biome) != 0)
                .sorted(Comparator.comparingInt(OreConfig::index))
                .forEachOrdered(oreConfig -> {
                    int oreBlock = OreConfig.oreBlock(oreConfig);
                    int numReplaceBlocks = OreConfig.numReplaceBlocks(oreConfig);
                    MemorySegment replaceBlocks = OreConfig.replaceBlocks(oreConfig);
                    MemorySegment pos3List = Cubiomes.generateOres(arena, generator, surfaceNoise, oreConfig, chunk.x, chunk.z);
                    int size = Pos3List.size(pos3List);
                    MemorySegment pos3s = Pos3List.pos3s(pos3List);
                    for (int i = 0; i < size; i++) {
                        MemorySegment pos3 = Pos3.asSlice(pos3s, i);
                        BlockPos pos = new BlockPos(Pos3.x(pos3), Pos3.y(pos3), Pos3.z(pos3));
                        Integer previouslyGeneratedOre = generatedOres.get(pos);
                        if (previouslyGeneratedOre != null) {
                            boolean contains = false;
                            for (int j = 0; j < numReplaceBlocks; j++) {
                                int replaceBlock = replaceBlocks.getAtIndex(Cubiomes.C_INT, j);
                                if (replaceBlock == previouslyGeneratedOre) {
                                    contains = true;
                                    break;
                                }
                            }
                            if (!contains) {
                                continue;
                            }
                        }
                        generatedOres.put(pos, oreBlock);
                    }
                    Cubiomes.freePos3List(pos3List);
                });
            int[] count = {0};
            int block = blockPair.getFirst();
            int colour = blockPair.getSecond();
            generatedOres.forEach((pos, oreBlock) -> {
                if (oreBlock == block) {
                    RenderManager.drawBox(pos, colour);
                    count[0]++;
                }
            });

            source.sendFeedback(Component.translatable("command.highlight.success", count[0]));
            return count[0];
        }
    }
}
