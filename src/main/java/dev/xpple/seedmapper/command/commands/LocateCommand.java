package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.command.SharedHelpers;
import dev.xpple.seedmapper.util.chat.Chat;
import dev.xpple.seedmapper.util.maps.SimpleStructureMap;
import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.biomeutils.biome.Biomes;
import kaptainwutax.biomeutils.source.BiomeSource;
import kaptainwutax.featureutils.Feature;
import kaptainwutax.featureutils.loot.ILoot;
import kaptainwutax.featureutils.loot.item.Item;
import kaptainwutax.featureutils.misc.SlimeChunk;
import kaptainwutax.featureutils.structure.*;
import kaptainwutax.featureutils.structure.generator.Generator;
import kaptainwutax.featureutils.structure.generator.Generators;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.rand.seed.WorldSeed;
import kaptainwutax.mcutils.state.Dimension;
import kaptainwutax.mcutils.util.data.Pair;
import kaptainwutax.mcutils.util.data.SpiralIterator;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.util.pos.CPos;
import kaptainwutax.mcutils.util.pos.RPos;
import kaptainwutax.mcutils.version.MCVersion;
import kaptainwutax.terrainutils.TerrainGenerator;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static dev.xpple.seedmapper.SeedMapper.CLIENT;
import static dev.xpple.seedmapper.command.arguments.EnchantedItemPredicateArgumentType.enchantedItem;
import static dev.xpple.seedmapper.command.arguments.EnchantedItemPredicateArgumentType.getEnchantedItem;
import static dev.xpple.seedmapper.util.chat.ChatBuilder.*;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;
import static net.minecraft.command.CommandSource.suggestMatching;

public class LocateCommand extends ClientCommand implements SharedHelpers.Exceptions {

    @Override
    protected void build() {
        argumentBuilder
                .then(literal("biome")
                        .then(argument("biome", word())
                                .suggests((context, builder) -> suggestMatching(context.getSource().getRegistryManager().get(Registry.BIOME_KEY).getIds().stream().map(Identifier::getPath), builder))
                                .executes(ctx -> locateBiome(CustomClientCommandSource.of(ctx.getSource()), getString(ctx, "biome")))))
                .then(literal("feature")
                        .then(literal("structure")
                                .then(argument("structure", word())
                                        .suggests((context, builder) -> suggestMatching(context.getSource().getRegistryManager().get(Registry.STRUCTURE_FEATURE_KEY).getIds().stream().map(Identifier::getPath), builder))
                                        .executes(ctx -> locateStructure(CustomClientCommandSource.of(ctx.getSource()), getString(ctx, "structure")))))
                        .then(literal("slimechunk")
                                .executes(ctx -> locateSlimeChunk(CustomClientCommandSource.of(ctx.getSource())))))
                .then(literal("loot")
                        .then(argument("amount", integer(1))
                                .then(argument("item", enchantedItem().loot())
                                        .executes(ctx -> locateLoot(CustomClientCommandSource.of(ctx.getSource()), getInteger(ctx, "amount"), getEnchantedItem(ctx, "item"))))));
    }

    @Override
    protected String rootLiteral() {
        return "locate";
    }

