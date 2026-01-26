package dev.xpple.seedmapper.command.commands;

import com.github.cubiomes.CanyonCarverConfig;
import com.github.cubiomes.CaveCarverConfig;
import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.Generator;
import com.github.cubiomes.OreConfig;
import com.github.cubiomes.OreVeinParameters;
import com.github.cubiomes.Pos3;
import com.github.cubiomes.Pos3List;
import com.github.cubiomes.SurfaceNoise;
import com.github.cubiomes.TerrainNoise;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;

import dev.xpple.seedmapper.SeedMapper;
import dev.xpple.seedmapper.command.CommandExceptions;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.config.Configs;
import dev.xpple.seedmapper.feature.OreTypes;
import dev.xpple.seedmapper.render.RenderManager;
import dev.xpple.seedmapper.util.BaritoneIntegration;
import dev.xpple.seedmapper.util.ComponentUtils;
import dev.xpple.seedmapper.util.SeedIdentifier;
import dev.xpple.seedmapper.util.SpiralLoop;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SequenceLayout;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static dev.xpple.seedmapper.command.arguments.BlockArgument.*;
import static dev.xpple.seedmapper.command.arguments.CanyonCarverArgument.*;
import static dev.xpple.seedmapper.command.arguments.CaveCarverArgument.*;
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
                    .executes(ctx -> submit(() -> highlightOreVein(CustomClientCommandSource.of(ctx.getSource()), getInteger(ctx, "chunks"))))))
            .then(literal("terrain")
                .requires(_ -> Configs.DevMode)
                .executes(ctx -> highlightTerrain(CustomClientCommandSource.of(ctx.getSource())))
                .then(argument("chunks", integer(0, 5))
                    .executes(ctx -> submit(() -> highlightTerrain(CustomClientCommandSource.of(ctx.getSource()), getInteger(ctx, "chunks"))))))
            .then(literal("canyon")
                .requires(_ -> Configs.DevMode)
                .then(argument("canyon", canyonCarver())
                    .executes(ctx -> highlightCanyon(CustomClientCommandSource.of(ctx.getSource()), getCanyonCarver(ctx, "canyon")))
                    .then(argument("chunks", integer(0, 20))
                        .executes(ctx -> highlightCanyon(CustomClientCommandSource.of(ctx.getSource()), getCanyonCarver(ctx, "canyon"), getInteger(ctx, "chunks"))))))
            .then(literal("cave")
                .requires(_ -> Configs.DevMode)
                .then(argument("cave", caveCarver())
                    .executes(ctx -> highlightCave(CustomClientCommandSource.of(ctx.getSource()), getCaveCarver(ctx, "cave")))
                    .then(argument("chunks", integer(0, 20))
                        .executes(ctx -> submit(() -> highlightCave(CustomClientCommandSource.of(ctx.getSource()), getCaveCarver(ctx, "cave"), getInteger(ctx, "chunks"))))))));
    }

    private static int highlightBlock(CustomClientCommandSource source, Pair<Integer, Integer> blockPair) throws CommandSyntaxException {
        return highlightBlock(source, blockPair, 0);
    }

    private static int highlightBlock(CustomClientCommandSource source, Pair<Integer, Integer> blockPair, int chunkRange) throws CommandSyntaxException {
        SeedIdentifier seed = source.getSeed().getSecond();
        int version = source.getVersion();
        int dimension = source.getDimension();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment generator = Generator.allocate(arena);
            Cubiomes.setupGenerator(generator, version, source.getGeneratorFlags());
            Cubiomes.applySeed(generator, dimension, seed.seed());
            MemorySegment surfaceNoise = SurfaceNoise.allocate(arena);
            Cubiomes.initSurfaceNoise(surfaceNoise, dimension, seed.seed());

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
                        try {
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
                        } finally {
                            Cubiomes.freePos3List(pos3List);
                        }
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
                    if (SeedMapper.BARITONE_AVAILABLE && Configs.AutoMine) {
                        BaritoneIntegration.addGoals(blockOres);
                    }
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
        SeedIdentifier seed = source.getSeed().getSecond();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment parameters = OreVeinParameters.allocate(arena);
            if (Cubiomes.initOreVeinNoise(parameters, seed.seed(), version) == 0) {
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
                    if (SeedMapper.BARITONE_AVAILABLE && Configs.AutoMine) {
                        BaritoneIntegration.addGoals(positions);
                    }
                    if (block == Cubiomes.RAW_COPPER_BLOCK() || block == Cubiomes.RAW_IRON_BLOCK()) {
                        source.getClient().schedule(() -> source.sendFeedback(Component.translatable("command.highlight.oreVein.rawBlocks", ComponentUtils.formatXYZCollection(positions))));
                    }
                });

            source.getClient().schedule(() -> source.sendFeedback(Component.translatable("command.highlight.oreVein.success", accent(String.valueOf(count[0])))));
            return count[0];
        }
    }

    private static int highlightTerrain(CustomClientCommandSource source) throws CommandSyntaxException {
        return highlightTerrain(source, 0);
    }

    private static int highlightTerrain(CustomClientCommandSource source, int chunkRange) throws CommandSyntaxException {
        SeedIdentifier seed = source.getSeed().getSecond();
        int dimension = source.getDimension();
        if (dimension != Cubiomes.DIM_OVERWORLD()) {
            throw CommandExceptions.INVALID_DIMENSION_EXCEPTION.create();
        }
        int version = source.getVersion();
        int generatorFlags = source.getGeneratorFlags();

        ChunkPos center = new ChunkPos(BlockPos.containing(source.getPosition()));
        int minChunkX = center.x - chunkRange;
        int minChunkZ = center.z - chunkRange;
        int chunkW = chunkRange * 2 + 1;
        int chunkH = chunkRange * 2 + 1;
        int blockW = chunkW << 4;
        int blockH = chunkH << 4;
        int minX = minChunkX << 4;
        int minZ = minChunkZ << 4;

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment params = TerrainNoise.allocate(arena);
            if (Cubiomes.setupTerrainNoise(params, version, generatorFlags) == 0) {
                throw CommandExceptions.INCOMPATIBLE_PARAMETERS_EXCEPTION.create();
            }
            if (Cubiomes.initTerrainNoise(params, seed.seed(), dimension) == 0) {
                throw CommandExceptions.INCOMPATIBLE_PARAMETERS_EXCEPTION.create();
            }

            Set<BlockPos> blocks = new HashSet<>();
            SequenceLayout columnLayout = MemoryLayout.sequenceLayout(384, Cubiomes.C_INT);
            MemorySegment blockStates = arena.allocate(columnLayout, (long) blockW * blockH);
            Cubiomes.generateRegion(params, minChunkX, minChunkZ, chunkW, chunkH, blockStates, MemorySegment.NULL, 0);

            for (int relX = 0; relX < blockW; relX++) {
                int x = minX + relX;
                for (int relZ = 0; relZ < blockH; relZ++) {
                    int z = minZ + relZ;
                    int columnIdx = (relX * blockH + relZ) * 384;
                    for (int y = -64; y < 320; y++) {
                        int block = blockStates.getAtIndex(Cubiomes.C_INT, columnIdx + y + 64);
                        if (block == 1) {
                            blocks.add(new BlockPos(x, y, z));
                        }
                    }
                }
            }
            RenderManager.drawBoxes(blocks, 0xFF_FF0000);
            return blocks.size();
        }
    }

    private static int highlightCanyon(CustomClientCommandSource source, int canyonCarver) throws CommandSyntaxException {
        return highlightCanyon(source, canyonCarver, 0);
    }

    private static int highlightCanyon(CustomClientCommandSource source, int canyonCarver, int chunkRange) throws CommandSyntaxException {
        SeedIdentifier seed = source.getSeed().getSecond();
        int dimension = source.getDimension();
        int version = source.getVersion();

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment ccc = CanyonCarverConfig.allocate(arena);
            if (Cubiomes.getCanyonCarverConfig(canyonCarver, version, ccc) == 0) {
                throw CommandExceptions.CANYON_WRONG_VERSION_EXCEPTION.create();
            }
            if (CanyonCarverConfig.dim(ccc) != dimension) {
                throw CommandExceptions.INVALID_DIMENSION_EXCEPTION.create();
            }
            var biomeFunction = LocateCommand.getCarverBiomeFunction(arena, seed.seed(), dimension, version, source.getGeneratorFlags());
            return highlightCarver(source, chunkRange, (chunkX, chunkZ) -> {
                int biome = biomeFunction.applyAsInt(chunkX, chunkZ);
                if (Cubiomes.isViableCanyonBiome(canyonCarver, biome) == 0) {
                    return null;
                }
                return Cubiomes.carveCanyon(arena, seed.seed(), chunkX, chunkZ, ccc);
            });
        }
    }

    private static int highlightCave(CustomClientCommandSource source, int caveCarver) throws CommandSyntaxException {
        return highlightCave(source, caveCarver, 0);
    }

    private static int highlightCave(CustomClientCommandSource source, int caveCarver, int chunkRange) throws CommandSyntaxException {
        SeedIdentifier seed = source.getSeed().getSecond();
        int dimension = source.getDimension();
        int version = source.getVersion();

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment ccc = CaveCarverConfig.allocate(arena);
            if (Cubiomes.getCaveCarverConfig(caveCarver, version, -1, ccc) == 0) {
                throw CommandExceptions.CAVE_WRONG_VERSION_EXCEPTION.create();
            }
            if (CaveCarverConfig.dim(ccc) != dimension) {
                throw CommandExceptions.INVALID_DIMENSION_EXCEPTION.create();
            }
            var biomeFunction = LocateCommand.getCarverBiomeFunction(arena, seed.seed(), dimension, version, source.getGeneratorFlags());
            return highlightCarver(source, chunkRange, (chunkX, chunkZ) -> {
                int biome = biomeFunction.applyAsInt(chunkX, chunkZ);
                if (Cubiomes.isViableCaveBiome(caveCarver, biome) == 0) {
                    return null;
                }
                return Cubiomes.carveCave(arena, seed.seed(), chunkX, chunkZ, ccc);
            });
        }
    }

    private static int highlightCarver(CustomClientCommandSource source, int chunkRange, BiFunction<Integer, Integer, @Nullable MemorySegment> carverFunction) {
        ChunkPos center = new ChunkPos(BlockPos.containing(source.getPosition()));
        Set<BlockPos> blocks = new HashSet<>();
        SpiralLoop.spiral(center.x, center.z, chunkRange, (chunkX, chunkZ) -> {
            MemorySegment pos3List = carverFunction.apply(chunkX, chunkZ);
            if (pos3List == null) {
                return false;
            }
            int size = Pos3List.size(pos3List);
            MemorySegment pos3s = Pos3List.pos3s(pos3List);
            for (int i = 0; i < size; i++) {
                MemorySegment pos3 = Pos3.asSlice(pos3s, i);
                blocks.add(new BlockPos(Pos3.x(pos3), Pos3.y(pos3), Pos3.z(pos3)));
            }

            return false;
        });
        RenderManager.drawBoxes(blocks, 0xFF_FF0000);
        source.getClient().schedule(() -> source.sendFeedback(Component.translatable("command.highlight.carver.success", accent(String.valueOf(blocks.size())))));
        return blocks.size();
    }
}
