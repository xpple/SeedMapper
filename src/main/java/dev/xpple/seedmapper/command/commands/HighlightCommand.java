package dev.xpple.seedmapper.command.commands;

import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.Generator;
import com.github.cubiomes.OreConfig;
import com.github.cubiomes.OreVeinParameters;
import com.github.cubiomes.Pos3;
import com.github.cubiomes.Pos3List;
import com.github.cubiomes.SurfaceNoise;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import dev.xpple.seedmapper.command.CommandExceptions;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.config.Configs;
import dev.xpple.seedmapper.feature.OreTypes;
import dev.xpple.seedmapper.render.RenderManager;
import dev.xpple.seedmapper.util.ComponentUtils;
import dev.xpple.seedmapper.util.SpiralLoop;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static dev.xpple.seedmapper.command.arguments.BlockArgument.*;
import static dev.xpple.seedmapper.thread.LocatorThreadHelper.*;
import static dev.xpple.seedmapper.util.ChatBuilder.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class HighlightCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("sm:highlight")
            .then(literal("block")
                .then(argument("block", block())
                    .executes(ctx -> highlightBlock(CustomClientCommandSource.of(ctx.getSource()), getBlock(ctx, "block")))
                    .then(argument("chunks", integer(0, 20))
                        .executes(ctx -> submit(() -> highlightBlock(CustomClientCommandSource.of(ctx.getSource()), getBlock(ctx, "block"), getInteger(ctx, "chunks")))))))
            .then(literal("orevein")
                .executes(ctx -> submit(() -> highlightOreVein(CustomClientCommandSource.of(ctx.getSource()))))
                .then(argument("chunks", integer(0, 20))
                    .executes(ctx -> submit(() -> highlightOreVein(CustomClientCommandSource.of(ctx.getSource()), getInteger(ctx, "chunks")))))));
    }

    private static int highlightBlock(CustomClientCommandSource source, Pair<Integer, Integer> blockPair) throws CommandSyntaxException {
        return highlightBlock(source, blockPair, 0);
    }

    private static int highlightBlock(CustomClientCommandSource source, Pair<Integer, Integer> blockPair, int chunkRange) throws CommandSyntaxException {
        int version = source.getVersion();
        int dimension = source.getDimension();
        long seed = source.getSeed().getSecond();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment generator = Generator.allocate(arena);
            Cubiomes.setupGenerator(generator, version, 0);
            Cubiomes.applySeed(generator, dimension, seed);
            MemorySegment surfaceNoise = SurfaceNoise.allocate(arena);
            Cubiomes.initSurfaceNoise(surfaceNoise, dimension, seed);

            ChunkPos center = new ChunkPos(BlockPos.containing(source.getPosition()));

            int[] count = {0};
            SpiralLoop.spiral(center.x, center.z, chunkRange, (chunkX, chunkZ) -> {
                LevelChunk chunk = source.getWorld().getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
                boolean doAirCheck = Configs.OreAirCheck && chunk != null;
                Map<BlockPos, Integer> generatedOres = new HashMap<>();
                List<Integer> biomes;
                if (version <= Cubiomes.MC_1_17()) {
                    biomes = List.of(Cubiomes.getBiomeForOreGen(generator, chunkX, chunkZ, 0));
                } else {
                    // check certain Y-coordinates that matter for ore generation
                    // Minecraft checks _all_ biomes in a 3x3 square of chunks, which is not necessary
                    biomes = IntStream.of(-30, 64, 120)
                        .map(y -> Cubiomes.getBiomeForOreGen(generator, chunkX, chunkZ, y))
                        .boxed()
                        .toList();
                }
                OreTypes.ORE_TYPES.stream()
                    .filter(oreType -> biomes.stream().anyMatch(biome -> Cubiomes.isViableOreBiome(version, oreType, biome) != 0))
                    .<MemorySegment>mapMulti((oreType, consumer) -> {
                        MemorySegment oreConfig = OreConfig.allocate(arena);
                        // just do biomes.getFirst() because in 1.17 there is only one, and in 1.18 it does not matter
                        if (Cubiomes.getOreConfig(oreType, version, biomes.getFirst(), oreConfig) != 0) {
                            consumer.accept(oreConfig);
                        }
                    })
                    .sorted(Comparator.comparingInt(OreConfig::index))
                    .forEachOrdered(oreConfig -> {
                        int oreBlock = OreConfig.oreBlock(oreConfig);
                        int numReplaceBlocks = OreConfig.numReplaceBlocks(oreConfig);
                        MemorySegment replaceBlocks = OreConfig.replaceBlocks(oreConfig);
                        MemorySegment pos3List = Cubiomes.generateOres(arena, generator, surfaceNoise, oreConfig, chunkX, chunkZ);
                        int size = Pos3List.size(pos3List);
                        MemorySegment pos3s = Pos3List.pos3s(pos3List);
                        for (int i = 0; i < size; i++) {
                            MemorySegment pos3 = Pos3.asSlice(pos3s, i);
                            BlockPos pos = new BlockPos(Pos3.x(pos3), Pos3.y(pos3), Pos3.z(pos3));
                            if (doAirCheck && chunk.getBlockState(pos).isAir()) {
                                continue;
                            }
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

                int block = blockPair.getFirst();
                int colour = blockPair.getSecond();
                List<BlockPos> blockOres = generatedOres.entrySet().stream()
                    .filter(entry -> entry.getValue() == block)
                    .map(Map.Entry::getKey)
                    .toList();
                count[0] += blockOres.size();
                source.getClient().schedule(() -> {
                    RenderManager.drawBoxes(blockOres, colour);
                    source.sendFeedback(Component.translatable("command.highlight.block.chunkSuccess", accent(String.valueOf(blockOres.size())), ComponentUtils.formatXZ(chunkX, chunkZ)));
                });

                return false;
            });

            source.getClient().schedule(() -> source.sendFeedback(Component.translatable("command.highlight.block.success", accent(String.valueOf(count[0])))));
            return count[0];
        }
    }

    private static int highlightOreVein(CustomClientCommandSource source) throws CommandSyntaxException {
        return highlightOreVein(source, 0);
    }

    private static int highlightOreVein(CustomClientCommandSource source, int chunkRange) throws CommandSyntaxException {
        int version = source.getVersion();
        long seed = source.getSeed().getSecond();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment parameters = OreVeinParameters.allocate(arena);
            if (Cubiomes.initOreVeinNoise(parameters, seed, version) == 0) {
                throw CommandExceptions.ORE_VEIN_WRONG_VERSION_EXCEPTION.create();
            }

            ChunkPos center = new ChunkPos(BlockPos.containing(source.getPosition()));
            Map<BlockPos, Integer> blocks = new HashMap<>();
            SpiralLoop.spiral(center.x, center.z, chunkRange, (chunkX, chunkZ) -> {
                LevelChunk chunk = source.getWorld().getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
                boolean doAirCheck = Configs.OreAirCheck && chunk != null;
                int minX = chunkX << 4;
                int minZ = chunkZ << 4;

                for (int x = 0; x < LevelChunkSection.SECTION_WIDTH; x++) {
                    for (int z = 0; z < LevelChunkSection.SECTION_WIDTH; z++) {
                        for (int y = -60; y <= 50; y++) {
                            int block = Cubiomes.getOreVeinBlockAt(minX + x, y, minZ + z, parameters);
                            if (block == -1) {
                                continue;
                            }
                            BlockPos pos = new BlockPos(minX + x, y, minZ + z);
                            if (doAirCheck && chunk.getBlockState(pos).isAir()) {
                                continue;
                            }
                            blocks.put(pos, block);
                        }
                    }
                }
                return false;
            });

            int[] count = {0};
            blocks.entrySet().stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList())))
                .forEach((block, positions) -> {
                    if (block == Cubiomes.GRANITE() || block == Cubiomes.TUFF()) {
                        return;
                    }
                    count[0] += positions.size();
                    int colour = BLOCKS.values().stream().filter(pair -> Objects.equals(block, pair.getFirst())).findAny().orElseThrow().getSecond();
                    RenderManager.drawBoxes(positions, colour);
                    if (block == Cubiomes.RAW_COPPER_BLOCK() || block == Cubiomes.RAW_IRON_BLOCK()) {
                        source.getClient().schedule(() -> source.sendFeedback(Component.translatable("command.highlight.oreVein.rawBlocks", ComponentUtils.formatXYZCollection(positions))));
                    }
                });

            source.getClient().schedule(() -> source.sendFeedback(Component.translatable("command.highlight.oreVein.success", accent(String.valueOf(count[0])))));
            return count[0];
        }
    }
}
