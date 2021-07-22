package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.SharedHelpers;
import dev.xpple.seedmapper.util.chat.Chat;
import dev.xpple.seedmapper.util.maps.SimpleStructureMap;
import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.biomeutils.biome.Biomes;
import kaptainwutax.biomeutils.source.BiomeSource;
import kaptainwutax.featureutils.Feature;
import kaptainwutax.featureutils.loot.ChestContent;
import kaptainwutax.featureutils.loot.ILoot;
import kaptainwutax.featureutils.loot.item.Item;
import kaptainwutax.featureutils.loot.item.Items;
import kaptainwutax.featureutils.misc.SlimeChunk;
import kaptainwutax.featureutils.structure.*;
import kaptainwutax.featureutils.structure.generator.Generator;
import kaptainwutax.featureutils.structure.generator.Generators;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.rand.seed.WorldSeed;
import kaptainwutax.mcutils.state.Dimension;
import kaptainwutax.mcutils.util.data.SpiralIterator;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.util.pos.CPos;
import kaptainwutax.mcutils.util.pos.RPos;
import kaptainwutax.mcutils.version.MCVersion;
import kaptainwutax.terrainutils.TerrainGenerator;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static dev.xpple.seedmapper.SeedMapper.CLIENT;
import static dev.xpple.seedmapper.util.chat.ChatBuilder.*;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;
import static net.minecraft.command.CommandSource.suggestMatching;

public class LocateCommand extends ClientCommand implements SharedHelpers.Exceptions {

    @Override
    protected void register() {
        String[] lootableItems = new String[]{"diamond_pickaxe", "diamond_sword", "golden_horse_armor", "string", "bell", "poisonous_potato", "gold_ingot", "iron_boots", "iron_leggings", "flint_and_steel", "beetroot_seeds", "carrot", "gold_block", "gold_nugget", "bamboo", "diamond_horse_armor", "paper", "golden_hoe", "gunpowder", "lapis_lazuli", "iron_horse_armor", "diamond_chestplate", "diamond_shovel", "golden_chestplate", "golden_leggings", "golden_carrot", "filled_map", "coal", "diamond_boots", "compass", "golden_boots", "cooked_salmon", "iron_shovel", "suspicious_stew", "golden_pickaxe", "emerald", "golden_shovel", "sand", "leather_boots", "heart_of_the_sea", "iron_nugget", "wheat", "golden_sword", "light_weighted_pressure_plate", "pumpkin", "iron_pickaxe", "flint", "golden_axe", "potato", "cooked_cod", "map", "feather", "leather_chestplate", "leather_leggings", "enchanted_book", "iron_chestplate", "moss_block", "book", "clock", "iron_sword", "golden_apple", "enchanted_golden_apple", "fire_charge", "spider_eye", "bone", "prismarine_crystals", "obsidian", "glistering_melon_slice", "rotten_flesh", "experience_bottle", "diamond", "golden_helmet", "iron_helmet", "diamond_helmet", "leather_helmet", "iron_ingot", "saddle", "tnt", "diamond_leggings", "diamond_pickaxe", "diamond_sword", "golden_horse_armor", "string", "bell", "poisonous_potato", "gold_ingot", "iron_boots", "iron_leggings", "flint_and_steel", "beetroot_seeds", "carrot", "gold_block", "gold_nugget", "bamboo", "diamond_horse_armor", "paper", "golden_hoe", "gunpowder", "lapis_lazuli", "iron_horse_armor", "diamond_chestplate", "diamond_shovel", "golden_chestplate", "golden_leggings", "golden_carrot", "filled_map", "coal", "diamond_boots", "compass", "golden_boots", "cooked_salmon", "iron_shovel", "suspicious_stew", "golden_pickaxe", "emerald", "golden_shovel", "sand", "leather_boots", "heart_of_the_sea", "iron_nugget", "wheat", "golden_sword", "light_weighted_pressure_plate", "pumpkin", "iron_pickaxe", "flint", "golden_axe", "potato", "cooked_cod", "map", "feather", "leather_chestplate", "leather_leggings", "enchanted_book", "iron_chestplate", "moss_block", "book", "clock", "iron_sword", "golden_apple", "enchanted_golden_apple", "fire_charge", "spider_eye", "bone", "prismarine_crystals", "obsidian", "glistering_melon_slice", "rotten_flesh", "experience_bottle", "diamond", "golden_helmet", "iron_helmet", "diamond_helmet", "leather_helmet", "iron_ingot", "saddle", "tnt", "diamond_leggings"};
        argumentBuilder
                .then(literal("biome")
                        .then(argument("biome", word())
                                .suggests((context, builder) -> suggestMatching(context.getSource().getRegistryManager().get(Registry.BIOME_KEY).getIds().stream().map(Identifier::getPath), builder))
                                .executes(ctx -> locateBiome(ctx.getSource(), getString(ctx, "biome")))
                                .then(argument("version", word())
                                        .suggests((context, builder) -> suggestMatching(Arrays.stream(MCVersion.values()).map(mcVersion -> mcVersion.name), builder))
                                        .executes(ctx -> locateBiome(ctx.getSource(), getString(ctx, "biome"), getString(ctx, "version"))))))
                .then(literal("feature")
                        .then(literal("structure")
                                .then(argument("structure", word())
                                        .suggests((context, builder) -> suggestMatching(context.getSource().getRegistryManager().get(Registry.STRUCTURE_FEATURE_KEY).getIds().stream().map(Identifier::getPath), builder))
                                        .executes(ctx -> locateStructure(ctx.getSource(), getString(ctx, "structure")))
                                        .then(argument("version", word())
                                                .suggests((context, builder) -> suggestMatching(Arrays.stream(MCVersion.values()).map(mcVersion -> mcVersion.name), builder))
                                                .executes(ctx -> locateStructure(ctx.getSource(), getString(ctx, "structure"), getString(ctx, "version"))))))
                        .then(literal("slimechunk")
                                .executes(ctx -> locateSlimeChunk(ctx.getSource()))
                                .then(argument("version", word())
                                        .suggests((context, builder) -> suggestMatching(Arrays.stream(MCVersion.values()).map(mcVersion -> mcVersion.name), builder))
                                        .executes(ctx -> locateSlimeChunk(ctx.getSource(), getString(ctx, "version"))))))
                .then(literal("loot")
                        .then(argument("item", string())
                                .suggests((context, builder) -> suggestMatching(lootableItems, builder))
                                .executes(ctx -> locateLoot(ctx.getSource(), getString(ctx, "item")))
                                .then(argument("version", word())
                                        .suggests((context, builder) -> suggestMatching(Arrays.stream(MCVersion.values()).map(mcVersion -> mcVersion.name), builder))
                                        .executes(ctx -> locateLoot(ctx.getSource(), getString(ctx, "item"), getString(ctx, "version"))))));
    }

