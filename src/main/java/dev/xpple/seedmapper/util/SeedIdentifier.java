package dev.xpple.seedmapper.util;

import com.github.cubiomes.Cubiomes;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Map;

public record SeedIdentifier(BiomeSeedIdentifier biomeSeedIdentifier, Map<String, Integer> customStructureSalts) {
    public SeedIdentifier(long seed) {
        this(seed, Cubiomes.MC_UNDEF());
    }

    public SeedIdentifier(long seed, int version) {
        this(seed, version, 0);
    }

    public SeedIdentifier(long seed, int version, int generatorFlags) {
        this(new BiomeSeedIdentifier(seed, version, generatorFlags));
    }

    public SeedIdentifier(BiomeSeedIdentifier biomeSeedIdentifier) {
        this(biomeSeedIdentifier, Collections.emptyMap());
    }

    public SeedIdentifier(long seed, int version, int generatorFlags, Map<String, Integer> customStructureSalts) {
        this(new BiomeSeedIdentifier(seed, version, generatorFlags), customStructureSalts);
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

    public boolean hasVersion() {
        return this.biomeSeedIdentifier.hasVersion();
    }

    public boolean hasFlags() {
        return this.biomeSeedIdentifier.hasFlags();
    }

    public boolean hasCustomStructureSalts() {
        return !this.customStructureSalts.isEmpty();
    }

    public SeedIdentifier withVersion(int version) {
        return new SeedIdentifier(this.biomeSeedIdentifier.withVersion(version), this.customStructureSalts);
    }

    public SeedIdentifier withGeneratorFlag(int generatorFlag) {
        return new SeedIdentifier(this.biomeSeedIdentifier.withGeneratorFlag(generatorFlag), this.customStructureSalts);
    }

    public SeedIdentifier withCustomStructureSalt(String structure, int structureSalt) {
        Map<String, Integer> customStructureSalts = ImmutableMap.<String, Integer>builder()
            .putAll(this.customStructureSalts)
            .put(structure, structureSalt)
            .buildKeepingLast();
        return new SeedIdentifier(this.biomeSeedIdentifier, customStructureSalts);
    }
}
