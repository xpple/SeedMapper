package dev.xpple.seedmapper.seedmap;

import dev.xpple.seedmapper.util.QuartPos2;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;

public record TilePos(int x, int z) {
    public static final int TILE_SIZE_CHUNKS = 25;

    public static TilePos fromQuartPos(QuartPos2 quartPos) {
        return new TilePos(QuartPos.toSection(quartPos.x()) / TILE_SIZE_CHUNKS, QuartPos.toSection(quartPos.z()) / TILE_SIZE_CHUNKS);
    }

    public static TilePos fromChunkPos(ChunkPos chunkPos) {
        return new TilePos(chunkPos.x / TILE_SIZE_CHUNKS, chunkPos.z / TILE_SIZE_CHUNKS);
    }

    public ChunkPos toChunkPos() {
        return new ChunkPos(this.x * TILE_SIZE_CHUNKS, this.z * TILE_SIZE_CHUNKS);
    }

    public TilePos add(TilePos tilePos) {
        return this.add(tilePos.x, tilePos.z);
    }

    public TilePos add(int tileX, int tileZ) {
        return new TilePos(this.x + tileX, this.z + tileZ);
    }

    public TilePos subtract(TilePos tilePos) {
        return this.add(-tilePos.x, -tilePos.z);
    }
}