    private static int locateBiome(CustomClientCommandSource source, String biomeName) throws CommandSyntaxException {
        long seed = SharedHelpers.getSeed();
        String dimensionPath;
        if (source.getMeta("dimension") == null) {
            dimensionPath = source.getWorld().getRegistryKey().getValue().getPath();
        } else {
            dimensionPath = ((Identifier) source.getMeta("dimension")).getPath();
        }
        Dimension dimension = SharedHelpers.getDimension(dimensionPath);
        MCVersion mcVersion;
        if (source.getMeta("version") == null) {
            mcVersion = SharedHelpers.getMCVersion(CLIENT.getGame().getVersion().getName());
        } else {
            mcVersion = (MCVersion) source.getMeta("version");
        }

        Biome desiredBiome = null;
        for (Biome biome : Biomes.REGISTRY.values()) {
            if (biome.getName().equals(biomeName)) {
                desiredBiome = biome;
                break;
            }
        }
        if (desiredBiome == null) {
            throw BIOME_NOT_FOUND_EXCEPTION.create(biomeName);
        }
        BiomeSource biomeSource = BiomeSource.of(dimension, mcVersion, seed);
        if (desiredBiome.getDimension() != biomeSource.getDimension()) {
            throw INVALID_DIMENSION_EXCEPTION.create();
        }

        BlockPos center = new BlockPos(source.getPosition());
        BPos biomePos = locateBiome(desiredBiome::equals, new BPos(center.getX(), 0, center.getZ()), 6400, 8, biomeSource);

        if (biomePos == null) {
            Chat.print("", new TranslatableText("command.locate.biome.noneFound", biomeName));
        } else {
            Chat.print("", chain(
                    highlight(new TranslatableText("command.locate.biome.success.0", biomeName)),
                    copy(
                            hover(
                                    accent("x: " + biomePos.getX() + ", z: " + biomePos.getZ()),
                                    base(new TranslatableText("command.locate.biome.success.1", biomeName))
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

    private static int locateStructure(CustomClientCommandSource source, String structureName) throws CommandSyntaxException {
        long seed = SharedHelpers.getSeed();
        String dimensionPath;
        if (source.getMeta("dimension") == null) {
            dimensionPath = source.getWorld().getRegistryKey().getValue().getPath();
        } else {
            dimensionPath = ((Identifier) source.getMeta("dimension")).getPath();
        }
        Dimension dimension = SharedHelpers.getDimension(dimensionPath);
        MCVersion mcVersion;
        if (source.getMeta("version") == null) {
            mcVersion = SharedHelpers.getMCVersion(CLIENT.getGame().getVersion().getName());
        } else {
            mcVersion = (MCVersion) source.getMeta("version");
        }

        final Structure<?, ?> desiredFeature = SimpleStructureMap.getForVersion(mcVersion).values().stream().filter(structure -> structure.getName().equals(structureName)).findAny().orElse(null);

        if (desiredFeature == null) {
            throw STRUCTURE_NOT_FOUND_EXCEPTION.create(structureName);
        }

        BiomeSource biomeSource = BiomeSource.of(dimension, mcVersion, seed);
        if (!desiredFeature.isValidDimension(biomeSource.getDimension())) {
            throw INVALID_DIMENSION_EXCEPTION.create();
        }

        BlockPos center = new BlockPos(source.getPosition());
        BPos structurePos = locateStructure(desiredFeature, new BPos(center.getX(), center.getY(), center.getZ()), 6400, new ChunkRand(), biomeSource, TerrainGenerator.of(biomeSource));

        if (structurePos == null) {
            Chat.print("", new TranslatableText("command.locate.feature.structure.noneFound", structureName));
        } else {
            Chat.print("", chain(
                    highlight(new TranslatableText("command.locate.feature.structure.success.0", structureName)),
                    copy(
                            hover(
                                    accent("x: " + structurePos.getX() + ", z: " + structurePos.getZ()),
                                    base(new TranslatableText("command.locate.feature.structure.success.1", structureName))
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

    private static int locateSlimeChunk(CustomClientCommandSource source) throws CommandSyntaxException {
        long seed = SharedHelpers.getSeed();
        String dimensionPath;
        if (source.getMeta("dimension") == null) {
            dimensionPath = source.getWorld().getRegistryKey().getValue().getPath();
        } else {
            dimensionPath = ((Identifier) source.getMeta("dimension")).getPath();
        }
        Dimension dimension = SharedHelpers.getDimension(dimensionPath);
        MCVersion mcVersion;
        if (source.getMeta("version") == null) {
            mcVersion = SharedHelpers.getMCVersion(CLIENT.getGame().getVersion().getName());
        } else {
            mcVersion = (MCVersion) source.getMeta("version");
        }

        BlockPos center = new BlockPos(source.getPosition());
        CPos centerChunk = new CPos(center.getX() >> 4, center.getZ() >> 4);

        CPos slimeChunkPos = locateSlimeChunk(new SlimeChunk(mcVersion), centerChunk, 6400, seed, new ChunkRand(), dimension);
        if (slimeChunkPos == null) {
            Chat.print("", new TranslatableText("command.locate.feature.slimeChunk.noneFound"));
        } else {
            int x = (slimeChunkPos.getX() << 4) + 9;
            int z = (slimeChunkPos.getZ() << 4) + 9;
            Chat.print("", chain(
                    highlight(new TranslatableText("command.locate.feature.slimeChunk.success.0")),
                    copy(
                            hover(
                                    accent("x: " + x + ", z: " + z),
                                    base(new TranslatableText("command.locate.feature.slimeChunk.success.1"))
                            ),
                            String.format("%d ~ %d", x, z)
                    ),
                    highlight(new TranslatableText("command.locate.feature.slimeChunk.success.2")),
                    copy(
                            hover(
                                    accent(slimeChunkPos.getX() + " " + slimeChunkPos.getZ()),
                                    base(new TranslatableText("command.locate.feature.slimeChunk.success.3"))
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
        long seed = SharedHelpers.getSeed();
        String dimensionPath;
        if (source.getMeta("dimension") == null) {
            dimensionPath = source.getWorld().getRegistryKey().getValue().getPath();
        } else {
            dimensionPath = ((Identifier) source.getMeta("dimension")).getPath();
        }
        Dimension dimension = SharedHelpers.getDimension(dimensionPath);
        MCVersion mcVersion;
        if (source.getMeta("version") == null) {
            mcVersion = SharedHelpers.getMCVersion(CLIENT.getGame().getVersion().getName());
        } else {
            mcVersion = (MCVersion) source.getMeta("version");
        }

        String itemString = item.getFirst();

        Set<RegionStructure<?, ?>> lootableStructures = SimpleStructureMap.getForVersion(mcVersion).values().stream()
                .filter(structure -> structure instanceof ILoot)
                .filter(structure -> structure.isValidDimension(dimension))
                .map(structure -> (RegionStructure<?, ?>) structure)
                .collect(Collectors.toSet());

        BiomeSource biomeSource = BiomeSource.of(dimension, mcVersion, seed);

        BlockPos center = new BlockPos(source.getPosition());

        Set<BPos> lootPositions = locateLoot(item.getSecond(), i -> i.getName().equals(itemString), amount, new BPos(center.getX(), center.getY(), center.getZ()), new ChunkRand(), biomeSource, lootableStructures);
        if (lootPositions == null || lootPositions.isEmpty()) {
            Chat.print("", new TranslatableText("command.locate.loot.noneFound", itemString));
        } else {
            Chat.print("", chain(
                    highlight(new TranslatableText("command.locate.loot.success.0", amount, itemString)),
                    join(highlight(", "), lootPositions.stream().map(bPos ->
                            copy(
                                    hover(
                                            accent("x: " + bPos.getX() + ", z: " + bPos.getZ()),
                                            base(new TranslatableText("command.locate.loot.success.1", itemString))
                                    ),
                                    String.format("%d ~ %d", bPos.getX(), bPos.getZ())
                            )
                    ).collect(Collectors.toList())),
                    highlight(".")
            ));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static Set<BPos> locateLoot(Predicate<Item> item, Predicate<Item> nameEquals, int amount, BPos center, ChunkRand chunkRand, BiomeSource biomeSource, Set<RegionStructure<?, ?>> structures) {
        AtomicInteger itemsFound = new AtomicInteger(0);
        for (RegionStructure<?, ?> structure : structures) {
            Generator.GeneratorFactory<?> factory;
            if (structure.getName().equals("endcity")) {
                factory = Generators.get(EndCity.class);
            } else {
                factory = Generators.get(structure.getClass());
            }
            if (factory == null) {
                continue;
            }
            Generator structureGenerator = factory.create(biomeSource.getVersion());
            if (structureGenerator.getPossibleLootItems().stream().noneMatch(nameEquals)) {
                System.out.println("s");
                continue;
            }

            int chunkInRegion = structure.getSpacing();
            int regionSize = chunkInRegion * 16;
            TerrainGenerator terrainGenerator = TerrainGenerator.of(biomeSource);
            final int border = 30_000_000;
            SpiralIterator<RPos> spiralIterator = new SpiralIterator<>(center.toRegionPos(regionSize), new BPos(-border, 0, -border).toRegionPos(regionSize), new BPos(border, 0, border).toRegionPos(regionSize), 1, (x, y, z) -> new RPos(x, z, regionSize));
            return StreamSupport.stream(spiralIterator.spliterator(), false)
                    .map(cPos -> structure.getInRegion(biomeSource.getWorldSeed(), cPos.getX(), cPos.getZ(), chunkRand))
                    .filter(Objects::nonNull)
                    .filter(cPos -> (structure.canSpawn(cPos, biomeSource)) && (terrainGenerator == null || structure.canGenerate(cPos, terrainGenerator)))
                    .filter(cPos -> structureGenerator.generate(terrainGenerator, cPos, chunkRand))
                    .map(cPos -> new Pair<>(cPos, ((ILoot) structure).getLoot(WorldSeed.toStructureSeed(biomeSource.getWorldSeed()), structureGenerator, chunkRand, false).stream()
                            .mapToInt(chest -> chest.getCount(item)).sum()))
                    .filter(pair -> pair.getSecond() > 0)
                    .takeWhile(pair -> itemsFound.addAndGet(pair.getSecond()) <= amount)
                    .map(Pair::getFirst)
                    .map(cPos -> cPos.toBlockPos().add(9, 0, 9))
                    .collect(Collectors.toSet());
        }
        return null;
    }
}
