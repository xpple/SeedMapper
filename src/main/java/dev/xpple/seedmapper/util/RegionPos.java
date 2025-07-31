package dev.xpple.seedmapper.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

public record RegionPos(int x, int z, int regionSizeChunks) {
    public static RegionPos fromBlockPos(BlockPos blockPos, int regionSizeChunks) {
        return new RegionPos(SectionPos.blockToSectionCoord(blockPos.getX()) / regionSizeChunks, SectionPos.blockToSectionCoord(blockPos.getZ()) / regionSizeChunks, regionSizeChunks);
    }

    public BlockPos toBlockPos() {
        return new BlockPos(SectionPos.sectionToBlockCoord(this.x * this.regionSizeChunks), 0, SectionPos.sectionToBlockCoord(this.z * this.regionSizeChunks));
    }

    public static RegionPos fromQuartPos(QuartPos2 quartPos, int regionSizeChunks) {
        return new RegionPos(QuartPos.toSection(quartPos.x()) / regionSizeChunks, QuartPos.toSection(quartPos.z()) / regionSizeChunks, regionSizeChunks);
    }

    public static RegionPos fromChunkPos(ChunkPos chunkPos, int regionSizeChunks) {
        return new RegionPos(chunkPos.x / regionSizeChunks, chunkPos.z / regionSizeChunks, regionSizeChunks);
    }

    public ChunkPos toChunkPos() {
        return new ChunkPos(this.x * this.regionSizeChunks, this.z * this.regionSizeChunks);
    }

    public RegionPos add(RegionPos regionPos) {
        if (this.regionSizeChunks != regionPos.regionSizeChunks) {
            throw new IllegalArgumentException("Region sizes must match (%d != %d)".formatted(this.regionSizeChunks, regionPos.regionSizeChunks));
        }
        return new RegionPos(this.x + regionPos.x, this.z + regionPos.z, this.regionSizeChunks);
    }

    public RegionPos subtract(RegionPos regionPos) {
        if (this.regionSizeChunks != regionPos.regionSizeChunks) {
            throw new IllegalArgumentException("Region sizes must match (%d != %d)".formatted(this.regionSizeChunks, regionPos.regionSizeChunks));
        }
        return new RegionPos(this.x - regionPos.x, this.z - regionPos.z, this.regionSizeChunks);
    }
}
