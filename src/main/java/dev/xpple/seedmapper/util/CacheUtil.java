package dev.xpple.seedmapper.util;

import dev.xpple.seedmapper.util.maps.SimpleOreMap;
import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.mcutils.block.Block;
import kaptainwutax.mcutils.block.Blocks;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.rand.seed.WorldSeed;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.util.pos.CPos;
import kaptainwutax.mcutils.version.MCVersion;
import kaptainwutax.terrainutils.TerrainGenerator;

import java.util.*;

public class CacheUtil {

    private static final Map<MCVersion, Map<BPos, Block>> blocksForWorld = new HashMap<>();
    private static final Map<MCVersion, Set<CPos>> cachedChunks = new HashMap<>();

    public static void generateBlocksAt(CPos centerChunk, TerrainGenerator terrainGenerator) {
        final MCVersion mcVersion = terrainGenerator.getVersion();
        final Biome biome = terrainGenerator.getBiomeSource().getBiome((centerChunk.getX() << 4) + 8, 0, (centerChunk.getZ() << 4) + 8);

        final Map<BPos, Block> versionedBlocksForWorld = blocksForWorld.computeIfAbsent(mcVersion, v -> new HashMap<>());

        final int cPosBeginX = centerChunk.getX() << 4;
        final int cPosEndX = cPosBeginX + 16;
        final int cPosBeginZ = centerChunk.getZ() << 4;
        final int cPosEndZ = cPosBeginZ + 16;

        for (int x = cPosBeginX; x < cPosEndX; x++) {
            for (int z = cPosBeginZ; z < cPosEndZ; z++) {
                final Block[] column = terrainGenerator.getBiomeColumnAt(x, z);
                final int length = column.length;
                for (int y = 0; y < 255; y++) {
                    versionedBlocksForWorld.put(new BPos(x, y, z), y >= length ? Blocks.AIR : column[y]);
                }
            }
        }


        SimpleOreMap.getForVersion(mcVersion).values().stream()
                .filter(oreDecorator -> oreDecorator.isValidDimension(terrainGenerator.getDimension()))
                .sorted(Comparator.comparingInt(oreDecorator -> oreDecorator.getSalt(biome)))
                .forEachOrdered(oreDecorator -> {
                    if (!oreDecorator.canSpawn(centerChunk.getX(), centerChunk.getZ(), terrainGenerator.getBiomeSource())) {
                        return;
                    }
                    oreDecorator.generate(WorldSeed.toStructureSeed(terrainGenerator.getWorldSeed()), centerChunk.getX(), centerChunk.getZ(), biome, new ChunkRand(), terrainGenerator).positions
                            .forEach(bPos -> {
                                Block block = versionedBlocksForWorld.get(bPos);
                                if (block != null) {
                                    if (!oreDecorator.getReplaceBlocks(biome).contains(block)) {
                                        return;
                                    }
                                }
                                if (block != null && block.equals(Blocks.DEEPSLATE)) {
                                    versionedBlocksForWorld.put(bPos, toDeepslateVariant(oreDecorator.getOreBlock(biome)));
                                } else {
                                    versionedBlocksForWorld.put(bPos, oreDecorator.getOreBlock(biome));
                                }
                            });
                });
        cachedChunks.computeIfAbsent(mcVersion, v -> new HashSet<>()).add(centerChunk);
    }

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

    public static boolean isNotCached(MCVersion mcVersion, CPos cPos) {
        Set<CPos> chunks = cachedChunks.get(mcVersion);
        return chunks == null || !chunks.contains(cPos);
    }

    public static Map<BPos, Block> getBlocksForWorld(MCVersion mcVersion) {
        return blocksForWorld.get(mcVersion);
    }
}