    @Override
    protected String rootLiteral() {
        return "locate";
    }

    private static int locateBiome(FabricClientCommandSource source, String biomeName) throws CommandSyntaxException {
        return locateBiome(source, biomeName, CLIENT.getGame().getVersion().getName());
    }

    private static int locateBiome(FabricClientCommandSource source, String biomeName, String version) throws CommandSyntaxException {
        long seed = SharedHelpers.getSeed();
        String dimensionPath = CLIENT.world.getRegistryKey().getValue().getPath();
        Dimension dimension = SharedHelpers.getDimension(dimensionPath);
        MCVersion mcVersion = SharedHelpers.getMCVersion(version);

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

        BlockPos center = CLIENT.player.getBlockPos();
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
        if (predicate.test(biomeSource.getBiome(center))) {
            return center;
        }
        SpiralIterator<BPos> spiralIterator = new SpiralIterator<>(center, new BPos(radius, 0, radius), increment, BPos::new);
        return StreamSupport.stream(spiralIterator.spliterator(), false)
                .filter(bPos -> predicate.test(biomeSource.getBiome(bPos)))
                .findAny().orElse(null);
    }

    private static int locateStructure(FabricClientCommandSource source, String structure) throws CommandSyntaxException {
        return locateStructure(source, structure, CLIENT.getGame().getVersion().getName());
    }

