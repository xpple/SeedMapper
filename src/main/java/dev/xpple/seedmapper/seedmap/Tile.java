package dev.xpple.seedmapper.seedmap;

import net.minecraft.client.renderer.texture.DynamicTexture;

public record Tile(TilePos pos, DynamicTexture texture) {

    public static final int TEXTURE_SIZE = TilePos.TILE_SIZE_CHUNKS * SeedMapScreen.SCALED_CHUNK_SIZE;

    public Tile(TilePos pos, long seed, int dimension) {
        this(pos, initTexture(pos, seed, dimension));
    }

    private static DynamicTexture initTexture(TilePos pos, long seed, int dimension) {
        return new DynamicTexture("Tile %s %s (%d/%d)".formatted(pos.x(), pos.z(), seed, dimension), TEXTURE_SIZE, TEXTURE_SIZE, true);
    }

    public void close() {
        this.texture.close();
    }
}
