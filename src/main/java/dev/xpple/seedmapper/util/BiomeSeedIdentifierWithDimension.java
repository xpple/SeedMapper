package dev.xpple.seedmapper.util;

public record BiomeSeedIdentifierWithDimension(BiomeSeedIdentifier biomeSeedIdentifier, int dimension) {
    public BiomeSeedIdentifierWithDimension(long seed, int dimension, int version, int generatorFlags) {
        this(new BiomeSeedIdentifier(seed, version, generatorFlags), dimension);
    }

    public long seed() {
        return this.biomeSeedIdentifier.seed();
    }

    public int version() {
        return this.biomeSeedIdentifier.version();
    }

    public int generatorFlags() {
        return this.biomeSeedIdentifier.generatorFlags();
    }
}
