package dev.xpple.seedmapper.util;

import dev.xpple.seedmapper.seedmap.TilePos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;

public record RegionPos(int x, int z, int regionSizeChunks) {
    public static RegionPos fromBlockPos(BlockPos blockPos, int regionSizeChunks) {
        return new RegionPos(Mth.floorDiv(SectionPos.blockToSectionCoord(blockPos.getX()), regionSizeChunks), Mth.floorDiv(SectionPos.blockToSectionCoord(blockPos.getZ()), regionSizeChunks), regionSizeChunks);
    }

    public BlockPos toBlockPos() {
        return new BlockPos(SectionPos.sectionToBlockCoord(this.x * this.regionSizeChunks), 0, SectionPos.sectionToBlockCoord(this.z * this.regionSizeChunks));
    }

    public static RegionPos fromQuartPos(QuartPos2 quartPos, int regionSizeChunks) {
        return new RegionPos(Mth.floorDiv(QuartPos.toSection(quartPos.x()), regionSizeChunks), Mth.floorDiv(QuartPos.toSection(quartPos.z()), regionSizeChunks), regionSizeChunks);
    }

    public static RegionPos fromChunkPos(ChunkPos chunkPos, int regionSizeChunks) {
        return new RegionPos(Mth.floorDiv(chunkPos.x, regionSizeChunks), Mth.floorDiv(chunkPos.z, regionSizeChunks), regionSizeChunks);
    }

    public ChunkPos toChunkPos() {
        return new ChunkPos(this.x * this.regionSizeChunks, this.z * this.regionSizeChunks);
    }

    public static RegionPos fromTilePos(TilePos tilePos, int regionSize) {
        return new RegionPos(Mth.floorDiv(TilePos.TILE_SIZE_CHUNKS * tilePos.x(), regionSize), Mth.floorDiv(TilePos.TILE_SIZE_CHUNKS * tilePos.z(), regionSize), regionSize);
    }

    public RegionPos add(RegionPos regionPos) {
        checkRegionSize(regionPos.regionSizeChunks);
        return this.add(regionPos.x, regionPos.z);
    }

    public RegionPos add(int regionX, int regionZ) {
        return new RegionPos(this.x + regionX, this.z + regionZ, this.regionSizeChunks);
    }

    public RegionPos subtract(RegionPos regionPos) {
        checkRegionSize(regionPos.regionSizeChunks);
        return this.add(-regionPos.x, -regionPos.z);
    }

    private void checkRegionSize(int regionSizeChunks) {
        if (this.regionSizeChunks != regionSizeChunks) {
            throw new IllegalArgumentException("Region sizes must match (expected %d , got %d)".formatted(this.regionSizeChunks, regionSizeChunks));
        }
    }
}
