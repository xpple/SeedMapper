package dev.xpple.seedmapper.command.commands;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.seedfinding.mcbiome.biome.Biome;
import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.rand.seed.WorldSeed;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.util.data.Pair;
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
import com.seedfinding.mcfeature.misc.SlimeChunk;
import com.seedfinding.mcfeature.structure.*;
import com.seedfinding.mcfeature.structure.generator.Generator;
import com.seedfinding.mcfeature.structure.generator.Generators;
import com.seedfinding.mcterrain.TerrainGenerator;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.command.SharedHelpers;
import dev.xpple.seedmapper.util.chat.Chat;
import dev.xpple.seedmapper.util.features.FeatureFactory;
import dev.xpple.seedmapper.util.features.Features;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static dev.xpple.seedmapper.command.arguments.BiomeArgumentType.biome;
import static dev.xpple.seedmapper.command.arguments.BiomeArgumentType.getBiome;
import static dev.xpple.seedmapper.command.arguments.DecoratorFactoryArgumentType.decoratorFactory;
import static dev.xpple.seedmapper.command.arguments.DecoratorFactoryArgumentType.getDecoratorFactory;
import static dev.xpple.seedmapper.command.arguments.EnchantedItemPredicateArgumentType.enchantedItem;
import static dev.xpple.seedmapper.command.arguments.EnchantedItemPredicateArgumentType.getEnchantedItem;
import static dev.xpple.seedmapper.command.arguments.StructureFactoryArgumentType.getStructureFactory;
import static dev.xpple.seedmapper.command.arguments.StructureFactoryArgumentType.structureFactory;
import static dev.xpple.seedmapper.util.chat.ChatBuilder.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class LocateCommand extends ClientCommand implements SharedHelpers.Exceptions {

    @Override
    protected LiteralCommandNode<FabricClientCommandSource> build(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        return dispatcher.register(literal(this.getRootLiteral())
            .then(literal("biome")
                .then(argument("biome", biome())
                    .executes(ctx -> locateBiome(CustomClientCommandSource.of(ctx.getSource()), getBiome(ctx, "biome")))))
            .then(literal("feature")
                .then(literal("structure")
                    .then(argument("structure", structureFactory())
                        .executes(ctx -> locateStructure(CustomClientCommandSource.of(ctx.getSource()), getStructureFactory(ctx, "structure")))))
                .then(literal("decorator")
                    .then(argument("decorator", decoratorFactory())
                        .executes(ctx -> locateDecorator(CustomClientCommandSource.of(ctx.getSource()), getDecoratorFactory(ctx, "decorator")))))
                .then(literal("slimechunk")
                    .executes(ctx -> locateSlimeChunk(CustomClientCommandSource.of(ctx.getSource())))))
            .then(literal("loot")
                .then(argument("amount", integer(1))
                    .then(argument("item", enchantedItem().loot())
                        .executes(ctx -> locateLoot(CustomClientCommandSource.of(ctx.getSource()), getInteger(ctx, "amount"), getEnchantedItem(ctx, "item")))))));
    }

    @Override
    protected String rootLiteral() {
        return "locate";
    }

    private static int locateBiome(CustomClientCommandSource source, Biome biome) throws CommandSyntaxException {
        SharedHelpers helpers = new SharedHelpers(source);

        BiomeSource biomeSource = BiomeSource.of(helpers.dimension, helpers.mcVersion, helpers.seed);
        if (biome.getDimension() != biomeSource.getDimension()) {
            throw INVALID_DIMENSION_EXCEPTION.create();
        }

        BlockPos center = BlockPos.ofFloored(source.getPosition());
        BPos biomePos = locateBiome(biome::equals, new BPos(center.getX(), 0, center.getZ()), 6400, 8, biomeSource);

        if (biomePos == null) {
            Chat.print(Text.translatable("command.locate.biome.noneFound", biome.getName()));
        } else {
            Chat.print(chain(
                highlight(Text.translatable("command.locate.biome.foundAt", biome.getName())),
                highlight(" "),
                copy(
                    hover(
                        accent("x: " + biomePos.getX() + ", z: " + biomePos.getZ()),
                        base(Text.translatable("command.locate.biome.copy", biome.getName()))
                    ),
                    String.format("%d ~ %d", biomePos.getX(), biomePos.getZ())
                ),
                highlight(".")
            ));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static BPos locateBiome(Predicate<Biome> predicate, BPos center, int radius, int increment, BiomeSource biomeSource) {
        SpiralIterator<BPos> spiralIterator = new SpiralIterator<>(center, new BPos(radius, 0, radius), increment, BPos::new);
        return StreamSupport.stream(spiralIterator.spliterator(), false)
            .filter(bPos -> predicate.test(biomeSource.getBiome(bPos)))
            .findAny().orElse(null);
    }

    private static int locateStructure(CustomClientCommandSource source, FeatureFactory<? extends Structure<?, ?>> structureFactory) throws CommandSyntaxException {
        SharedHelpers helpers = new SharedHelpers(source);

        final Structure<?, ?> structure = structureFactory.create(helpers.mcVersion);

        BiomeSource biomeSource = BiomeSource.of(helpers.dimension, helpers.mcVersion, helpers.seed);
        if (!structure.isValidDimension(biomeSource.getDimension())) {
            throw INVALID_DIMENSION_EXCEPTION.create();
        }

        BlockPos center = BlockPos.ofFloored(source.getPosition());
        BPos structurePos = locateStructure(structure, new BPos(center.getX(), center.getY(), center.getZ()), 6400, new ChunkRand(), biomeSource, TerrainGenerator.of(biomeSource));

        if (structurePos == null) {
            Chat.print(Text.translatable("command.locate.feature.structure.noneFound", structure.getName()));
        } else {
            Chat.print(chain(
                highlight(Text.translatable("command.locate.feature.structure.foundAt", structure.getName())),
                highlight(" "),
                copy(
                    hover(
                        accent("x: " + structurePos.getX() + ", z: " + structurePos.getZ()),
                        base(Text.translatable("command.locate.feature.structure.copy", structure.getName()))
                    ),
                    String.format("%d ~ %d", structurePos.getX(), structurePos.getZ())
                ),
                highlight(".")
            ));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static BPos locateStructure(Structure<?, ?> structure, BPos center, int radius, ChunkRand chunkRand, BiomeSource source, TerrainGenerator terrainGenerator) {
        if (structure instanceof RegionStructure<?, ?> regionStructure) {
            int chunkInRegion = regionStructure.getSpacing();
            int regionSize = chunkInRegion * 16;

            final int border = 30_000_000;
            SpiralIterator<RPos> spiralIterator = new SpiralIterator<>(center.toRegionPos(regionSize), new BPos(-border, 0, -border).toRegionPos(regionSize), new BPos(border, 0, border).toRegionPos(regionSize), 1, (x, y, z) -> new RPos(x, z, regionSize));
            return StreamSupport.stream(spiralIterator.spliterator(), false)
                .map(rPos -> regionStructure.getInRegion(source.getWorldSeed(), rPos.getX(), rPos.getZ(), chunkRand))
                .filter(Objects::nonNull)
                .filter(cPos -> (regionStructure.canSpawn(cPos, source)) && (terrainGenerator == null || regionStructure.canGenerate(cPos, terrainGenerator)))
                .findAny().map(cPos -> cPos.toBlockPos().add(9, 0, 9)).orElse(null);
        } else {
            if (structure instanceof Stronghold strongholdStructure) {
                CPos currentChunkPos = center.toChunkPos();
                int squaredDistance = Integer.MAX_VALUE;
                CPos closest = new CPos(0, 0);
                for (CPos stronghold : strongholdStructure.getAllStarts(source, chunkRand)) {
                    int newSquaredDistance = (currentChunkPos.getX() - stronghold.getX()) * (currentChunkPos.getX() - stronghold.getX()) + (currentChunkPos.getZ() - stronghold.getZ()) * (currentChunkPos.getZ() - stronghold.getZ());
                    if (newSquaredDistance < squaredDistance) {
                        squaredDistance = newSquaredDistance;
                        closest = stronghold;
                    }
                }
                BPos dimPos = closest.toBlockPos().add(9, 0, 9);
                return new BPos(dimPos.getX(), 0, dimPos.getZ());
            } else if (structure instanceof Mineshaft mineshaft) {
                SpiralIterator<CPos> spiralIterator = new SpiralIterator<>(new CPos(center.getX() >> 4, center.getZ() >> 4), new CPos(radius, radius), (x, y, z) -> new CPos(x, z));

                return StreamSupport.stream(spiralIterator.spliterator(), false)
                    .filter(cPos -> {
                        Feature.Data<Mineshaft> data = mineshaft.at(cPos.getX(), cPos.getZ());
                        return data.testStart(source.getWorldSeed(), chunkRand) && data.testBiome(source) && data.testGenerate(terrainGenerator);
                    })
                    .findAny().map(cPos -> cPos.toBlockPos().add(9, 0, 9)).orElse(null);
            }
        }
        return null;
    }

    private static int locateDecorator(CustomClientCommandSource source, FeatureFactory<? extends Decorator<?, ?>> decoratorFactory) throws CommandSyntaxException {
        SharedHelpers helpers = new SharedHelpers(source);

        final Decorator<?, ?> decorator = decoratorFactory.create(helpers.mcVersion);

        BiomeSource biomeSource = BiomeSource.of(helpers.dimension, helpers.mcVersion, helpers.seed);
        if (!decorator.isValidDimension(biomeSource.getDimension())) {
            throw INVALID_DIMENSION_EXCEPTION.create();
        }

        BlockPos center = BlockPos.ofFloored(source.getPosition());
        BPos decoratorPos = locateDecorator(decorator, new BPos(center.getX(), center.getY(), center.getZ()).toChunkPos(), 6400, biomeSource, new ChunkRand());

        if (decoratorPos == null) {
            Chat.print(Text.translatable("command.locate.feature.decorator.noneFound", decorator.getName()));
        } else {
            Chat.print(chain(
                highlight(Text.translatable("command.locate.feature.decorator.foundAt", decorator.getName())),
                highlight(" "),
                copy(
                    hover(
                        accent("x: " + decoratorPos.getX() + ", z: " + decoratorPos.getZ()),
                        base(Text.translatable("command.locate.feature.decorator.copy", decorator.getName()))
                    ),
                    String.format("%d ~ %d", decoratorPos.getX(), decoratorPos.getZ())
                ),
                highlight(".")
            ));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static BPos locateDecorator(Decorator<?, ?> decorator, CPos center, int radius, BiomeSource source, ChunkRand chunkRand) {
        long structureSeed = WorldSeed.toStructureSeed(source.getWorldSeed());
        if (decorator instanceof DesertWell desertWell) {
            SpiralIterator<CPos> spiralIterator = new SpiralIterator<>(center, new CPos(radius, radius), (x, y, z) -> new CPos(x, z));
            return StreamSupport.stream(spiralIterator.spliterator(), false)
                .filter(cPos -> {
                    int chunkX = cPos.getX();
                    int chunkZ = cPos.getZ();
                    DesertWell.Data data = desertWell.getData(structureSeed, chunkX, chunkZ, chunkRand);
                    return data != null && desertWell.canSpawn(chunkX, chunkZ, source);
                })
                .findAny().map(cPos -> cPos.toBlockPos().add(9, 0, 9)).orElse(null);
        } else if (decorator instanceof EndGateway endGateway) {
            SpiralIterator<CPos> spiralIterator = new SpiralIterator<>(center, new CPos(radius, radius), (x, y, z) -> new CPos(x, z));
            for (CPos cPos : spiralIterator) {
                int chunkX = cPos.getX();
                int chunkZ = cPos.getZ();
                EndGateway.Data data = endGateway.getData(structureSeed, chunkX, chunkZ, chunkRand);
                if (data == null) {
                    continue;
                }
                if (endGateway.canSpawn(chunkX, chunkZ, source)) {
                    return new BPos(data.blockX, 0, data.blockZ);
                }
            }
        }
        return null;
    }

    private static int locateSlimeChunk(CustomClientCommandSource source) throws CommandSyntaxException {
        SharedHelpers helpers = new SharedHelpers(source);

        BlockPos center = BlockPos.ofFloored(source.getPosition());
        CPos centerChunk = new CPos(center.getX() >> 4, center.getZ() >> 4);

        CPos slimeChunkPos = locateSlimeChunk(new SlimeChunk(helpers.mcVersion), centerChunk, 6400, helpers.seed, new ChunkRand(), helpers.dimension);
        if (slimeChunkPos == null) {
            Chat.print(Text.translatable("command.locate.feature.slimeChunk.noneFound"));
        } else {
            int x = (slimeChunkPos.getX() << 4) + 9;
            int z = (slimeChunkPos.getZ() << 4) + 9;
            Chat.print(chain(
                highlight(Text.translatable("command.locate.feature.slimeChunk.foundAt")),
                highlight(" "),
                copy(
                    hover(
                        accent("x: " + x + ", z: " + z),
                        base(Text.translatable("command.locate.feature.slimeChunk.copy"))
                    ),
                    String.format("%d ~ %d", x, z)
                ),
                highlight(" ("),
                highlight(Text.translatable("command.locate.feature.slimeChunk.chunk")),
                highlight(" "),
                copy(
                    hover(
                        accent(slimeChunkPos.getX() + " " + slimeChunkPos.getZ()),
                        base(Text.translatable("command.locate.feature.slimeChunk.copyChunk"))
                    ),
                    String.format("%d %d", slimeChunkPos.getX(), slimeChunkPos.getZ())
                ),
                highlight(").")
            ));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static CPos locateSlimeChunk(SlimeChunk slimeChunk, CPos centerChunk, int radius, long seed, ChunkRand rand, Dimension dimension) throws CommandSyntaxException {
        if (!slimeChunk.isValidDimension(dimension)) {
            throw INVALID_DIMENSION_EXCEPTION.create();
        }
        SpiralIterator<CPos> spiralIterator = new SpiralIterator<>(centerChunk, new CPos(radius, radius), (x, y, z) -> new CPos(x, z));
        return StreamSupport.stream(spiralIterator.spliterator(), false)
            .filter(cPos -> {
                SlimeChunk.Data data = slimeChunk.at(cPos.getX(), cPos.getZ(), true);
                return data.testStart(seed, rand);
            })
            .findAny().orElse(null);
    }

    private static int locateLoot(CustomClientCommandSource source, int amount, Pair<String, Predicate<Item>> item) throws CommandSyntaxException {
        SharedHelpers helpers = new SharedHelpers(source);

        String itemString = item.getFirst();

        Set<RegionStructure<?, ?>> lootableStructures = Features.getStructuresForVersion(helpers.mcVersion).stream()
            .filter(structure -> structure instanceof ILoot)
            .filter(structure -> structure.isValidDimension(helpers.dimension))
            .map(structure -> (RegionStructure<?, ?>) structure)
            .collect(Collectors.toSet());

        BiomeSource biomeSource = BiomeSource.of(helpers.dimension, helpers.mcVersion, helpers.seed);

        BlockPos center = BlockPos.ofFloored(source.getPosition());

        List<BPos> lootPositions = locateLoot(item.getSecond(), i -> i.getName().equals(itemString), amount, new BPos(center.getX(), center.getY(), center.getZ()), new ChunkRand(), biomeSource, lootableStructures);
        if (lootPositions == null || lootPositions.isEmpty()) {
            Chat.print(Text.translatable("command.locate.loot.noneFound", itemString));
        } else {
            Chat.print(chain(
                highlight(Text.translatable("command.locate.loot.foundAt", amount, itemString)),
                highlight(" "),
                join(highlight(", "), lootPositions.stream().map(bPos ->
                    copy(
                        hover(
                            accent("x: " + bPos.getX() + ", z: " + bPos.getZ()),
                            base(Text.translatable("command.locate.loot.copy", itemString))
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
}
