package dev.xpple.seedmapper.command.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.SharedExceptions;
import dev.xpple.seedmapper.util.chat.Chat;
import dev.xpple.seedmapper.util.config.Config;
import dev.xpple.seedmapper.util.maps.SimpleFeatureMap;
import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.biomeutils.biome.Biomes;
import kaptainwutax.biomeutils.source.BiomeSource;
import kaptainwutax.featureutils.Feature;
import kaptainwutax.featureutils.misc.SlimeChunk;
import kaptainwutax.featureutils.structure.Mineshaft;
import kaptainwutax.featureutils.structure.RegionStructure;
import kaptainwutax.featureutils.structure.Stronghold;
import kaptainwutax.featureutils.structure.Structure;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.state.Dimension;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.util.pos.CPos;
import kaptainwutax.mcutils.version.MCVersion;
import kaptainwutax.terrainutils.TerrainGenerator;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static dev.xpple.seedmapper.SeedMapper.CLIENT;
import static dev.xpple.seedmapper.util.chat.ChatBuilder.*;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;
import static net.minecraft.command.CommandSource.suggestMatching;

public class LocateCommand extends ClientCommand implements SharedExceptions {

    @Override
    protected void register() {
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
                                        .suggests(((context, builder) -> suggestMatching(context.getSource().getRegistryManager().get(Registry.STRUCTURE_FEATURE_KEY).getIds().stream().map(Identifier::getPath), builder)))
                                        .executes(ctx -> locateStructure(ctx.getSource(), getString(ctx, "structure")))
                                        .then(argument("version", word())
                                                .suggests((context, builder) -> suggestMatching(Arrays.stream(MCVersion.values()).map(mcVersion -> mcVersion.name), builder))
                                                .executes(ctx -> locateStructure(ctx.getSource(), getString(ctx, "structure"), getString(ctx, "version"))))))
                        .then(literal("slimechunk")
                                .executes(ctx -> locateSlimeChunk(ctx.getSource()))
                                .then(argument("version", word())
                                        .suggests((context, builder) -> suggestMatching(Arrays.stream(MCVersion.values()).map(mcVersion -> mcVersion.name), builder))
                                        .executes(ctx -> locateSlimeChunk(ctx.getSource(), getString(ctx, "version"))))));
    }

    @Override
    protected String rootLiteral() {
        return "locate";
    }

    private static int locateBiome(FabricClientCommandSource source, String biomeName) throws CommandSyntaxException {
        return locateBiome(source, biomeName, CLIENT.getGame().getVersion().getName());
    }

    private static int locateBiome(FabricClientCommandSource source, String biomeName, String version) throws CommandSyntaxException {
        long seed;
        String key = CLIENT.getNetworkHandler().getConnection().getAddress().toString();
        if (Config.getSeeds().containsKey(key)) {
            seed = Config.getSeeds().get(key);
        } else {
            JsonElement element = Config.get("seed");
            if (element instanceof JsonNull) {
                throw NULL_POINTER_EXCEPTION.create("seed");
            }
            seed = element.getAsLong();
        }
        String dimensionPath = CLIENT.world.getRegistryKey().getValue().getPath();
        Dimension dimension = Dimension.fromString(dimensionPath);
        if (dimension == null) {
            throw DIMENSION_NOT_SUPPORTED_EXCEPTION.create(dimensionPath);
        }
        MCVersion mcVersion = MCVersion.fromString(version);
        if (mcVersion == null) {
            throw VERSION_NOT_FOUND_EXCEPTION.create(version);
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

        BlockPos center = CLIENT.player.getBlockPos();
        BPos biomePos = locateBiome(desiredBiome, center.getX(), center.getZ(), 6400, 8, biomeSource);

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

    private static BPos locateBiome(Biome biome, int centerX, int centerZ, int radius, int increment, BiomeSource biomeSource) throws CommandSyntaxException {
        if (biome.getDimension() != biomeSource.getDimension()) {
            throw INVALID_DIMENSION_EXCEPTION.create();
        }
        if (biome.equals(biomeSource.getBiome(centerX, 0, centerZ))) {
            return new BPos(centerX, 0, centerZ);
        }

        int x = centerX;
        int z = centerZ;

        float n = 1;
        int floorN = 1;
        for (int i = 0; floorN / 2 < radius; i++, n += 0.5) {
            floorN = (int) Math.floor(n);
            for (int j = 0; j < floorN; j++) {
                switch (i % 4) {
                    case 0 -> z += increment;
                    case 1 -> x += increment;
                    case 2 -> z -= increment;
                    case 3 -> x -= increment;
                }
                if (biome.equals(biomeSource.getBiome(x, 0, z))) {
                    return new BPos(x, 0, z);
                }
            }
        }
        return null;
    }

    private static int locateStructure(FabricClientCommandSource source, String structure) throws CommandSyntaxException {
        return locateStructure(source, structure, CLIENT.getGame().getVersion().getName());
    }

    private static int locateStructure(FabricClientCommandSource source, String structureName, String version) throws CommandSyntaxException {
        long seed;
        String key = CLIENT.getNetworkHandler().getConnection().getAddress().toString();
        if (Config.getSeeds().containsKey(key)) {
            seed = Config.getSeeds().get(key);
        } else {
            JsonElement element = Config.get("seed");
            if (element instanceof JsonNull) {
                throw NULL_POINTER_EXCEPTION.create("seed");
            }
            seed = element.getAsLong();
        }
        String dimensionPath = CLIENT.world.getRegistryKey().getValue().getPath();
        Dimension dimension = Dimension.fromString(dimensionPath);
        if (dimension == null) {
            throw DIMENSION_NOT_SUPPORTED_EXCEPTION.create(dimensionPath);
        }
        MCVersion mcVersion = MCVersion.fromString(version);
        if (mcVersion == null) {
            throw VERSION_NOT_FOUND_EXCEPTION.create(version);
        }

        Feature<?, ?> desiredFeature = null;
        for (Feature<?, ?> feature : SimpleFeatureMap.getForVersion(mcVersion).values()) {
            if (feature.getName().equals(structureName)) {
                desiredFeature = feature;
                break;
            }
        }
        if (desiredFeature == null) {
            throw STRUCTURE_NOT_FOUND_EXCEPTION.create(structureName);
        }

        BiomeSource biomeSource = BiomeSource.of(dimension, mcVersion, seed);

        BlockPos center = CLIENT.player.getBlockPos();
        BPos structurePos = locateStructure((Structure<?, ?>) desiredFeature, new BPos(center.getX(), center.getY(), center.getZ()), 6400, new ChunkRand(seed), biomeSource, TerrainGenerator.of(biomeSource), dimension.getId());

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

    private static BPos locateStructure(Structure<?, ?> structure, BPos currentPos, int radius, ChunkRand chunkRand, BiomeSource source, TerrainGenerator terrainGenerator, int dimCoeff) throws CommandSyntaxException {
        if (!structure.isValidDimension(source.getDimension())) {
            throw INVALID_DIMENSION_EXCEPTION.create();
        }
        if (structure instanceof RegionStructure<?, ?> regionStructure) {
            int chunkInRegion = regionStructure.getSpacing();
            int regionSize = chunkInRegion * 16;

            int centerX = currentPos.toRegionPos(regionSize).getX();
            int centerZ = currentPos.toRegionPos(regionSize).getZ();
            int x = centerX;
            int z = centerZ;

            float n = 1;
            int floorN = 1;
            for (int i = 0; floorN / 2 < radius; i++, n += 0.5) {
                floorN = (int) Math.floor(n);
                for (int j = 0; j < floorN; j++) {
                    switch (i % 4) {
                        case 0 -> z++;
                        case 1 -> x++;
                        case 2 -> z--;
                        case 3 -> x--;
                    }
                    CPos cpos = regionStructure.getInRegion(source.getWorldSeed(), x, z, chunkRand);
                    if (cpos == null) {
                        continue;
                    }
                    if ((regionStructure.canSpawn(cpos, source)) && (terrainGenerator == null || regionStructure.canGenerate(cpos, terrainGenerator))) {
                        BPos dimPos = cpos.toBlockPos().add(9, 0, 9);
                        return new BPos(dimPos.getX() << dimCoeff, 0, dimPos.getZ() << dimCoeff);
                    }
                }
            }
        } else {
            if (structure instanceof Stronghold strongholdStructure) {
                CPos currentChunkPos = currentPos.toChunkPos();
                int squaredDistance = Integer.MAX_VALUE;
                CPos closest = new CPos(0, 0);
                for (CPos stronghold : strongholdStructure.getAllStarts(source, chunkRand)) {
                    int newSquaredDistance = (currentChunkPos.getX() - stronghold.getX()) * (currentChunkPos.getX() - stronghold.getX()) + (currentChunkPos.getX() - stronghold.getX()) * (currentChunkPos.getZ() - stronghold.getZ());
                    if (newSquaredDistance < squaredDistance) {
                        squaredDistance = newSquaredDistance;
                        closest = stronghold;
                    }
                }
                BPos dimPos = closest.toBlockPos().add(9, 0, 9);
                return new BPos(dimPos.getX() << dimCoeff, 0, dimPos.getZ() << dimCoeff);
            } else if (structure instanceof Mineshaft mineshaft) {
                int x = currentPos.getX() >> 4;
                int z = currentPos.getZ() >> 4;

                float n = 1;
                int floorN = 1;
                for (int i = 0; floorN / 2 < radius; i++, n += 0.5) {
                    floorN = (int) Math.floor(n);
                    for (int j = 0; j < floorN; j++) {
                        switch (i % 4) {
                            case 0 -> z++;
                            case 1 -> x++;
                            case 2 -> z--;
                            case 3 -> x--;
                        }
                        Feature.Data<Mineshaft> data = mineshaft.at(x, z);
                        if (data.testStart(source.getWorldSeed(), chunkRand) && data.testBiome(source) && data.testGenerate(terrainGenerator)) {
                            return new BPos(x << 4, 0, z << 4);
                        }
                    }
                }
            }
        }
        return null;
    }

    private static int locateSlimeChunk(FabricClientCommandSource source) throws CommandSyntaxException {
        return locateSlimeChunk(source, CLIENT.getGame().getVersion().getName());
    }

    private static int locateSlimeChunk(FabricClientCommandSource source, String version) throws CommandSyntaxException {
        long seed;
        String key = CLIENT.getNetworkHandler().getConnection().getAddress().toString();
        if (Config.getSeeds().containsKey(key)) {
            seed = Config.getSeeds().get(key);
        } else {
            JsonElement element = Config.get("seed");
            if (element instanceof JsonNull) {
                throw NULL_POINTER_EXCEPTION.create("seed");
            }
            seed = element.getAsLong();
        }
        String dimensionPath = CLIENT.world.getRegistryKey().getValue().getPath();
        Dimension dimension = Dimension.fromString(dimensionPath);
        if (dimension == null) {
            throw DIMENSION_NOT_SUPPORTED_EXCEPTION.create(dimensionPath);
        }
        MCVersion mcVersion = MCVersion.fromString(version);
        if (mcVersion == null) {
            throw VERSION_NOT_FOUND_EXCEPTION.create(version);
        }

        BlockPos center = CLIENT.player.getBlockPos();

        CPos slimeChunkPos = locateSlimeChunk(new SlimeChunk(mcVersion), center.getX(), center.getZ(), 6400, seed, new ChunkRand(seed), dimension);
        if (slimeChunkPos == null) {
            Chat.print("", new TranslatableText("command.locate.feature.slimeChunk.noneFound"));
        } else {
            int x = slimeChunkPos.getX() << 4;
            int z = slimeChunkPos.getZ() << 4;
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

    private static CPos locateSlimeChunk(SlimeChunk slimeChunk, int centerX, int centerZ, int radius, long seed, ChunkRand rand, Dimension dimension) throws CommandSyntaxException {
        if (!slimeChunk.isValidDimension(dimension)) {
            throw INVALID_DIMENSION_EXCEPTION.create();
        }
        int x = centerX >> 4;
        int z = centerZ >> 4;

        float n = 1;
        int floorN = 1;
        for (int i = 0; floorN / 2 < radius; i++, n += 0.5) {
            floorN = (int) Math.floor(n);
            for (int j = 0; j < floorN; j++) {
                switch (i % 4) {
                    case 0 -> z++;
                    case 1 -> x++;
                    case 2 -> z--;
                    case 3 -> x--;
                }
                SlimeChunk.Data data = slimeChunk.at(x, z, true);
                if (data.testStart(seed, rand)) {
                    return new CPos(x, z);
                }
            }
        }
        return null;
    }
}
