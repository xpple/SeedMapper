package dev.xpple.seedmapper.command.commands;

import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.Generator;
import com.github.cubiomes.Pos;
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
import net.minecraft.world.level.levelgen.WorldgenRandom;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.List;

import static com.mojang.brigadier.arguments.BoolArgumentType.*;
import static dev.xpple.seedmapper.command.arguments.BiomeArgument.*;
import static dev.xpple.seedmapper.command.arguments.StructureArgument.*;
import static dev.xpple.seedmapper.util.ChatBuilder.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class LocateCommand {

    private static final int BIOME_SEARCH_RADIUS = 6400;

    private static final Long2ObjectMap<TwoDTree> cachedStrongholds = new Long2ObjectOpenHashMap<>();

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("sm:locate")
            .then(literal("biome")
                .then(argument("biome", biome())
                    .executes(ctx -> locateBiome(CustomClientCommandSource.of(ctx.getSource()), getBiome(ctx, "biome")))))
            .then(literal("feature")
                .then(literal("structure")
                    .then(argument("structure", structure())
                        .executes(ctx -> locateStructure(CustomClientCommandSource.of(ctx.getSource()), getStructure(ctx, "structure")))
                        .then(argument("variantdata", bool())
                            .executes(ctx -> locateStructure(CustomClientCommandSource.of(ctx.getSource()), getStructure(ctx, "structure"), getBool(ctx, "variantdata"))))))
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

    private static int locateStructure(CustomClientCommandSource source, int structure) throws CommandSyntaxException {
        return locateStructure(source, structure, false);
    }

    private static int locateStructure(CustomClientCommandSource source, int structure, boolean variantData) throws CommandSyntaxException {
        try (Arena arena = Arena.ofConfined()) {
            int version = source.getVersion();
            int dimension = source.getDimension();
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

            StructureChecks.StructureCheck structureCheck = StructureChecks.get(structure);

            BlockPos center = BlockPos.containing(source.getPosition());
            int regionSize = StructureConfig.regionSize(structureConfig) << 4;
            MemorySegment structurePos = Pos.allocate(arena);
            SpiralLoop.Coordinate pos = SpiralLoop.spiral(center.getX() / regionSize, center.getZ() / regionSize, Level.MAX_LEVEL_SIZE / regionSize, (x, z) -> {
                return structureCheck.check(generator, surfaceNoise, x, z, structurePos);
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

            if (!variantData) {
                return Command.SINGLE_SUCCESS;
            }
            int biome = Cubiomes.getBiomeAt(generator, 4, Pos.x(structurePos) >> 2, 320 >> 2, Pos.z(structurePos) >> 2);
            MemorySegment structureVariant = StructureVariant.allocate(arena);
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
