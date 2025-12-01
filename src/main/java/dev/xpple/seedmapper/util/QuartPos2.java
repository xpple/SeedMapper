package dev.xpple.seedmapper.util;

import dev.xpple.seedmapper.seedmap.TilePos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;

public record QuartPos2(int x, int z) {
    public static QuartPos2 fromQuartPos2f(QuartPos2f quartPos2f) {
        return new QuartPos2(Mth.floor(quartPos2f.x()), Mth.floor(quartPos2f.z()));
    }

    public static QuartPos2 fromBlockPos(BlockPos blockPos) {
        return new QuartPos2(QuartPos.fromBlock(blockPos.getX()), QuartPos.fromBlock(blockPos.getZ()));
    }

    public BlockPos toBlockPos() {
        return new BlockPos(QuartPos.toBlock(this.x), 0, QuartPos.toBlock(this.z));
    }

    public static QuartPos2 fromChunkPos(ChunkPos chunkPos) {
        return new QuartPos2(QuartPos.fromSection(chunkPos.x), QuartPos.fromSection(chunkPos.z));
    }

    public ChunkPos toChunkPos() {
        return new ChunkPos(QuartPos.toSection(this.x), QuartPos.toSection(this.z));
    }

    public static QuartPos2 fromTilePos(TilePos tilePos) {
        return new QuartPos2(QuartPos.fromSection(TilePos.TILE_SIZE_CHUNKS * tilePos.x()), QuartPos.fromSection(TilePos.TILE_SIZE_CHUNKS * tilePos.z()));
    }

    public static QuartPos2 fromRegionPos(RegionPos regionPos) {
        return new QuartPos2(QuartPos.fromSection(regionPos.x() * regionPos.regionSizeChunks()), QuartPos.fromSection(regionPos.z() * regionPos.regionSizeChunks()));
    }

    public QuartPos2 add(QuartPos2 quartPos) {
        return this.add(quartPos.x, quartPos.z);
    }

    public QuartPos2 add(int quartX, int quartZ) {
        return new QuartPos2(this.x + quartX, this.z + quartZ);
    }

    public QuartPos2 subtract(QuartPos2 quartPos) {
        return this.add(-quartPos.x, -quartPos.z);
    }
}
