package dev.xpple.seedmapper.command.commands;

import com.github.cubiomes.CanyonCarverConfig;
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
import dev.xpple.seedmapper.command.arguments.CanyonCarverArgument;
import dev.xpple.seedmapper.feature.StructureChecks;
import dev.xpple.seedmapper.feature.StructureVariantFeedbackHelper;
import dev.xpple.seedmapper.seedmap.SeedMapScreen;
import dev.xpple.seedmapper.util.ComponentUtils;
import dev.xpple.seedmapper.util.SeedIdentifier;
import dev.xpple.seedmapper.util.SpiralLoop;
import dev.xpple.seedmapper.util.SpiralSpliterator;
import dev.xpple.seedmapper.util.TwoDTree;
import dev.xpple.seedmapper.util.WorldIdentifier;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.WorldgenRandom;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntBiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.mojang.brigadier.arguments.BoolArgumentType.*;
import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static dev.xpple.seedmapper.command.arguments.BiomeArgument.*;
import static dev.xpple.seedmapper.command.arguments.ItemAndEnchantmentsPredicateArgument.*;
import static dev.xpple.seedmapper.command.arguments.StructurePredicateArgument.*;
import static dev.xpple.seedmapper.thread.LocatorThreadHelper.*;
import static dev.xpple.seedmapper.util.ChatBuilder.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class LocateCommand {

    private static final int BIOME_SEARCH_RADIUS = 25600;
    private static final int BIOME_SEARCH_HORIZONTAL_STEP = 32;
    private static final int BIOME_SEARCH_VERTICAL_STEP = 64;

    public static final Set<Integer> LOOT_SUPPORTED_STRUCTURES = Set.of(Cubiomes.Treasure(), Cubiomes.Desert_Pyramid(), Cubiomes.End_City(), Cubiomes.Igloo(), Cubiomes.Jungle_Pyramid(), Cubiomes.Ruined_Portal(), Cubiomes.Ruined_Portal_N(), Cubiomes.Fortress(), Cubiomes.Bastion(), Cubiomes.Outpost(), Cubiomes.Shipwreck());

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("sm:locate")
            .then(literal("biome")
                .then(argument("biome", biome())
                    .executes(ctx -> submit(() -> locateBiome(CustomClientCommandSource.of(ctx.getSource()), getBiome(ctx, "biome"))))))
            .then(literal("feature")
                .then(argument("structure", structurePredicate())
                    .executes(ctx -> submit(() -> locateStructure(CustomClientCommandSource.of(ctx.getSource()), getStructurePredicate(ctx, "structure"))))
                    .then(argument("variantdata", bool())
                        .executes(ctx -> submit(() -> locateStructure(CustomClientCommandSource.of(ctx.getSource()), getStructurePredicate(ctx, "structure"), getBool(ctx, "variantdata"))))))
                .then(literal("stronghold")
                    .executes(ctx -> submit(() -> locateStronghold(CustomClientCommandSource.of(ctx.getSource()))))))
            .then(literal("loot")
                .then(argument("amount", integer(1))
                    .then(argument("item", itemAndEnchantments())
                        .executes(ctx -> submit(() -> locateLoot(CustomClientCommandSource.of(ctx.getSource()), getInteger(ctx, "amount"), getItemAndEnchantments(ctx, "item")))))))
            .then(literal("slimechunk")
                .executes(ctx -> locateSlimeChunk(CustomClientCommandSource.of(ctx.getSource()))))
            .then(literal("spawn")
                .executes(ctx -> locateSpawn(CustomClientCommandSource.of(ctx.getSource()))))
            .then(literal("orevein")
                .then(literal("copper")
                    .executes(ctx -> submit(() -> locateOreVein(CustomClientCommandSource.of(ctx.getSource()), true))))
                .then(literal("iron")
                    .executes(ctx -> submit(() -> locateOreVein(CustomClientCommandSource.of(ctx.getSource()), false)))))
            .then(literal("canyon")
                .executes(ctx -> submit(() -> locateCanyon(CustomClientCommandSource.of(ctx.getSource()))))));
    }

    private static int locateBiome(CustomClientCommandSource source, int biome) throws CommandSyntaxException {
        SeedIdentifier seed = source.getSeed().getSecond();
        int dimension = source.getDimension();
        int version = source.getVersion();
        if (Cubiomes.getDimension(biome) != dimension) {
            throw CommandExceptions.INVALID_DIMENSION_EXCEPTION.create();
        }
        if (Cubiomes.biomeExists(version, biome) == 0) {
            throw CommandExceptions.BIOME_WRONG_VERSION_EXCEPTION.create();
        }
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment generator = Generator.allocate(arena);
            Cubiomes.setupGenerator(generator, version, source.getGeneratorFlags());
            Cubiomes.applySeed(generator, dimension, seed.seed());

            BlockPos center = BlockPos.containing(source.getPosition());

            int minY = version <= Cubiomes.MC_1_17_1() ? 0 : -64;
            int maxY = version <= Cubiomes.MC_1_17_1() ? 256 : 320;
            int[] ys = Mth.outFromOrigin(center.getY(), minY + 1, maxY + 1, BIOME_SEARCH_VERTICAL_STEP).toArray();

            SpiralLoop.Coordinate pos = SpiralLoop.spiral(center.getX(), center.getZ(), BIOME_SEARCH_RADIUS, BIOME_SEARCH_HORIZONTAL_STEP, (x, z) -> {
                for (int y : ys) {
                    if (Cubiomes.getBiomeAt(generator, 4, QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z)) == biome) {
                        return true;
                    }
                }
                return false;
            });
            if (pos == null) {
                throw CommandExceptions.NO_BIOME_FOUND_EXCEPTION.create(BIOME_SEARCH_RADIUS);
            }

            source.getClient().schedule(() -> source.sendFeedback(Component.translatable("command.locate.biome.foundAt", ComponentUtils.formatXZ(pos.x(), pos.z()))));
            return Command.SINGLE_SUCCESS;
        }
    }

    private static int locateStructure(CustomClientCommandSource source, StructureAndPredicate structureAndPredicate) throws CommandSyntaxException {
        return locateStructure(source, structureAndPredicate, false);
    }

    private static int locateStructure(CustomClientCommandSource source, StructureAndPredicate structureAndPredicate, boolean variantData) throws CommandSyntaxException {
        SeedIdentifier seed = source.getSeed().getSecond();
        int version = source.getVersion();
        int dimension = source.getDimension();
        try (Arena arena = Arena.ofConfined()) {
            int structure = structureAndPredicate.structure();
            MemorySegment structureConfig = StructureConfig.allocate(arena);
            int config = Cubiomes.getStructureConfig(structure, version, structureConfig);
            if (config == 0) {
                throw CommandExceptions.INCOMPATIBLE_PARAMETERS_EXCEPTION.create();
            }
            if (StructureConfig.dim(structureConfig) != dimension) {
                throw CommandExceptions.INVALID_DIMENSION_EXCEPTION.create();
            }

            MemorySegment generator = Generator.allocate(arena);
            Cubiomes.setupGenerator(generator, version, source.getGeneratorFlags());
            Cubiomes.applySeed(generator, dimension, seed.seed());

            // currently only used for end cities
            MemorySegment surfaceNoise = SurfaceNoise.allocate(arena);
            Cubiomes.initSurfaceNoise(surfaceNoise, dimension, seed.seed());

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

            int structureXPos = Pos.x(structurePos);
            int structureZPos = Pos.z(structurePos);
            source.getClient().schedule(() -> source.sendFeedback(Component.translatable("command.locate.feature.structure.foundAt", ComponentUtils.formatXZ(structureXPos, structureZPos))));

            if (structure == Cubiomes.End_City()) {
                int numPieces = Cubiomes.getEndCityPieces(pieces, seed.seed(), structureXPos >> 4, structureZPos >> 4);
                IntStream.range(0, numPieces)
                    .mapToObj(i -> Piece.asSlice(pieces, i))
                    .filter(piece -> Piece.type(piece) == Cubiomes.END_SHIP())
                    .findAny() // only one ship per end city
                    .map(Piece::pos)
                    .map(city -> new BlockPos(Pos3.x(city), Pos3.y(city) + 60, Pos3.z(city)))
                    .ifPresent(city -> source.getClient().schedule(() -> source.sendFeedback(Component.literal(" - ")
                        .append(Component.translatable("command.locate.feature.structure.endCity.hasShip", ComponentUtils.formatXYZ(city.getX(), city.getY(), city.getZ()))))));
            } else if (structure == Cubiomes.Fortress()) {
                int numPieces = Cubiomes.getFortressPieces(pieces, StructureChecks.MAX_END_CITY_AND_FORTRESS_PIECES, version, seed.seed(), structureXPos >> 4, structureZPos >> 4);
                IntStream.range(0, numPieces)
                    .mapToObj(i -> Piece.asSlice(pieces, i))
                    .filter(piece -> Piece.type(piece) == Cubiomes.BRIDGE_SPAWNER())
                    .map(Piece::pos)
                    .map(monsterThrone -> new BlockPos(Pos3.x(monsterThrone), Pos3.y(monsterThrone) + 10, Pos3.z(monsterThrone)))
                    .forEach(spawnerPos -> source.getClient().schedule(() -> source.sendFeedback(Component.literal(" - ")
                        .append(Component.translatable("command.locate.feature.structure.fortress.hasSpawner", ComponentUtils.formatXYZ(spawnerPos.getX(), spawnerPos.getY(), spawnerPos.getZ()))))));
            }

            if (!variantData) {
                return Command.SINGLE_SUCCESS;
            }
            int biome = Cubiomes.getBiomeAt(generator, 4, structureXPos >> 2, 320 >> 2, structureZPos >> 2);
            Cubiomes.getVariant(structureVariant, structure, version, seed.seed(), structureXPos, structureZPos, biome);

            List<Component> components = StructureVariantFeedbackHelper.get(structure, structureVariant);
            if (components.isEmpty()) {
                return Command.SINGLE_SUCCESS;
            }
            source.getClient().schedule(() -> source.sendFeedback(Component.translatable("command.locate.feature.structure.variantData")));
            components.forEach(component -> source.getClient().schedule(() -> source.sendFeedback(Component.literal(" - ").append(component))));
            return Command.SINGLE_SUCCESS;
        }
    }

    private static int locateStronghold(CustomClientCommandSource source) throws CommandSyntaxException {
        SeedIdentifier seed = source.getSeed().getSecond();
        int dimension = source.getDimension();
        if (dimension != Cubiomes.DIM_OVERWORLD()) {
            throw CommandExceptions.INVALID_DIMENSION_EXCEPTION.create();
        }
        int version = source.getVersion();
        int generatorFlags = source.getGeneratorFlags();

        BlockPos position = BlockPos.containing(source.getPosition());

        TwoDTree tree = SeedMapScreen.strongholdDataCache.computeIfAbsent(new WorldIdentifier(seed.seed(), dimension, version, generatorFlags), _ -> calculateStrongholds(seed.seed(), dimension, version, generatorFlags));

        BlockPos pos = tree.nearestTo(position.atY(0));

        source.getClient().schedule(() -> source.sendFeedback(Component.translatable("command.locate.feature.stronghold.success", ComponentUtils.formatXZ(pos.getX(), pos.getZ()))));
        return Command.SINGLE_SUCCESS;
    }

    public static TwoDTree calculateStrongholds(long seed, int dimension, int version, int generatorFlags) {
        TwoDTree tree = new TwoDTree();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment strongholdIter = StrongholdIter.allocate(arena);
            Cubiomes.initFirstStronghold(arena, strongholdIter, version, seed);
            MemorySegment generator = Generator.allocate(arena);
            Cubiomes.setupGenerator(generator, version, generatorFlags);
            Cubiomes.applySeed(generator, dimension, seed);

            final int count = version <= Cubiomes.MC_1_8() ? 3 : 128;
            for (int i = 0; i < count; i++) {
                if (Cubiomes.nextStronghold(strongholdIter, generator) == 0) {
                    break;
                }
                MemorySegment pos = StrongholdIter.pos(strongholdIter);
                tree.insert(new BlockPos(Pos.x(pos), 0, Pos.z(pos)));
            }
        }
        tree.balance();
        return tree;
    }

    private static int locateSlimeChunk(CustomClientCommandSource source) throws CommandSyntaxException {
        SeedIdentifier seed = source.getSeed().getSecond();
        if (source.getDimension() != Cubiomes.DIM_OVERWORLD()) {
            throw CommandExceptions.INVALID_DIMENSION_EXCEPTION.create();
        }
        ChunkPos center = new ChunkPos(BlockPos.containing(source.getPosition()));
        SpiralLoop.Coordinate pos = SpiralLoop.spiral(center.x, center.z, 6400, (x, z) -> {
            RandomSource random = WorldgenRandom.seedSlimeChunk(x, z, seed.seed(), 987234911L);
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
        SeedIdentifier seed = source.getSeed().getSecond();
        int version = source.getVersion();
        if (version <= Cubiomes.MC_1_12()) {
            throw CommandExceptions.LOOT_NOT_SUPPORTED_EXCEPTION.create();
        }
        int dimension = source.getDimension();

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment generator = Generator.allocate(arena);
            Cubiomes.setupGenerator(generator, version, source.getGeneratorFlags());
            Cubiomes.applySeed(generator, dimension, seed.seed());

            // currently only used for end cities
            MemorySegment surfaceNoise = SurfaceNoise.allocate(arena);
            Cubiomes.initSurfaceNoise(surfaceNoise, dimension, seed.seed());

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

            MemorySegment ltcPtr = arena.allocate(Cubiomes.C_POINTER);
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
                if (structureStates.isEmpty()) {
                    throw CommandExceptions.LOOT_NOT_AVAILABLE_EXCEPTION.create();
                }
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
                        Cubiomes.getVariant(structureVariant, structure, version, seed.seed(), posX, posZ, biome);
                        biome = StructureVariant.biome(structureVariant) != -1 ? StructureVariant.biome(structureVariant) : biome;
                        if (Cubiomes.getStructureSaltConfig(structure, version, biome, structureSaltConfig) == 0) {
                            return;
                        }
                        int numPieces = Cubiomes.getStructurePieces(pieces, StructureChecks.MAX_END_CITY_AND_FORTRESS_PIECES, structure, structureSaltConfig, structureVariant, version, seed.seed(), posX, posZ);
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
                            MemorySegment lootTables = Piece.lootTables(piece);
                            MemorySegment lootSeeds = Piece.lootSeeds(piece);
                            for (int j = 0; j < chestCount; j++) {
                                MemorySegment lootTable = lootTables.getAtIndex(ValueLayout.ADDRESS, j).reinterpret(Long.MAX_VALUE);
                                MemorySegment lootTableContext;
                                if (Cubiomes.init_loot_table_name(ltcPtr, lootTable, version) == 0) {
                                    lootTableContext = null;
                                } else {
                                    lootTableContext = ltcPtr.get(ValueLayout.ADDRESS, 0).reinterpret(LootTableContext.sizeof());
                                }
                                if (lootTableContext == null || Cubiomes.has_item(lootTableContext, itemPredicate.item()) == 0) {
                                    Set<String> structureIgnoredLootTables = ignoredLootTables.computeIfAbsent(structure, _ -> new HashSet<>());
                                    structureIgnoredLootTables.add(lootTable.getString(0));
                                    // if structure has no loot tables with the desired item, remove structure from state loop
                                    if (structureIgnoredLootTables.size() == lootTableCount.get(structure)) {
                                        return;
                                    }
                                    continue;
                                }
                                Cubiomes.set_loot_seed(lootTableContext, lootSeeds.getAtIndex(Cubiomes.C_LONG_LONG, j));
                                Cubiomes.generate_loot(lootTableContext);
                                int lootCount = LootTableContext.generated_item_count(lootTableContext);
                                for (int k = 0; k < lootCount; k++) {
                                    MemorySegment itemStack = ItemStack.asSlice(LootTableContext.generated_items(lootTableContext), k);
                                    if (Cubiomes.get_global_item_id(lootTableContext, ItemStack.item(itemStack)) == itemPredicate.item() && itemPredicate.enchantmensPredicate().test(itemStack)) {
                                        foundInStructure += ItemStack.count(itemStack);
                                    }
                                }
                            }
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
            String itemName = Cubiomes.global_id2item_name(itemPredicate.item(), version).getString(0);
            source.getClient().schedule(() -> source.sendFeedback(Component.translatable("command.locate.loot.totalFound", accent(String.valueOf(found[0])), itemName)));
            return found[0];
        }
    }

    private static int locateSpawn(CustomClientCommandSource source) throws CommandSyntaxException {
        SeedIdentifier seed = source.getSeed().getSecond();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment generator = Generator.allocate(arena);
            Cubiomes.setupGenerator(generator, source.getVersion(), source.getGeneratorFlags());
            Cubiomes.applySeed(generator, source.getDimension(), seed.seed());
            MemorySegment pos = Cubiomes.getSpawn(arena, generator);

            source.sendFeedback(Component.translatable("command.locate.spawn.success", ComponentUtils.formatXZ(Pos.x(pos), Pos.z(pos))));
            return Command.SINGLE_SUCCESS;
        }
    }

    private static int locateOreVein(CustomClientCommandSource source, boolean wantCopperVein) throws CommandSyntaxException {
        SeedIdentifier seed = source.getSeed().getSecond();
        int version = source.getVersion();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment parameters = OreVeinParameters.allocate(arena);
            if (Cubiomes.initOreVeinNoise(parameters, seed.seed(), version) == 0) {
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
                            boolean isCopperVein = block == Cubiomes.RAW_COPPER_BLOCK() || block == Cubiomes.COPPER_ORE() || block == Cubiomes.GRANITE();
                            if (wantCopperVein == isCopperVein) {
                                pos[0] = new BlockPos(minX + x, y, minZ + z);
                                return true;
                            }
                        }
                    }
                }
                return false;
            });
            if (pos[0] == null) {
                throw CommandExceptions.NO_ORE_VEIN_FOUND_EXCEPTION.create(6400);
            }

            source.getClient().schedule(() -> source.sendFeedback(Component.translatable("command.locate.oreVein.foundAt", ComponentUtils.formatXYZ(pos[0].getX(), pos[0].getY(), pos[0].getZ(), Component.translatable("command.locate.oreVein.copy")))));

            return Command.SINGLE_SUCCESS;
        }
    }

    private static int locateCanyon(CustomClientCommandSource source) throws CommandSyntaxException {
        SeedIdentifier seed = source.getSeed().getSecond();
        int dimension = source.getDimension();
        if (dimension != Cubiomes.DIM_OVERWORLD()) {
            throw CommandExceptions.INVALID_DIMENSION_EXCEPTION.create();
        }
        int version = source.getVersion();
        if (version < Cubiomes.MC_1_13()) {
            throw CommandExceptions.CANYON_WRONG_VERSION_EXCEPTION.create();
        }
        ChunkPos center = new ChunkPos(BlockPos.containing(source.getPosition()));
        try (Arena arena = Arena.ofConfined()) {
            ToIntBiFunction<Integer, Integer> biomeFunction = getCarverBiomeFunction(arena, seed.seed(), dimension, version, source.getGeneratorFlags());
            MemorySegment ccc = CanyonCarverConfig.allocate(arena);
            MemorySegment rnd = arena.allocate(Cubiomes.C_LONG_LONG);
            SpiralLoop.Coordinate pos = SpiralLoop.spiral(center.x, center.z, 6400, (chunkX, chunkZ) -> {
                for (int canyonCarver : CanyonCarverArgument.CANYON_CARVERS.values()) {
                    if (Cubiomes.getCanyonCarverConfig(canyonCarver, version, ccc) == 0) {
                        continue;
                    }
                    int biome = biomeFunction.applyAsInt(chunkX, chunkZ);
                    if (Cubiomes.isViableCanyonBiome(canyonCarver, biome) == 0) {
                        continue;
                    }
                    if (Cubiomes.checkCanyonStart(seed.seed(), chunkX, chunkZ, ccc, rnd) == 0) {
                        continue;
                    }
                    return true;
                }
                return false;
            });
            if (pos == null) {
                throw CommandExceptions.NO_CANYON_FOUND_EXCEPTION.create(6400);
            }
            source.getClient().schedule(() -> source.sendFeedback(Component.translatable("command.locate.canyon.foundAt", ComponentUtils.formatXZ(SectionPos.sectionToBlockCoord(pos.x()), SectionPos.sectionToBlockCoord(pos.z()), Component.translatable("command.locate.canyon.copy")))));
            return Command.SINGLE_SUCCESS;
        }
    }

    static ToIntBiFunction<Integer, Integer> getCarverBiomeFunction(Arena arena, long seed, int dimension, int version, int generatorFlags) {
        if (version > Cubiomes.MC_1_17_1()) {
            return (_, _) -> -1;
        }
        MemorySegment generator = Generator.allocate(arena);
        Cubiomes.setupGenerator(generator, version, generatorFlags);
        Cubiomes.applySeed(generator, dimension, seed);
        return (chunkX, chunkZ) -> Cubiomes.getBiomeAt(generator, 4, chunkX << 2, 0, chunkZ << 2);
    }
}
