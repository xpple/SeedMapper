package dev.xpple.seedmapper.seedmap;

import net.minecraft.client.renderer.texture.DynamicTexture;

public record Tile(TilePos pos, DynamicTexture texture) {
    public Tile(TilePos pos, long seed, int dimension) {
        this(pos, initTexture(pos, seed, dimension));
    }

    private static DynamicTexture initTexture(TilePos pos, long seed, int dimension) {
        int size = TilePos.TILE_SIZE_CHUNKS * SeedMapScreen.SCALED_CHUNK_SIZE;
        return new DynamicTexture("Tile %s %s (%d/%d)".formatted(pos.x(), pos.z(), seed, dimension), size, size, true);
    }

    public void close() {
        this.texture.close();
    }
}
