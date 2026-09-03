package dev.xpple.seedmapper.util;

import java.util.Map;

public record SeedIdentifierWithDimension(SeedIdentifier seedIdentifier, int dimension) {
    public SeedIdentifierWithDimension(long seed, int version, int generatorFlags, Map<String, Integer> customStructureSalts, int dimension) {
        this(new SeedIdentifier(new BiomeSeedIdentifier(seed, version, generatorFlags), customStructureSalts), dimension);
    }

    public long seed() {
        return this.seedIdentifier.biomeSeedIdentifier().seed();
    }

    public int version() {
        return this.seedIdentifier.biomeSeedIdentifier().version();
    }

    public int generatorFlags() {
        return this.seedIdentifier.biomeSeedIdentifier().generatorFlags();
    }

    public Map<String, Integer> customStructureSalts() {
        return this.seedIdentifier.customStructureSalts();
    }

    public SeedIdentifierWithDimension withDimension(int dimension) {
        return new SeedIdentifierWithDimension(this.seedIdentifier, dimension);
    }
}
