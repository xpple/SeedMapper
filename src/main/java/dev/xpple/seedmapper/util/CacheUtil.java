package dev.xpple.seedmapper.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.xpple.seedmapper.util.maps.SimpleOreMap;
import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.mcutils.block.Block;
import kaptainwutax.mcutils.block.Blocks;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.rand.seed.WorldSeed;
import kaptainwutax.mcutils.util.data.Pair;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.util.pos.CPos;
import kaptainwutax.mcutils.version.MCVersion;
import kaptainwutax.terrainutils.TerrainGenerator;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class CacheUtil {

    private static final LoadingCache<Pair<CPos, TerrainGenerator>, Map<BPos, Block>> oresForChunk = CacheBuilder.newBuilder()
            .build(new CacheLoader<>() {
                @Override
                public Map<BPos, Block> load(Pair<CPos, TerrainGenerator> key) throws ExecutionException {
                    final CPos cPos = key.getFirst();
                    final TerrainGenerator terrainGenerator = key.getSecond();
                    final Biome biome = terrainGenerator.getBiomeSource().getBiome((cPos.getX() << 4) + 8, 0, (cPos.getZ() << 4) + 8);
                    final MCVersion mcVersion = key.getSecond().getVersion();

                    final Map<BPos, Block> generatedOres = new HashMap<>();
                    SimpleOreMap.getForVersion(mcVersion).values().stream()
                            .filter(oreDecorator -> oreDecorator.isValidDimension(terrainGenerator.getDimension()))
                            .sorted(Comparator.comparingInt(oreDecorator -> oreDecorator.getSalt(biome)))
                            .forEachOrdered(oreDecorator -> {
                                if (!oreDecorator.canSpawn(cPos.getX(), cPos.getZ(), terrainGenerator.getBiomeSource())) {
                                    return;
                                }
                                oreDecorator.generate(WorldSeed.toStructureSeed(terrainGenerator.getWorldSeed()), cPos.getX(), cPos.getZ(), biome, new ChunkRand(), terrainGenerator).positions
                                        .forEach(bPos -> {
                                            Block block = generatedOres.get(bPos);
                                            if (block != null) {
                                                if (!oreDecorator.getReplaceBlocks(biome).contains(block)) {
                                                    return;
                                                }
                                            }
                                            if (block != null && block.equals(Blocks.DEEPSLATE)) {
                                                generatedOres.put(bPos, toDeepslateVariant(oreDecorator.getOreBlock(biome)));
                                            } else {
                                                generatedOres.put(bPos, oreDecorator.getOreBlock(biome));
                                            }
                                        });
                            });
                    return generatedOres;
                }
            });

    private static final LoadingCache<Pair<CPos, TerrainGenerator>, Map<BPos, Block>> blocksForChunk = CacheBuilder.newBuilder()
            .build(new CacheLoader<>() {
                @Override
                public Map<BPos, Block> load(Pair<CPos, TerrainGenerator> key) throws ExecutionException {
                    final CPos cPos = key.getFirst();
                    final TerrainGenerator terrainGenerator = key.getSecond();

                    final int cPosBeginX = cPos.getX() << 4;
                    final int cPosEndX = cPosBeginX + 16;
                    final int cPosBeginZ = cPos.getZ() << 4;
                    final int cPosEndZ = cPosBeginZ + 16;

                    final Map<BPos, Block> generatedBlocks = new HashMap<>();
                    for (int x = cPosBeginX; x < cPosEndX; x++) {
                        for (int z = cPosBeginZ; z < cPosEndZ; z++) {
                            final Block[] column = terrainGenerator.getBiomeColumnAt(x, z);
                            for (int y = 0; y < column.length; y++) {
                                generatedBlocks.put(new BPos(x, y, z), column[y]);
                            }
                        }
                    }
                    generatedBlocks.putAll(getOresForChunk(cPos, terrainGenerator));
                    return generatedBlocks;
                }
            });

    private static Block toDeepslateVariant(Block block) {
        if (Blocks.COAL_ORE.equals(block)) {
            return Blocks.DEEPSLATE_COAL_ORE;
        } else if (Blocks.COPPER_ORE.equals(block)) {
            return Blocks.DEEPSLATE_COPPER_ORE;
        } else if (Blocks.DIAMOND_ORE.equals(block)) {
            return Blocks.DEEPSLATE_DIAMOND_ORE;
        } else if (Blocks.EMERALD_ORE.equals(block)) {
            return Blocks.DEEPSLATE_EMERALD_ORE;
        } else if (Blocks.GOLD_ORE.equals(block)) {
            return Blocks.DEEPSLATE_COAL_ORE;
        } else if (Blocks.IRON_ORE.equals(block)) {
            return Blocks.DEEPSLATE_IRON_ORE;
        } else if (Blocks.LAPIS_ORE.equals(block)) {
            return Blocks.DEEPSLATE_LAPIS_ORE;
        } else if (Blocks.REDSTONE_ORE.equals(block)) {
            return Blocks.DEEPSLATE_REDSTONE_ORE;
        }
        return block;
    }

    public static Map<BPos, Block> getOresForChunk(CPos cPos, TerrainGenerator terrainGenerator) throws ExecutionException {
        return oresForChunk.get(new Pair<>(cPos, terrainGenerator));
    }

    public static Map<BPos, Block> getBlocksForChunk(CPos cPos, TerrainGenerator terrainGenerator) throws ExecutionException {
        return blocksForChunk.get(new Pair<>(cPos, terrainGenerator));
    }
}
