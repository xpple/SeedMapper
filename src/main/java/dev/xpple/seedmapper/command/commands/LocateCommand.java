package dev.xpple.seedmapper.command.commands;

import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.Generator;
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
import dev.xpple.seedmapper.util.CheckedSupplier;
import dev.xpple.seedmapper.util.SpiralLoop;
import dev.xpple.seedmapper.util.TwoDTree;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.WorldgenRandom;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static com.mojang.brigadier.arguments.BoolArgumentType.*;
import static dev.xpple.seedmapper.command.arguments.BiomeArgument.*;
import static dev.xpple.seedmapper.command.arguments.StructurePredicateArgument.*;
import static dev.xpple.seedmapper.util.ChatBuilder.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class LocateCommand {

    private static final int BIOME_SEARCH_RADIUS = 6400;

    private static final Long2ObjectMap<TwoDTree> cachedStrongholds = new Long2ObjectOpenHashMap<>();

    private static final ExecutorService locatingExecutor = Executors.newCachedThreadPool();
    private static Future<Integer> currentTask = null;

    public static final Component STOP_TASK_COMPONENT = run(hover(format(Component.translatable("commands.exceptions.alreadyBusyLocating.stopTask"), ChatFormatting.UNDERLINE), base(Component.translatable("commands.exceptions.alreadyBusyLocating.clickToStop"))), () -> {
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
            Minecraft.getInstance().player.displayClientMessage(Component.translatable("command.locate.taskStopped"), false);
        } else {
            Minecraft.getInstance().player.displayClientMessage(format(Component.translatable("command.locate.noTaskRunning"), ChatFormatting.RED), false);
        }
    });

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(locatingExecutor::shutdownNow));
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("sm:locate")
            .then(literal("biome")
                .then(argument("biome", biome())
                    .executes(ctx -> schedule(() -> locateBiome(CustomClientCommandSource.of(ctx.getSource()), getBiome(ctx, "biome"))))))
            .then(literal("feature")
                .then(literal("structure")
                    .then(argument("structure", structurePredicate())
                        .executes(ctx -> schedule(() -> locateStructure(CustomClientCommandSource.of(ctx.getSource()), getStructurePredicate(ctx, "structure"))))
                        .then(argument("variantdata", bool())
                            .executes(ctx -> schedule(() -> locateStructure(CustomClientCommandSource.of(ctx.getSource()), getStructurePredicate(ctx, "structure"), getBool(ctx, "variantdata")))))))
                .then(literal("stronghold")
                    .executes(ctx -> schedule(() -> locateStronghold(CustomClientCommandSource.of(ctx.getSource())))))
                .then(literal("slimechunk")
                    .executes(ctx -> schedule(() -> locateSlimeChunk(CustomClientCommandSource.of(ctx.getSource()))))))
            .then(literal("spawn")
                .executes(ctx -> schedule(() -> locateSpawn(CustomClientCommandSource.of(ctx.getSource()))))));
    }

    private static int schedule(CheckedSupplier<Integer, CommandSyntaxException> task) throws CommandSyntaxException {
        if (currentTask != null && !currentTask.isDone()) {
            throw CommandExceptions.ALREADY_BUSY_LOCATING_EXCEPTION.create();
        }
        currentTask = locatingExecutor.submit(() -> {
            try {
                return task.get();
            } catch (CommandSyntaxException e) {
                Player player = Minecraft.getInstance().player;
                if (player != null) {
                    Minecraft.getInstance().schedule(() -> player.displayClientMessage(format((MutableComponent) e.getRawMessage(), ChatFormatting.RED), false));
                }
                return 0;
            }
        });
        Minecraft.getInstance().player.displayClientMessage(Component.translatable("command.locate.taskStarted"), false);
        return Command.SINGLE_SUCCESS;
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

            source.sendFeedback(chain(
                highlight(Component.translatable("command.locate.biome.foundAt")),
                highlight(" "),
                copy(
                    hover(
                        accent("x: %d, z: %d".formatted(pos.x(), pos.z())),
                        base(Component.translatable("chat.copy.click"))
                    ),
                    "%d ~ %d".formatted(pos.x(), pos.z())
                ),
                highlight(".")
            ));
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

            source.sendFeedback(chain(
                highlight(Component.translatable("command.locate.feature.structure.foundAt")),
                highlight(" "),
                copy(
                    hover(
                        accent("x: %d, z: %d".formatted(Pos.x(structurePos), Pos.z(structurePos))),
                        base(Component.translatable("chat.copy.click"))
                    ),
                    "%d ~ %d".formatted(Pos.x(structurePos), Pos.z(structurePos))
                ),
                highlight(".")
            ));

            if (structure == Cubiomes.End_City()) {
                int numPieces = Cubiomes.getEndCityPieces(pieces, seed, Pos.x(structurePos) >> 4, Pos.z(structurePos) >> 4);
                IntStream.range(0, numPieces)
                    .mapToObj(i -> Piece.asSlice(pieces, i))
                    .filter(piece -> Piece.type(piece) == Cubiomes.END_SHIP())
                    .findAny() // only one ship per end city
                    .map(Piece::pos)
                    .map(city -> new BlockPos(Pos3.x(city), Pos3.y(city) + 60, Pos3.z(city)))
                    .ifPresent(city -> source.sendFeedback(Component.literal(" - ").append(Component.translatable("command.locate.feature.structure.endCity.hasShip",
                        copy(
                            hover(
                                accent("x: %d, y: %d, z: %d".formatted(city.getX(), city.getY(), city.getZ())),
                                base(Component.translatable("chat.copy.click"))
                            ),
                            "%d %d %d".formatted(city.getX(), city.getY(), city.getZ())
                        )))));
            } else if (structure == Cubiomes.Fortress()) {
                int numPieces = Cubiomes.getFortressPieces(pieces, 400, version, seed, Pos.x(structurePos) >> 4, Pos.z(structurePos) >> 4);
                IntStream.range(0, numPieces)
                    .mapToObj(i -> Piece.asSlice(pieces, i))
                    .filter(piece -> Piece.type(piece) == Cubiomes.BRIDGE_SPAWNER())
                    .map(Piece::pos)
                    .map(monsterThrone -> new BlockPos(Pos3.x(monsterThrone), Pos3.y(monsterThrone) + 10, Pos3.z(monsterThrone)))
                    .forEach(spawnerPos -> source.sendFeedback(Component.literal(" - ").append(Component.translatable("command.locate.feature.structure.fortress.hasSpawner",
                        copy(
                            hover(
                                accent("x: %d, y: %d, z: %d".formatted(spawnerPos.getX(), spawnerPos.getY(), spawnerPos.getZ())),
                                base(Component.translatable("chat.copy.click"))
                            ),
                            "%d %d %d".formatted(spawnerPos.getX(), spawnerPos.getY(), spawnerPos.getZ())
                        )))));
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

        source.sendFeedback(chain(
            highlight(Component.translatable("command.locate.feature.stronghold.success", copy(
                hover(
                    accent("x: %d, z: %d".formatted(pos.getX(), pos.getZ())),
                    base(Component.translatable("chat.copy.click"))
                ),
                "%d ~ %d".formatted(pos.getX(), pos.getZ())
            )))
        ));
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
        source.sendFeedback(chain(
            highlight(Component.translatable("command.locate.feature.slimeChunk.foundAt")),
            highlight(" "),
            copy(
                hover(
                    accent("x: %d, z: %d".formatted(blockPosX, blockPosZ)),
                    base(Component.translatable("command.locate.feature.slimeChunk.copy"))
                ),
                "%d ~ %d".formatted(blockPosX, blockPosZ)
            ),
            highlight(" ("),
            highlight(Component.translatable("command.locate.feature.slimeChunk.chunk")),
            highlight(" "),
            copy(
                hover(
                    accent(pos.x() + " " + pos.z()),
                    base(Component.translatable("command.locate.feature.slimeChunk.copyChunk"))
                ),
                "%d %d".formatted(pos.x(), pos.z())
            ),
            highlight(").")
        ));
        return Command.SINGLE_SUCCESS;
    }

    private static int locateSpawn(CustomClientCommandSource source) throws CommandSyntaxException {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment generator = Generator.allocate(arena);
            Cubiomes.setupGenerator(generator, source.getVersion(), 0);
            Cubiomes.applySeed(generator, source.getDimension(), source.getSeed().getSecond());
            MemorySegment pos = Cubiomes.getSpawn(arena, generator);

            source.sendFeedback(chain(
                highlight(Component.translatable("command.locate.spawn.success", copy(
                    hover(
                        accent("x: %d, z: %d".formatted(Pos.x(pos), Pos.z(pos))),
                        base(Component.translatable("chat.copy.click"))
                    ),
                    "%d ~ %d".formatted(Pos.x(pos), Pos.z(pos))
                )))
            ));
            return Command.SINGLE_SUCCESS;
        }
    }
}
