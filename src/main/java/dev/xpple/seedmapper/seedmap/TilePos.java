package dev.xpple.seedmapper.seedmap;

import dev.xpple.seedmapper.util.QuartPos2;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

public record TilePos(int x, int z, BiomeScale biomeScale) {
    /// Represents {@value} blocks at biome scale `1`.
    public static final int SIZE_PIXELS = 256;

    public static TilePos fromBlockPos(BlockPos blockPos, BiomeScale biomeScale) {
        int blocksPerTile = SIZE_PIXELS * biomeScale.val;
        return new TilePos(Math.floorDiv(blockPos.getX(), blocksPerTile), Math.floorDiv(blockPos.getZ(), blocksPerTile), biomeScale);
    }

    public BlockPos toBlockPos() {
        int blocksPerTile = SIZE_PIXELS * this.biomeScale.val;
        return new BlockPos(blocksPerTile * this.x, 0, blocksPerTile * this.z);
    }

    public static TilePos fromQuartPos(QuartPos2 quartPos, BiomeScale biomeScale) {
        return new TilePos(Math.floorDiv(QuartPos.toBlock(quartPos.x()), SIZE_PIXELS * biomeScale.val), Math.floorDiv(QuartPos.toBlock(quartPos.z()), SIZE_PIXELS * biomeScale.val), biomeScale);
    }

    public static TilePos fromChunkPos(ChunkPos chunkPos, BiomeScale biomeScale) {
        return new TilePos(Math.floorDiv(SectionPos.sectionToBlockCoord(chunkPos.x()), SIZE_PIXELS * biomeScale.val), Math.floorDiv(SectionPos.sectionToBlockCoord(chunkPos.z()), SIZE_PIXELS * biomeScale.val), biomeScale);
    }

    public ChunkPos toChunkPos() {
        int chunksPerTile = SectionPos.blockToSectionCoord(SIZE_PIXELS * this.biomeScale.val);
        return new ChunkPos(this.x * chunksPerTile, this.z * chunksPerTile);
    }

    public TilePos add(TilePos tilePos) {
        assert this.biomeScale == tilePos.biomeScale;
        return this.add(tilePos.x, tilePos.z);
    }

    public TilePos add(int tileX, int tileZ) {
        return new TilePos(this.x + tileX, this.z + tileZ, this.biomeScale);
    }

    public TilePos subtract(TilePos tilePos) {
        assert this.biomeScale == tilePos.biomeScale;
        return this.add(-tilePos.x, -tilePos.z);
    }
}
