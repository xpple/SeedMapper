package dev.xpple.seedmapper.command.commands;

import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.Generator;
import com.github.cubiomes.Pos;
import com.github.cubiomes.StrongholdIter;
import com.github.cubiomes.StructureConfig;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.seedmapper.command.CommandExceptions;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.util.SpiralLoop;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.WorldgenRandom;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import static dev.xpple.seedmapper.command.arguments.BiomeArgument.*;
import static dev.xpple.seedmapper.command.arguments.StructureArgument.*;
import static dev.xpple.seedmapper.util.ChatBuilder.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class LocateCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("sm:locate")
            .then(literal("biome")
                .then(argument("biome", biome())
                    .executes(ctx -> locateBiome(CustomClientCommandSource.of(ctx.getSource()), getBiome(ctx, "biome")))))
            .then(literal("feature")
                .then(literal("structure")
                    .then(argument("structure", structure())
                        .executes(ctx -> locateStructure(CustomClientCommandSource.of(ctx.getSource()), getStructure(ctx, "structure")))))
                .then(literal("stronghold")
                    .executes(ctx -> locateStronghold(CustomClientCommandSource.of(ctx.getSource()))))
                .then(literal("slimechunk")
                    .executes(ctx -> locateSlimeChunk(CustomClientCommandSource.of(ctx.getSource())))))
            .then(literal("spawn")
                .executes(ctx -> locateSpawn(CustomClientCommandSource.of(ctx.getSource())))));
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

            SpiralLoop.spiral(center.getX(), center.getZ(), 6400, 32, (x, z) -> {
                if (Cubiomes.getBiomeAt(generator, 1, x, 63, z) != biome) {
                    return false;
                }
                source.sendFeedback(chain(
                    highlight(Component.translatable("command.locate.biome.foundAt")),
                    highlight(" "),
                    copy(
                        hover(
                            accent("x: %d, z: %d".formatted(x, z)),
                            base(Component.translatable("chat.copy.click"))
                        ),
                        "%d ~ %d".formatted(x, z)
                    ),
                    highlight(".")
                ));
                return true;
            }, CommandExceptions.NO_BIOME_FOUND_EXCEPTION::create);
            return Command.SINGLE_SUCCESS;
        }
    }

    private static int locateStructure(CustomClientCommandSource source, int structure) throws CommandSyntaxException {
        int dimension = source.getDimension();
        if (Cubiomes.getStructureDimension(structure) != dimension) {
            throw CommandExceptions.INVALID_DIMENSION_EXCEPTION.create();
        }
        try (Arena arena = Arena.ofConfined()) {
            int version = source.getVersion();
            MemorySegment structureConfig = StructureConfig.allocate(arena);
            int config = Cubiomes.getStructureConfig(structure, version, structureConfig);
            if (config == 0) {
                throw CommandExceptions.INCOMPATIBLE_PARAMETERS_EXCEPTION.create();
            }
            long seed = source.getSeed().getSecond();

            MemorySegment generator = Generator.allocate(arena);
            Cubiomes.setupGenerator(generator, version, 0);
            Cubiomes.applySeed(generator, dimension, seed);

            BlockPos center = BlockPos.containing(source.getPosition());
            int regionSize = StructureConfig.regionSize(structureConfig) << 4;
            SpiralLoop.spiral(center.getX() / regionSize, center.getZ() / regionSize, Level.MAX_LEVEL_SIZE / regionSize, (x, z) -> {
                MemorySegment structurePos = Pos.allocate(arena);
                if (Cubiomes.getStructurePos(structure, version, seed, x, z, structurePos) == 0) {
                    return false;
                }
                if (Cubiomes.isViableStructurePos(structure, generator, Pos.x(structurePos), Pos.z(structurePos), 0) == 0) {
                    return false;
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
                return true;
            }, () -> CommandExceptions.NO_STRUCTURE_FOUND_EXCEPTION.create(Level.MAX_LEVEL_SIZE));
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

        try (Arena arena = Arena.ofConfined()) {
            SortedSet<MemorySegment> strongholdLocations = new TreeSet<>(Comparator.comparingDouble(o -> position.distToLowCornerSqr(Pos.x(o), position.getY(), Pos.z(o))));

            MemorySegment strongholdIter = StrongholdIter.allocate(arena);
            Cubiomes.initFirstStronghold(arena, strongholdIter, version, seed);
            MemorySegment generator = Generator.allocate(arena);
            Cubiomes.setupGenerator(generator, version, 0);
            Cubiomes.applySeed(generator, dimension, seed);

            for (int i = 0; i < 12; i++) {
                if (Cubiomes.nextStronghold(strongholdIter, generator) == 0) {
                    break;
                }
                MemorySegment pos = Pos.allocate(arena);
                strongholdLocations.add(pos.copyFrom(StrongholdIter.pos(strongholdIter)));
            }

            MemorySegment pos = strongholdLocations.getFirst();

            source.sendFeedback(chain(
                highlight(Component.translatable("command.locate.feature.stronghold.success", copy(
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

    private static int locateSlimeChunk(CustomClientCommandSource source) throws CommandSyntaxException {
        if (source.getDimension() != Cubiomes.DIM_OVERWORLD()) {
            throw CommandExceptions.INVALID_DIMENSION_EXCEPTION.create();
        }
        long seed = source.getSeed().getSecond();
        ChunkPos center = new ChunkPos(BlockPos.containing(source.getPosition()));
        SpiralLoop.spiral(center.x, center.z, 6400, (x, z) -> {
            RandomSource random = WorldgenRandom.seedSlimeChunk(x, z, seed, 987234911L);
            if (random.nextInt(10) == 0) {
                int blockPosX = (x << 4) + 9;
                int blockPosZ = (z << 4) + 9;
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
                            accent(x + " " + z),
                            base(Component.translatable("command.locate.feature.slimeChunk.copyChunk"))
                        ),
                        "%d %d".formatted(x, z)
                    ),
                    highlight(").")
                ));
                return true;
            }
            return false;
        }, CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand()::create);
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