    private static int locateStructure(FabricClientCommandSource source, String structureName, String version) throws CommandSyntaxException {
        long seed = SharedHelpers.getSeed();
        String dimensionPath = CLIENT.world.getRegistryKey().getValue().getPath();
        Dimension dimension = SharedHelpers.getDimension(dimensionPath);
        MCVersion mcVersion = SharedHelpers.getMCVersion(version);

        Structure<?, ?> desiredFeature = null;
        for (Structure<?, ?> structure : SimpleStructureMap.getForVersion(mcVersion).values()) {
            if (structure.getName().equals(structureName)) {
                desiredFeature = structure;
                break;
            }
        }
        if (desiredFeature == null) {
            throw STRUCTURE_NOT_FOUND_EXCEPTION.create(structureName);
        }

        BiomeSource biomeSource = BiomeSource.of(dimension, mcVersion, seed);
        if (!desiredFeature.isValidDimension(biomeSource.getDimension())) {
            throw INVALID_DIMENSION_EXCEPTION.create();
        }

        BlockPos center = CLIENT.player.getBlockPos();
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

    private static int locateSlimeChunk(FabricClientCommandSource source) throws CommandSyntaxException {
        return locateSlimeChunk(source, CLIENT.getGame().getVersion().getName());
    }

    private static int locateSlimeChunk(FabricClientCommandSource source, String version) throws CommandSyntaxException {
        long seed = SharedHelpers.getSeed();
        String dimensionPath = CLIENT.world.getRegistryKey().getValue().getPath();
        Dimension dimension = SharedHelpers.getDimension(dimensionPath);
        MCVersion mcVersion = SharedHelpers.getMCVersion(version);

        BlockPos center = CLIENT.player.getBlockPos();
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
        for (CPos next : spiralIterator) {
            SlimeChunk.Data data = slimeChunk.at(next.getX(), next.getZ(), true);
            if (data.testStart(seed, rand)) {
                return next;
            }
        }
        return null;
    }

    private static int locateLoot(FabricClientCommandSource source, String itemString) throws CommandSyntaxException {
        return locateLoot(source, itemString, CLIENT.getGame().getVersion().getName());
    }

    private static int locateLoot(FabricClientCommandSource source, String itemString, String version) throws CommandSyntaxException {
        long seed = SharedHelpers.getSeed();
        String dimensionPath = CLIENT.world.getRegistryKey().getValue().getPath();
        Dimension dimension = SharedHelpers.getDimension(dimensionPath);
        MCVersion mcVersion = SharedHelpers.getMCVersion(version);

        final Item desiredItem = Items.getItems().values().stream().filter(item -> item.getName().equals(itemString)).findAny().orElse(null);

        if (desiredItem == null) {
            throw LOOT_ITEM_NOT_FOUND_EXCEPTION.create(itemString);
        }
        Set<RegionStructure<?, ?>> lootableStructures = SimpleStructureMap.getForVersion(mcVersion).values().stream()
                .filter(structure -> structure instanceof ILoot)
                .filter(structure -> structure.isValidDimension(dimension))
                .map(structure -> (RegionStructure<?, ?>) structure)
                .collect(Collectors.toSet());

        BiomeSource biomeSource = BiomeSource.of(dimension, mcVersion, seed);

        BlockPos center = CLIENT.player.getBlockPos();

        BPos lootPos = locateLoot(item -> item.getName().equals(desiredItem.getName()), new BPos(center.getX(), center.getY(), center.getZ()), new ChunkRand(), biomeSource, lootableStructures);
        if (lootPos == null) {
            Chat.print("", new TranslatableText("command.locate.loot.noneFound", desiredItem.getName()));
        } else {
            Chat.print("", chain(
                    highlight(new TranslatableText("command.locate.loot.success.0", desiredItem.getName())),
                    copy(
                            hover(
                                    accent("x: " + lootPos.getX() + ", z: " + lootPos.getZ()),
                                    base(new TranslatableText("command.locate.loot.success.1", desiredItem.getName()))
                            ),
                            String.format("%d ~ %d", lootPos.getX(), lootPos.getZ())
                    ),
                    highlight(".")
            ));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static BPos locateLoot(Predicate<Item> item, BPos center, ChunkRand chunkRand, BiomeSource biomeSource, Set<RegionStructure<?, ?>> structures) {
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
            if (structureGenerator.getPossibleLootItems().stream().noneMatch(item)) {
                continue;
            }

            int chunkInRegion = structure.getSpacing();
            int regionSize = chunkInRegion * 16;
            TerrainGenerator terrainGenerator = TerrainGenerator.of(biomeSource);
            final int border = 30_000_000;
            SpiralIterator<RPos> spiralIterator = new SpiralIterator<>(center.toRegionPos(regionSize), new BPos(-border, 0, -border).toRegionPos(regionSize), new BPos(border, 0, border).toRegionPos(regionSize), 1, (x, y, z) -> new RPos(x, z, regionSize));
            Stream<BPos> positions = StreamSupport.stream(spiralIterator.spliterator(), false)
                    .map(cPos -> structure.getInRegion(biomeSource.getWorldSeed(), cPos.getX(), cPos.getZ(), chunkRand))
                    .filter(Objects::nonNull)
                    .filter(cPos -> (structure.canSpawn(cPos, biomeSource)) && (terrainGenerator == null || structure.canGenerate(cPos, terrainGenerator)))
                    .map(cPos -> cPos.toBlockPos().add(9, 0, 9));

            BPos lootPos = positions.filter(bPos -> {
                if (!structureGenerator.generate(terrainGenerator, bPos.toChunkPos(), chunkRand)) {
                    return false;
                }
                List<ChestContent> loot = ((ILoot) structure).getLoot(WorldSeed.toStructureSeed(biomeSource.getWorldSeed()), structureGenerator, chunkRand, false);
                for (ChestContent chest : loot) {
                    if (chest.getCount(item) >= 1) {
                        return true;
                    }
                }
                return false;
            }).findAny().orElse(null);
            if (lootPos != null) {
                return lootPos;
            }
        }
        return null;
    }
}
