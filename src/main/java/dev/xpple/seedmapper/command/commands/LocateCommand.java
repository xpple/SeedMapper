package dev.xpple.seedmapper.command.commands;

import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.Generator;
import com.github.cubiomes.OreVeinParameters;
import com.github.cubiomes.Piece;
import com.github.cubiomes.Pos;
import com.github.cubiomes.Pos3;
import com.github.cubiomes.StrongholdIter;
import com.github.cubiomes.StructureConfig;
import com.github.cubiomes.StructureVariant;
import com.github.cubiomes.SurfaceNoise;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.seedmapper.command.CommandExceptions;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.feature.StructureChecks;
import dev.xpple.seedmapper.feature.StructureVariantFeedbackHelper;
import dev.xpple.seedmapper.util.ComponentUtils;
import dev.xpple.seedmapper.util.SpiralLoop;
import dev.xpple.seedmapper.util.TwoDTree;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.WorldgenRandom;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.List;
import java.util.stream.IntStream;

import static com.mojang.brigadier.arguments.BoolArgumentType.*;
import static dev.xpple.seedmapper.command.arguments.BiomeArgument.*;
import static dev.xpple.seedmapper.command.arguments.StructurePredicateArgument.*;
import static dev.xpple.seedmapper.thread.ThreadingHelper.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class LocateCommand {

    private static final int BIOME_SEARCH_RADIUS = 6400;

    private static final Long2ObjectMap<TwoDTree> cachedStrongholds = new Long2ObjectOpenHashMap<>();

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("sm:locate")
            .then(literal("biome")
                .then(argument("biome", biome())
                    .executes(ctx -> submit(() -> locateBiome(CustomClientCommandSource.of(ctx.getSource()), getBiome(ctx, "biome"))))))
            .then(literal("feature")
                .then(literal("structure")
                    .then(argument("structure", structurePredicate())
                        .executes(ctx -> submit(() -> locateStructure(CustomClientCommandSource.of(ctx.getSource()), getStructurePredicate(ctx, "structure"))))
                        .then(argument("variantdata", bool())
                            .executes(ctx -> submit(() -> locateStructure(CustomClientCommandSource.of(ctx.getSource()), getStructurePredicate(ctx, "structure"), getBool(ctx, "variantdata")))))))
                .then(literal("stronghold")
                    .executes(ctx -> submit(() -> locateStronghold(CustomClientCommandSource.of(ctx.getSource())))))
                .then(literal("slimechunk")
                    .executes(ctx -> submit(() -> locateSlimeChunk(CustomClientCommandSource.of(ctx.getSource()))))))
            .then(literal("spawn")
                .executes(ctx -> submit(() -> locateSpawn(CustomClientCommandSource.of(ctx.getSource())))))
            .then(literal("orevein")
                .executes(ctx -> submit(() -> locateOreVein(CustomClientCommandSource.of(ctx.getSource()))))));
    }

    private static int locateBiome(CustomClientCommandSource source, int biome) throws CommandSyntaxException {
        int dimension = source.getDimension();
        if (Cubiomes.getDimension(biome) != dimension) {
            throw CommandExceptions.INVALID_DIMENSION_EXCEPTION.create();
        }
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment generator = Generator.allocate(arena);
            Cubiomes.setupGenerator(generator, source.getVersion(), 0);
            Cubiomes.applySeed(generator, dimension, source.getSeed().getSecond());

            BlockPos center = BlockPos.containing(source.getPosition());

            SpiralLoop.Coordinate pos = SpiralLoop.spiral(center.getX(), center.getZ(), BIOME_SEARCH_RADIUS, 32, (x, z) -> {
                return Cubiomes.getBiomeAt(generator, 1, x, 63, z) == biome;
            });
            if (pos == null) {
                throw CommandExceptions.NO_BIOME_FOUND_EXCEPTION.create(BIOME_SEARCH_RADIUS);
            }

            source.sendFeedback(Component.translatable("command.locate.biome.foundAt", ComponentUtils.formatXZ(pos.x(), pos.z())));
            return Command.SINGLE_SUCCESS;
        }
    }

    private static int locateStructure(CustomClientCommandSource source, StructureAndPredicate structureAndPredicate) throws CommandSyntaxException {
        return locateStructure(source, structureAndPredicate, false);
    }

    private static int locateStructure(CustomClientCommandSource source, StructureAndPredicate structureAndPredicate, boolean variantData) throws CommandSyntaxException {
        try (Arena arena = Arena.ofConfined()) {
            int version = source.getVersion();
            int dimension = source.getDimension();
            int structure = structureAndPredicate.structure();
            MemorySegment structureConfig = StructureConfig.allocate(arena);
            int config = Cubiomes.getStructureConfig(structure, version, structureConfig);
            if (config == 0) {
                throw CommandExceptions.INCOMPATIBLE_PARAMETERS_EXCEPTION.create();
            }
            if (StructureConfig.dim(structureConfig) != dimension) {
                throw CommandExceptions.INVALID_DIMENSION_EXCEPTION.create();
            }
            long seed = source.getSeed().getSecond();

            MemorySegment generator = Generator.allocate(arena);
            Cubiomes.setupGenerator(generator, version, 0);
            Cubiomes.applySeed(generator, dimension, seed);

            // currently only used for end cities
            MemorySegment surfaceNoise = SurfaceNoise.allocate(arena);
            Cubiomes.initSurfaceNoise(surfaceNoise, dimension, seed);

            // check if the structure will generate at a position
            StructureChecks.GenerationCheck generationCheck = StructureChecks.getGenerationCheck(structure);
            // check if the structure at that location also matches our requirements
            PiecesPredicate piecesPredicate = structureAndPredicate.piecesPredicate();
            StructureChecks.PiecesPredicateCheck piecesPredicateCheck = StructureChecks.getPiecesPredicateCheck(structure);
            VariantPredicate variantPredicate = structureAndPredicate.variantPredicate();
            StructureChecks.VariantPredicateCheck variantPredicateCheck = StructureChecks.getVariantPredicateCheck(structure);

            BlockPos center = BlockPos.containing(source.getPosition());
            int regionSize = StructureConfig.regionSize(structureConfig) << 4;
            MemorySegment structurePos = Pos.allocate(arena);
            MemorySegment pieces = Piece.allocateArray(StructureChecks.MAX_END_CITY_AND_FORTRESS_PIECES, arena);
            MemorySegment structureVariant = StructureVariant.allocate(arena);
            SpiralLoop.Coordinate pos = SpiralLoop.spiral(center.getX() / regionSize, center.getZ() / regionSize, Level.MAX_LEVEL_SIZE / regionSize, (x, z) -> {
                if (!generationCheck.check(generator, surfaceNoise, x, z, structurePos)) {
                    return false;
                }
                if (!piecesPredicateCheck.check(piecesPredicate, pieces, generator, structurePos)) {
                    return false;
                }
                if (!variantPredicateCheck.check(variantPredicate, structureVariant, generator, structurePos)) {
                    return false;
                }
                return true;
            });
            if (pos == null) {
                throw CommandExceptions.NO_STRUCTURE_FOUND_EXCEPTION.create(Level.MAX_LEVEL_SIZE);
            }

            source.sendFeedback(Component.translatable("command.locate.feature.structure.foundAt", ComponentUtils.formatXZ(Pos.x(structurePos), Pos.z(structurePos))));

            if (structure == Cubiomes.End_City()) {
                int numPieces = Cubiomes.getEndCityPieces(pieces, seed, Pos.x(structurePos) >> 4, Pos.z(structurePos) >> 4);
                IntStream.range(0, numPieces)
                    .mapToObj(i -> Piece.asSlice(pieces, i))
                    .filter(piece -> Piece.type(piece) == Cubiomes.END_SHIP())
                    .findAny() // only one ship per end city
                    .map(Piece::pos)
                    .map(city -> new BlockPos(Pos3.x(city), Pos3.y(city) + 60, Pos3.z(city)))
                    .ifPresent(city -> source.sendFeedback(Component.literal(" - ")
                        .append(Component.translatable("command.locate.feature.structure.endCity.hasShip", ComponentUtils.formatXYZ(city.getX(), city.getY(), city.getZ())))));
            } else if (structure == Cubiomes.Fortress()) {
                int numPieces = Cubiomes.getFortressPieces(pieces, 400, version, seed, Pos.x(structurePos) >> 4, Pos.z(structurePos) >> 4);
                IntStream.range(0, numPieces)
                    .mapToObj(i -> Piece.asSlice(pieces, i))
                    .filter(piece -> Piece.type(piece) == Cubiomes.BRIDGE_SPAWNER())
                    .map(Piece::pos)
                    .map(monsterThrone -> new BlockPos(Pos3.x(monsterThrone), Pos3.y(monsterThrone) + 10, Pos3.z(monsterThrone)))
                    .forEach(spawnerPos -> source.sendFeedback(Component.literal(" - ")
                        .append(Component.translatable("command.locate.feature.structure.fortress.hasSpawner", ComponentUtils.formatXYZ(spawnerPos.getX(), spawnerPos.getY(), spawnerPos.getZ())))));
            }

            if (!variantData) {
                return Command.SINGLE_SUCCESS;
            }
            int biome = Cubiomes.getBiomeAt(generator, 4, Pos.x(structurePos) >> 2, 320 >> 2, Pos.z(structurePos) >> 2);
            Cubiomes.getVariant(structureVariant, structure, version, seed, Pos.x(structurePos), Pos.z(structurePos), biome);

            List<Component> components = StructureVariantFeedbackHelper.get(structure, structureVariant);
            if (components.isEmpty()) {
                return Command.SINGLE_SUCCESS;
            }
            source.sendFeedback(Component.translatable("command.locate.feature.structure.variantData"));
            components.forEach(component -> source.sendFeedback(Component.literal(" - ").append(component)));
            return Command.SINGLE_SUCCESS;
        }
    }

    private static int locateStronghold(CustomClientCommandSource source) throws CommandSyntaxException {
        int dimension = source.getDimension();
        if (dimension != Cubiomes.DIM_OVERWORLD()) {
            throw CommandExceptions.INVALID_DIMENSION_EXCEPTION.create();
        }
        int version = source.getVersion();
        long seed = source.getSeed().getSecond();

        BlockPos position = BlockPos.containing(source.getPosition());

        TwoDTree tree = cachedStrongholds.computeIfAbsent(seed, _ -> new TwoDTree());
        if (tree.isEmpty()) {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment strongholdIter = StrongholdIter.allocate(arena);
                Cubiomes.initFirstStronghold(arena, strongholdIter, version, seed);
                MemorySegment generator = Generator.allocate(arena);
                Cubiomes.setupGenerator(generator, version, 0);
                Cubiomes.applySeed(generator, dimension, seed);

                final int count = source.getVersion() <= Cubiomes.MC_1_8() ? 3 : 128;
                for (int i = 0; i < count; i++) {
                    if (Cubiomes.nextStronghold(strongholdIter, generator) == 0) {
                        break;
                    }
                    MemorySegment pos = StrongholdIter.pos(strongholdIter);
                    tree.insert(new BlockPos(Pos.x(pos), 0, Pos.z(pos)));
                }
            }
        }

        BlockPos pos = tree.nearestTo(position.atY(0));

        source.sendFeedback(Component.translatable("command.locate.feature.stronghold.success", ComponentUtils.formatXZ(pos.getX(), pos.getZ())));
        return Command.SINGLE_SUCCESS;
    }

    private static int locateSlimeChunk(CustomClientCommandSource source) throws CommandSyntaxException {
        if (source.getDimension() != Cubiomes.DIM_OVERWORLD()) {
            throw CommandExceptions.INVALID_DIMENSION_EXCEPTION.create();
        }
        long seed = source.getSeed().getSecond();
        ChunkPos center = new ChunkPos(BlockPos.containing(source.getPosition()));
        SpiralLoop.Coordinate pos = SpiralLoop.spiral(center.x, center.z, 6400, (x, z) -> {
            RandomSource random = WorldgenRandom.seedSlimeChunk(x, z, seed, 987234911L);
            return random.nextInt(10) == 0;
        });
        assert pos != null;

        int blockPosX = (pos.x() << 4) + 9;
        int blockPosZ = (pos.z() << 4) + 9;
        source.sendFeedback(Component.translatable("command.locate.feature.slimeChunk.foundAt",
            ComponentUtils.formatXZ(blockPosX, blockPosZ, Component.translatable("command.locate.feature.slimeChunk.copy")),
            ComponentUtils.formatXZ(pos.x(), pos.z(), Component.translatable("command.locate.feature.slimeChunk.copyChunk"))
        ));
        return Command.SINGLE_SUCCESS;
    }

    private static int locateSpawn(CustomClientCommandSource source) throws CommandSyntaxException {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment generator = Generator.allocate(arena);
            Cubiomes.setupGenerator(generator, source.getVersion(), 0);
            Cubiomes.applySeed(generator, source.getDimension(), source.getSeed().getSecond());
            MemorySegment pos = Cubiomes.getSpawn(arena, generator);

            source.sendFeedback(Component.translatable("command.locate.spawn.success", ComponentUtils.formatXZ(Pos.x(pos), Pos.z(pos))));
            return Command.SINGLE_SUCCESS;
        }
    }

    private static int locateOreVein(CustomClientCommandSource source) throws CommandSyntaxException {
        int version = source.getVersion();
        long seed = source.getSeed().getSecond();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment parameters = OreVeinParameters.allocate(arena);
            Cubiomes.initOreVeinNoise(parameters, seed, version);
            ChunkPos center = new ChunkPos(BlockPos.containing(source.getPosition()));
            BlockPos[] pos = {null};
            SpiralLoop.spiral(center.x, center.z, 6400, (chunkX, chunkZ) -> {
                int minX = chunkX << 4;
                int minZ = chunkZ << 4;

                for (int x = 0; x < LevelChunkSection.SECTION_WIDTH; x++) {
                    for (int z = 0; z < LevelChunkSection.SECTION_WIDTH; z++) {
                        for (int y = -60; y <= 50; y++) {
                            int block = Cubiomes.getOreVeinBlockAt(minX + x, y, minZ + z, parameters);
                            if (block == -1) {
                                continue;
                            }
                            pos[0] = new BlockPos(minX + x, y, minZ + z);
                            return true;
                        }
                    }
                }
                return false;
            });
            if (pos[0] == null) {
                throw CommandExceptions.NO_ORE_VEIN_FOUND_EXCEPTION.create(6400);
            }

            source.sendFeedback(Component.translatable("command.locate.oreVein.foundAt", ComponentUtils.formatXYZ(pos[0].getX(), pos[0].getY(), pos[0].getZ(), Component.translatable("command.locate.oreVein.copy"))));

            return Command.SINGLE_SUCCESS;
        }
    }
}
