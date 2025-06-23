package dev.xpple.seedmapper.command.commands;

import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.Generator;
import com.github.cubiomes.ItemStack;
import com.github.cubiomes.LootTableContext;
import com.github.cubiomes.OreVeinParameters;
import com.github.cubiomes.Piece;
import com.github.cubiomes.Pos;
import com.github.cubiomes.Pos3;
import com.github.cubiomes.StrongholdIter;
import com.github.cubiomes.StructureConfig;
import com.github.cubiomes.StructureSaltConfig;
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
import dev.xpple.seedmapper.util.SpiralSpliterator;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.mojang.brigadier.arguments.BoolArgumentType.*;
import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static dev.xpple.seedmapper.command.arguments.BiomeArgument.*;
import static dev.xpple.seedmapper.command.arguments.ItemAndEnchantmentsPredicateArgument.*;
import static dev.xpple.seedmapper.command.arguments.StructurePredicateArgument.*;
import static dev.xpple.seedmapper.thread.ThreadingHelper.*;
import static dev.xpple.seedmapper.util.ChatBuilder.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class LocateCommand {

    private static final int BIOME_SEARCH_RADIUS = 6400;

    private static final Long2ObjectMap<TwoDTree> cachedStrongholds = new Long2ObjectOpenHashMap<>();

    private static final Set<Integer> LOOT_SUPPORTED_STRUCTURES = Set.of(Cubiomes.Treasure(), Cubiomes.Desert_Pyramid(), Cubiomes.End_City(), Cubiomes.Igloo(), Cubiomes.Ruined_Portal(), Cubiomes.Ruined_Portal_N());

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
            .then(literal("loot")
                .then(argument("amount", integer(1))
                    .then(argument("item", itemAndEnchantments())
                        .executes(ctx -> submit(() -> locateLoot(CustomClientCommandSource.of(ctx.getSource()), getInteger(ctx, "amount"), getItemAndEnchantments(ctx, "item")))))))
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
                int numPieces = Cubiomes.getFortressPieces(pieces, StructureChecks.MAX_END_CITY_AND_FORTRESS_PIECES, version, seed, Pos.x(structurePos) >> 4, Pos.z(structurePos) >> 4);
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

    private static int locateLoot(CustomClientCommandSource source, int amount, EnchantedItem itemPredicate) throws CommandSyntaxException {
        int version = source.getVersion();
        if (version <= Cubiomes.MC_1_12()) {
            throw CommandExceptions.LOOT_NOT_SUPPORTED_EXCEPTION.create();
        }
        int dimension = source.getDimension();
        long seed = source.getSeed().getSecond();

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment generator = Generator.allocate(arena);
            Cubiomes.setupGenerator(generator, version, 0);
            Cubiomes.applySeed(generator, dimension, seed);

            // currently only used for end cities
            MemorySegment surfaceNoise = SurfaceNoise.allocate(arena);
            Cubiomes.initSurfaceNoise(surfaceNoise, dimension, seed);

            BlockPos center = BlockPos.containing(source.getPosition());

            record StructureIterationState(MemorySegment structureConfig, StructureChecks.GenerationCheck generationCheck, SpiralSpliterator iterator) {
            }
            List<StructureIterationState> structureStates = LOOT_SUPPORTED_STRUCTURES.stream()
                .<MemorySegment>mapMulti((s, consumer) -> {
                    MemorySegment structureConfig = StructureConfig.allocate(arena);
                    if (Cubiomes.getStructureConfig(s, version, structureConfig) != 0) {
                        consumer.accept(structureConfig);
                    }
                })
                .filter(sconf -> StructureConfig.dim(sconf) == dimension)
                .sorted(Comparator.comparingInt(StructureConfig::regionSize))
                .map(sconf -> {
                    int regionSize = StructureConfig.regionSize(sconf) << 4;
                    return new StructureIterationState(sconf, StructureChecks.getGenerationCheck(StructureConfig.structType(sconf)), new SpiralSpliterator(center.getX() / regionSize, center.getZ() / regionSize, Level.MAX_LEVEL_SIZE / regionSize));
                })
                .collect(Collectors.toList());

            // count how many loot tables each structure has
            Map<Integer, Integer> lootTableCount = structureStates.stream()
                .collect(Collectors.toMap(state -> (int) StructureConfig.structType(state.structureConfig), state -> Cubiomes.getLootTableCountForStructure(StructureConfig.structType(state.structureConfig), version)));
            // ignore chests with loot tables that do not contain the desired item
            Map<Integer, Set<String>> ignoredLootTables = new HashMap<>();

            MemorySegment structurePos = Pos.allocate(arena);
            MemorySegment pieces = Piece.allocateArray(StructureChecks.MAX_END_CITY_AND_FORTRESS_PIECES, arena);
            MemorySegment structureVariant = StructureVariant.allocate(arena);
            MemorySegment structureSaltConfig = StructureSaltConfig.allocate(arena);
            int[] found = {0};

            /*
             * To locate loot closest to the player, all structures
             * must be considered. That means we must cycle over
             * the structures and incrementally find the desired
             * loot. However, structures have different region
             * sizes. That means some structures appear more often
             * than others. To this end, the below code intends to
             * loop over structures with smaller region sizes more
             * often.
             */
            for (int stateIndex = 0, radius = 0;; stateIndex++) {
                if (stateIndex >= structureStates.size()) {
                    stateIndex = 0;
                    radius++;
                }
                StructureIterationState state = structureStates.get(stateIndex);
                MemorySegment structureConfig = state.structureConfig;
                int structure = StructureConfig.structType(structureConfig);

                int regionSize = StructureConfig.regionSize(structureConfig) << 4;
                int maxRegionSize = StructureConfig.regionSize(structureStates.getLast().structureConfig) << 4;

                int previouslyFound = found[0];
                List<BlockPos> aggregatedLootPositions = new ArrayList<>();
                while (true) {
                    if (state.iterator.getX() * regionSize > center.getX() + radius * maxRegionSize
                        || state.iterator.getZ() * regionSize > center.getZ() + radius * maxRegionSize) {
                        break;
                    }

                    boolean exhausted = !state.iterator.tryAdvance(pos -> {
                        if (!state.generationCheck.check(generator, surfaceNoise, pos.x(), pos.z(), structurePos)) {
                            return;
                        }
                        int posX = Pos.x(structurePos);
                        int posZ = Pos.z(structurePos);
                        int biome = Cubiomes.getBiomeAt(generator, 4, posX >> 2, 320 >> 2, posZ >> 2);
                        Cubiomes.getVariant(structureVariant, structure, version, seed, posX, posZ, biome);
                        biome = StructureVariant.biome(structureVariant) != -1 ? StructureVariant.biome(structureVariant) : biome;
                        if (Cubiomes.getStructureSaltConfig(structure, version, biome, structureSaltConfig) == 0) {
                            return;
                        }
                        int numPieces = Cubiomes.getStructurePieces(pieces, StructureChecks.MAX_END_CITY_AND_FORTRESS_PIECES, structure, structureSaltConfig, structureVariant, version, seed, posX, posZ);
                        if (numPieces <= 0) {
                            return;
                        }
                        int foundInStructure = 0;
                        for (int i = 0; i < numPieces; i++) {
                            MemorySegment piece = Piece.asSlice(pieces, i);
                            int chestCount = Piece.chestCount(piece);
                            if (chestCount == 0) {
                                continue;
                            }
                            if (ignoredLootTables.getOrDefault(structure, Collections.emptySet()).contains(Piece.lootTable(piece).getString(0))) {
                                continue;
                            }
                            MemorySegment lootTableContext = LootTableContext.allocate(arena);
                            // it seems caching the loot tables is not faster
                            if (Cubiomes.init_loot_table_name(lootTableContext, Piece.lootTable(piece), version) == 0) {
                                continue;
                            }
                            if (Cubiomes.has_item(lootTableContext, itemPredicate.item()) == 0) {
                                Cubiomes.free_loot_table_pools(lootTableContext);
                                Set<String> structureIgnoredLootTables = ignoredLootTables.computeIfAbsent(structure, _ -> new HashSet<>());
                                structureIgnoredLootTables.add(Piece.lootTable(piece).getString(0));
                                // if structure has no loot tables with the desired item, remove structure from state loop
                                if (structureIgnoredLootTables.size() == lootTableCount.get(structure)) {
                                    return;
                                }
                                continue;
                            }
                            for (int j = 0; j < chestCount; j++) {
                                Cubiomes.set_loot_seed(lootTableContext, Piece.lootSeeds(piece).getAtIndex(Cubiomes.C_LONG_LONG, j));
                                Cubiomes.generate_loot(lootTableContext);
                                int lootCount = LootTableContext.generated_item_count(lootTableContext);
                                for (int k = 0; k < lootCount; k++) {
                                    MemorySegment itemStack = ItemStack.asSlice(LootTableContext.generated_items(lootTableContext), k);
                                    if (Cubiomes.get_global_item_id(lootTableContext, ItemStack.item(itemStack)) == itemPredicate.item() && itemPredicate.enchantmensPredicate().test(itemStack)) {
                                        foundInStructure += ItemStack.count(itemStack);
                                    }
                                }
                            }
                            Cubiomes.free_loot_table_pools(lootTableContext);
                        }
                        if (foundInStructure > 0) {
                            found[0] += foundInStructure;
                            aggregatedLootPositions.add(new BlockPos(posX, 0, posZ));
                        }
                    });
                    if (exhausted) {
                        structureStates.remove(stateIndex);
                        break;
                    }
                    if (ignoredLootTables.getOrDefault(structure, Collections.emptySet()).size() == lootTableCount.get(structure)) {
                        structureStates.remove(stateIndex);
                        break;
                    }
                    if (found[0] >= amount) {
                        break;
                    }
                }
                int newlyFound = found[0] - previouslyFound;
                if (newlyFound > 0) {
                    String structureName = Cubiomes.struct2str(StructureConfig.structType(structureConfig)).getString(0);
                    source.getClient().schedule(() -> source.sendFeedback(Component.translatable("command.locate.loot.foundAtStructure", accent(String.valueOf(newlyFound)), structureName, ComponentUtils.formatXZCollection(aggregatedLootPositions))));
                }
                if (found[0] >= amount) {
                    break;
                }
            }
            String itemName = Cubiomes.global_id2item_name(itemPredicate.item()).getString(0);
            source.getClient().schedule(() -> source.sendFeedback(Component.translatable("command.locate.loot.totalFound", accent(String.valueOf(found[0])), itemName)));
            return found[0];
        }
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
            if (Cubiomes.initOreVeinNoise(parameters, seed, version) == 0) {
                throw CommandExceptions.ORE_VEIN_WRONG_VERSION_EXCEPTION.create();
            }
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
