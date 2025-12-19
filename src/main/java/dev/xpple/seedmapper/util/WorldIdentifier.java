package dev.xpple.seedmapper.util;

public record WorldIdentifier(SeedIdentifier seedIdentifier, int dimension) {
    public WorldIdentifier(long seed, int dimension, int version, int generatorFlags) {
        this(new SeedIdentifier(seed, version, generatorFlags), dimension);
    }

    public long seed() {
        return this.seedIdentifier.seed();
    }

    public int version() {
        return this.seedIdentifier.version();
    }

    public int generatorFlags() {
        return this.seedIdentifier.generatorFlags();
    }
}
