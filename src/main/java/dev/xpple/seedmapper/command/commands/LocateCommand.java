package dev.xpple.seedmapper.command.commands;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.seedfinding.mcbiome.biome.Biomes;
import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.rand.seed.WorldSeed;
import com.seedfinding.mccore.util.data.SpiralIterator;
import com.seedfinding.mccore.util.data.Triplet;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mcfeature.Feature;
import com.seedfinding.mcfeature.decorator.Decorator;
import com.seedfinding.mcfeature.decorator.DesertWell;
import com.seedfinding.mcfeature.decorator.EndGateway;
import com.seedfinding.mcfeature.loot.ChestContent;
import com.seedfinding.mcfeature.loot.ILoot;
import com.seedfinding.mcfeature.loot.item.Item;
import com.seedfinding.mcfeature.structure.Mineshaft;
import com.seedfinding.mcfeature.structure.RegionStructure;
import com.seedfinding.mcfeature.structure.RuinedPortal;
import com.seedfinding.mcfeature.structure.Stronghold;
import com.seedfinding.mcfeature.structure.generator.Generator;
import com.seedfinding.mcfeature.structure.generator.Generators;
import com.seedfinding.mcterrain.TerrainGenerator;
import dev.xpple.clientarguments.arguments.CRegistryEntryPredicateArgumentType;
import dev.xpple.clientarguments.arguments.CRegistryPredicateArgumentType;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.command.SharedHelpers;
import dev.xpple.seedmapper.simulation.SimulatedServer;
import dev.xpple.seedmapper.simulation.SimulatedWorld;
import dev.xpple.seedmapper.util.chat.Chat;
import dev.xpple.seedmapper.util.config.Configs;
import dev.xpple.seedmapper.util.features.FeatureFactory;
import dev.xpple.seedmapper.util.features.Features;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.SharedConstants;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static dev.xpple.clientarguments.arguments.CRegistryEntryPredicateArgumentType.getRegistryEntryPredicate;
import static dev.xpple.clientarguments.arguments.CRegistryEntryPredicateArgumentType.registryEntryPredicate;
import static dev.xpple.clientarguments.arguments.CRegistryPredicateArgumentType.getCPredicate;
import static dev.xpple.clientarguments.arguments.CRegistryPredicateArgumentType.registryPredicate;
import static dev.xpple.seedmapper.command.arguments.DecoratorFactoryArgumentType.decoratorFactory;
import static dev.xpple.seedmapper.command.arguments.DecoratorFactoryArgumentType.getDecoratorFactory;
import static dev.xpple.seedmapper.command.arguments.EnchantedItemPredicateArgumentType.enchantedItem;
import static dev.xpple.seedmapper.command.arguments.EnchantedItemPredicateArgumentType.getEnchantedItem;
import static dev.xpple.seedmapper.util.chat.ChatBuilder.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class LocateCommand extends ClientCommand implements SharedHelpers.Exceptions {

    @Override
    protected LiteralCommandNode<FabricClientCommandSource> build(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        return dispatcher.register(literal(this.getRootLiteral())
            .then(literal("biome")
                .then(argument("biome", registryEntryPredicate(registryAccess, RegistryKeys.BIOME))
                    .executes(ctx -> locateBiome(CustomClientCommandSource.of(ctx.getSource()), getRegistryEntryPredicate(ctx, "biome", RegistryKeys.BIOME)))))
            .then(literal("feature")
                .then(literal("structure")
                    .then(argument("structure", registryPredicate(RegistryKeys.STRUCTURE))
                        .executes(ctx -> locateStructure(CustomClientCommandSource.of(ctx.getSource()), getCPredicate(ctx, "structure", RegistryKeys.STRUCTURE, STRUCTURE_NOT_FOUND_EXCEPTION)))))
                .then(literal("poi")
                    .then(argument("poi", registryEntryPredicate(registryAccess, RegistryKeys.POINT_OF_INTEREST_TYPE))
                        .executes(ctx -> locatePoi(CustomClientCommandSource.of(ctx.getSource()), getRegistryEntryPredicate(ctx, "poi", RegistryKeys.POINT_OF_INTEREST_TYPE)))))
                .then(literal("decorator")
                    .then(argument("decorator", decoratorFactory())
                        .executes(ctx -> locateDecorator(CustomClientCommandSource.of(ctx.getSource()), getDecoratorFactory(ctx, "decorator"))))))
            .then(literal("loot")
                .then(argument("amount", integer(1))
                    .then(argument("item", enchantedItem().loot())
                        .executes(ctx -> locateLoot(CustomClientCommandSource.of(ctx.getSource()), getInteger(ctx, "amount"), getEnchantedItem(ctx, "item")))))));
    }

    @Override
    protected String rootLiteral() {
        return "locate";
    }

    private static int locateBiome(CustomClientCommandSource source, CRegistryEntryPredicateArgumentType.EntryPredicate<Biome> biome) throws CommandSyntaxException {
        final Either<BlockPos, BPos> pos;

        if (Configs.UseWorldSimulation) {
            pos = locateBiomeUsingWorldSimulation(biome, source);
        } else {
            pos = locateBiomeUsingLibraries(new Identifier(biome.asString()).getPath(), source);
        }

        BlockPos blockPos = pos.map(Function.identity(), SharedHelpers::fromBPos);

        sendCoordinates(blockPos, biome.asString());
        return Command.SINGLE_SUCCESS;
    }

    private static Either<BlockPos, BPos> locateBiomeUsingWorldSimulation(CRegistryEntryPredicateArgumentType.EntryPredicate<Biome> biome, CustomClientCommandSource source) throws CommandSyntaxException {
        SharedHelpers helpers = new SharedHelpers(source);

        if (!helpers.mcVersion().name.equals(SharedConstants.getGameVersion().getName())) {
            throw UNSUPPORTED_VERSION_EXCEPTION.create();
        }

        try (SimulatedServer server = SimulatedServer.newServer(helpers.seed())) {
            SimulatedWorld world = new SimulatedWorld(server, helpers.dimension());
            BlockPos blockPos = BlockPos.ofFloored(source.getPosition());
            Pair<BlockPos, RegistryEntry<Biome>> pair = world.locateBiome(biome, blockPos, 6400, 32, 64);
            if (pair == null) {
                return Either.left(null);
            }
            return Either.left(pair.getFirst());
        } catch (Exception e) {
            if (e instanceof CommandSyntaxException commandSyntaxException) {
                throw commandSyntaxException;
            }
            throw WORLD_SIMULATION_ERROR_EXCEPTION.create(e.getMessage());
        }
    }

    private static Either<BlockPos, BPos> locateBiomeUsingLibraries(String biomeString, CustomClientCommandSource source) throws CommandSyntaxException {
        SharedHelpers helpers = new SharedHelpers(source);

        var biome = Biomes.REGISTRY.values().stream()
            .filter(b -> b.getName().equals(biomeString))
            .findAny().orElseThrow(() -> BIOME_NOT_FOUND_EXCEPTION.create(biomeString));

        BiomeSource biomeSource = BiomeSource.of(helpers.dimension(), helpers.mcVersion(), helpers.seed());
        if (biome.getDimension() != biomeSource.getDimension()) {
            throw INVALID_DIMENSION_EXCEPTION.create();
        }

        BPos center = SharedHelpers.fromBlockPos(BlockPos.ofFloored(source.getPosition()));
        SpiralIterator<BPos> spiralIterator = new SpiralIterator<>(center, new BPos(6400, 0, 6400), 32, BPos::new);
        return Either.right(StreamSupport.stream(spiralIterator.spliterator(), false)
            .filter(bPos -> biome.equals(biomeSource.getBiome(bPos)))
            .findAny().orElse(null));
    }

    private static int locateStructure(CustomClientCommandSource source, CRegistryPredicateArgumentType.RegistryPredicate<Structure> structure) throws CommandSyntaxException {
        final Either<BlockPos, BPos> pos;

        if (Configs.UseWorldSimulation) {
            pos = locateStructureUsingWorldSimulation(structure, source);
        } else {
            pos = locateStructureUsingLibraries(new Identifier(structure.asString()).getPath(), source);
        }

        BlockPos blockPos = pos.map(Function.identity(), SharedHelpers::fromBPos);

        sendCoordinates(blockPos, structure.asString());
        return Command.SINGLE_SUCCESS;
    }

    private static Either<BlockPos, BPos> locateStructureUsingWorldSimulation(CRegistryPredicateArgumentType.RegistryPredicate<Structure> structure, CustomClientCommandSource source) throws CommandSyntaxException {
        SharedHelpers helpers = new SharedHelpers(source);

        if (!helpers.mcVersion().name.equals(SharedConstants.getGameVersion().getName())) {
            throw UNSUPPORTED_VERSION_EXCEPTION.create();
        }

        try (SimulatedServer server = SimulatedServer.newServer(helpers.seed())) {
            SimulatedWorld world = new SimulatedWorld(server, helpers.dimension());
            Registry<Structure> registry = world.getRegistryManager().get(RegistryKeys.STRUCTURE);
            Optional<? extends RegistryEntryList.ListBacked<Structure>> structures = structure.getKey().map(key -> registry.getEntry(key).map(RegistryEntryList::of), registry::getEntryList);
            if (structures.isEmpty()) {
                throw STRUCTURE_NOT_FOUND_EXCEPTION.create(structure.asString());
            }
            Vec3d position = source.getPosition();
            Pair<BlockPos, RegistryEntry<Structure>> pair = world.getChunkManager().getChunkGenerator().locateStructure(world, structures.get(), BlockPos.ofFloored(position.x, position.y, position.z), 100, false);
            if (pair == null) {
                return Either.left(null);
            }
            return Either.left(pair.getFirst());
        } catch (Exception e) {
            if (e instanceof CommandSyntaxException commandSyntaxException) {
                throw commandSyntaxException;
            }
            throw WORLD_SIMULATION_ERROR_EXCEPTION.create(e.getMessage());
        }
    }

    private static Either<BlockPos, BPos> locateStructureUsingLibraries(String structureString, CustomClientCommandSource source) throws CommandSyntaxException {
        SharedHelpers helpers = new SharedHelpers(source);

        var factory = Features.STRUCTURE_REGISTRY.get(structureString);
        if (factory == null) {
            throw STRUCTURE_NOT_FOUND_EXCEPTION.create(structureString);
        }

        final var structure = factory.create(helpers.mcVersion());

        BiomeSource biomeSource = BiomeSource.of(helpers.dimension(), helpers.mcVersion(), helpers.seed());
        if (!structure.isValidDimension(biomeSource.getDimension())) {
            throw INVALID_DIMENSION_EXCEPTION.create();
        }

        BPos center = SharedHelpers.fromBlockPos(BlockPos.ofFloored(source.getPosition()));
        TerrainGenerator terrainGenerator = TerrainGenerator.of(biomeSource);
        ChunkRand chunkRand = new ChunkRand();
        if (structure instanceof RegionStructure<?, ?> regionStructure) {
            int chunkInRegion = regionStructure.getSpacing();
            int regionSize = chunkInRegion * 16;

            final int border = 30_000_000;
            SpiralIterator<RPos> spiralIterator = new SpiralIterator<>(center.toRegionPos(regionSize), new BPos(-border, 0, -border).toRegionPos(regionSize), new BPos(border, 0, border).toRegionPos(regionSize), 1, (x, y, z) -> new RPos(x, z, regionSize));
            return Either.right(StreamSupport.stream(spiralIterator.spliterator(), false)
                .map(rPos -> regionStructure.getInRegion(biomeSource.getWorldSeed(), rPos.getX(), rPos.getZ(), chunkRand))
                .filter(Objects::nonNull)
                .filter(cPos -> (regionStructure.canSpawn(cPos, biomeSource)) && (terrainGenerator == null || regionStructure.canGenerate(cPos, terrainGenerator)))
                .findAny().map(cPos -> cPos.toBlockPos().add(9, 0, 9)).orElse(null));
        } else {
            if (structure instanceof Stronghold strongholdStructure) {
                CPos currentChunkPos = center.toChunkPos();
                int squaredDistance = Integer.MAX_VALUE;
                CPos closest = new CPos(0, 0);
                for (CPos stronghold : strongholdStructure.getAllStarts(biomeSource, chunkRand)) {
                    int newSquaredDistance = (currentChunkPos.getX() - stronghold.getX()) * (currentChunkPos.getX() - stronghold.getX()) + (currentChunkPos.getZ() - stronghold.getZ()) * (currentChunkPos.getZ() - stronghold.getZ());
                    if (newSquaredDistance < squaredDistance) {
                        squaredDistance = newSquaredDistance;
                        closest = stronghold;
                    }
                }
                BPos dimPos = closest.toBlockPos().add(9, 0, 9);
                return Either.right(new BPos(dimPos.getX(), 0, dimPos.getZ()));
            } else if (structure instanceof Mineshaft mineshaft) {
                SpiralIterator<CPos> spiralIterator = new SpiralIterator<>(new CPos(center.getX() >> 4, center.getZ() >> 4), new CPos(6400, 6400), CPos.Builder::create);

                return Either.right(StreamSupport.stream(spiralIterator.spliterator(), false)
                    .filter(cPos -> {
                        Feature.Data<Mineshaft> data = mineshaft.at(cPos.getX(), cPos.getZ());
                        return data.testStart(biomeSource.getWorldSeed(), chunkRand) && data.testBiome(biomeSource) && data.testGenerate(terrainGenerator);
                    })
                    .findAny().map(cPos -> cPos.toBlockPos().add(9, 0, 9)).orElse(null));
            }
        }
        return Either.right(null);
    }

    private int locatePoi(CustomClientCommandSource source, CRegistryEntryPredicateArgumentType.EntryPredicate<PointOfInterestType> poi) throws CommandSyntaxException {
        if (!Configs.UseWorldSimulation) {
            throw REQUIRES_WORLD_SIMULATION_EXCEPTION.create();
        }

        SharedHelpers helpers = new SharedHelpers(source);

        if (!helpers.mcVersion().name.equals(SharedConstants.getGameVersion().getName())) {
            throw UNSUPPORTED_VERSION_EXCEPTION.create();
        }

        try (SimulatedServer server = SimulatedServer.newServer(helpers.seed())) {
            SimulatedWorld world = new SimulatedWorld(server, helpers.dimension());
            BlockPos position = BlockPos.ofFloored(source.getPosition());
            Optional<Pair<RegistryEntry<PointOfInterestType>, BlockPos>> optional = world.getPointOfInterestStorage().getNearestTypeAndPosition(poi, position, 256, PointOfInterestStorage.OccupationStatus.ANY);
            if (optional.isEmpty()) {
                Chat.print(Text.translatable("command.locate.noneFound", poi.asString()));
            } else {
                Pair<RegistryEntry<PointOfInterestType>, BlockPos> pair = optional.get();
                BlockPos blockPos = pair.getSecond();
                sendCoordinates(blockPos, poi.asString());
            }
        } catch (Exception e) {
            if (e instanceof CommandSyntaxException commandSyntaxException) {
                throw commandSyntaxException;
            }
            throw WORLD_SIMULATION_ERROR_EXCEPTION.create(e.getMessage());
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int locateDecorator(CustomClientCommandSource source, FeatureFactory<? extends Decorator<?, ?>> decoratorFactory) throws CommandSyntaxException {
        SharedHelpers helpers = new SharedHelpers(source);

        final Decorator<?, ?> decorator = decoratorFactory.create(helpers.mcVersion());
        if (Configs.UseWorldSimulation) {
            throw DECORATOR_NOT_FOUND_EXCEPTION.create(decorator.getName());
        }

        BPos decoratorPos = locateDecorator(decorator, source);

        sendCoordinates(decoratorPos, decorator.getName());
        return Command.SINGLE_SUCCESS;
    }

    private static BPos locateDecorator(Decorator<?, ?> decorator, CustomClientCommandSource source) throws CommandSyntaxException {
        SharedHelpers helpers = new SharedHelpers(source);

        BiomeSource biomeSource = BiomeSource.of(helpers.dimension(), helpers.mcVersion(), helpers.seed());
        if (!decorator.isValidDimension(biomeSource.getDimension())) {
            throw INVALID_DIMENSION_EXCEPTION.create();
        }

        CPos center = SharedHelpers.fromBlockPos(BlockPos.ofFloored(source.getPosition())).toChunkPos();
        ChunkRand chunkRand = new ChunkRand();
        long structureSeed = WorldSeed.toStructureSeed(biomeSource.getWorldSeed());
        if (decorator instanceof DesertWell desertWell) {
            SpiralIterator<CPos> spiralIterator = new SpiralIterator<>(center, new CPos(6400, 6400), CPos.Builder::create);
            return StreamSupport.stream(spiralIterator.spliterator(), false)
                .filter(cPos -> {
                    int chunkX = cPos.getX();
                    int chunkZ = cPos.getZ();
                    DesertWell.Data data = desertWell.getData(structureSeed, chunkX, chunkZ, chunkRand);
                    return data != null && desertWell.canSpawn(chunkX, chunkZ, biomeSource);
                })
                .findAny().map(cPos -> cPos.toBlockPos().add(9, 0, 9)).orElse(null);
        } else if (decorator instanceof EndGateway endGateway) {
            SpiralIterator<CPos> spiralIterator = new SpiralIterator<>(center, new CPos(6400, 6400), CPos.Builder::create);
            for (CPos cPos : spiralIterator) {
                int chunkX = cPos.getX();
                int chunkZ = cPos.getZ();
                EndGateway.Data data = endGateway.getData(structureSeed, chunkX, chunkZ, chunkRand);
                if (data == null) {
                    continue;
                }
                if (endGateway.canSpawn(chunkX, chunkZ, biomeSource)) {
                    return new BPos(data.blockX, 0, data.blockZ);
                }
            }
        }
        return null;
    }

    private static int locateLoot(CustomClientCommandSource source, int amount, Pair<String, Predicate<Item>> item) throws CommandSyntaxException {
        SharedHelpers helpers = new SharedHelpers(source);

        String itemString = item.getFirst();

        Set<RegionStructure<?, ?>> lootableStructures = Features.getStructuresForVersion(helpers.mcVersion()).stream()
            .filter(structure -> structure instanceof ILoot)
            .filter(structure -> structure.isValidDimension(helpers.dimension()))
            .map(structure -> (RegionStructure<?, ?>) structure)
            .collect(Collectors.toSet());

        BiomeSource biomeSource = BiomeSource.of(helpers.dimension(), helpers.mcVersion(), helpers.seed());

        BlockPos center = BlockPos.ofFloored(source.getPosition());

        List<BPos> lootPositions = locateLoot(item.getSecond(), i -> i.getName().equals(itemString), amount, new BPos(center.getX(), center.getY(), center.getZ()), new ChunkRand(), biomeSource, lootableStructures);
        if (lootPositions == null || lootPositions.isEmpty()) {
            Chat.print(Text.translatable("command.locate.noneFound", itemString));
        } else {
            Chat.print(chain(
                highlight(Text.translatable("command.locate.loot.foundAt", amount, itemString)),
                highlight(" "),
                join(highlight(", "), lootPositions.stream().map(bPos ->
                    copy(
                        hover(
                            accent("x: " + bPos.getX() + ", z: " + bPos.getZ()),
                            base(Text.translatable("command.locate.copy", itemString))
                        ),
                        String.format("%d ~ %d", bPos.getX(), bPos.getZ())
                    )
                ).collect(Collectors.toList())),
                highlight(".")
            ));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static List<BPos> locateLoot(Predicate<Item> item, Predicate<Item> nameEquals, int amount, BPos center, ChunkRand chunkRand, BiomeSource biomeSource, Set<RegionStructure<?, ?>> structures) {
        TerrainGenerator terrainGenerator = TerrainGenerator.of(biomeSource);

        int itemsFound = 0;
        List<BPos> positions = new ArrayList<>();
        List<Triplet<? extends RegionStructure<?, ?>, Generator, Iterator<RPos>>> structureInfo = new ArrayList<>();
        for (RegionStructure<?, ?> structure : structures) {
            Generator.GeneratorFactory<?> factory;
            if (structure instanceof RuinedPortal) {
                factory = Generators.get(RuinedPortal.class);
            } else {
                factory = Generators.get(structure.getClass());
            }
            if (factory == null) {
                continue;
            }
            Generator structureGenerator = factory.create(biomeSource.getVersion());
            if (structureGenerator.getPossibleLootItems().stream().noneMatch(nameEquals)) {
                continue;
            }

            int chunkInRegion = structure.getSpacing();
            int regionSize = chunkInRegion * 16;
            final int border = 30_000_000;
            Iterator<RPos> iterator = new SpiralIterator<>(center.toRegionPos(regionSize), new BPos(-border, 0, -border).toRegionPos(regionSize), new BPos(border, 0, border).toRegionPos(regionSize), 1, (x, y, z) -> new RPos(x, z, regionSize)).iterator();
            structureInfo.add(new Triplet<>(structure, structureGenerator, iterator));
        }

        Iterable<Triplet<? extends RegionStructure<?, ?>, Generator, Iterator<RPos>>> cycle = Iterables.cycle(structureInfo);
        cycle: for (Triplet<? extends RegionStructure<?, ?>, Generator, Iterator<RPos>> info : cycle) {
            RegionStructure<?, ?> structure = info.getFirst();
            Generator generator = info.getSecond();
            Iterator<RPos> iterator = info.getThird();

            while (iterator.hasNext()) {
                RPos rPos = iterator.next();
                CPos cPos = structure.getInRegion(biomeSource.getWorldSeed(), rPos.getX(), rPos.getZ(), chunkRand);
                if (cPos == null) {
                    continue;
                }
                if (!structure.canSpawn(cPos, biomeSource)) {
                    continue;
                }
                if (terrainGenerator != null && !structure.canGenerate(cPos, terrainGenerator)) {
                    continue;
                }
                if (!generator.generate(terrainGenerator, cPos, chunkRand)) {
                    continue;
                }
                List<ChestContent> loot = ((ILoot) structure).getLoot(WorldSeed.toStructureSeed(biomeSource.getWorldSeed()), generator, chunkRand, false);
                int matchedItems = loot.stream().mapToInt(chest -> chest.getCount(item)).sum();
                if (matchedItems == 0) {
                    continue;
                }
                itemsFound += matchedItems;
                positions.add(cPos.toBlockPos().add(9, 0, 9));
                if (itemsFound >= amount) {
                    return positions;
                }
                continue cycle;
            }
        }
        return null;
    }

    private static void sendCoordinates(BlockPos blockPos, String name) {
        if (blockPos == null) {
            Chat.print(Text.translatable("command.locate.noneFound", name));
        } else {
            Chat.print(chain(
                highlight(Text.translatable("command.locate.foundAt", name)),
                highlight(" "),
                copy(
                    hover(
                        accent("x: " + blockPos.getX() + ", y: " + blockPos.getY() + ", z: " + blockPos.getZ()),
                        base(Text.translatable("command.locate.copy", name))
                    ),
                    String.format("%d %d %d", blockPos.getX(), blockPos.getY(), blockPos.getZ())
                ),
                highlight(".")
            ));
        }
    }

    private static void sendCoordinates(BPos bPos, String name) {
        if (bPos == null) {
            sendCoordinates((BlockPos) null, name);
            return;
        }
        sendCoordinates(SharedHelpers.fromBPos(bPos), name);
    }
}
