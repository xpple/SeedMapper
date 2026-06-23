package dev.xpple.seedmapper.util;

import com.github.cubiomes.Cubiomes;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Map;

public record SeedIdentifier(long seed, int version, int generatorFlags, Map<Integer, Integer> customStructureSalts) {
    public SeedIdentifier(long seed) {
        this(seed, Cubiomes.MC_UNDEF());
    }

    public SeedIdentifier(long seed, int version) {
        this(seed, version, 0);
    }

    public SeedIdentifier(long seed, int version, int generatorFlags) {
        this(seed, version, generatorFlags, Collections.emptyMap());
    }

    public boolean hasVersion() {
        return this.version != Cubiomes.MC_UNDEF();
    }

    public boolean hasFlags() {
        return this.generatorFlags != 0;
    }

    public SeedIdentifier withVersion(int version) {
        return new SeedIdentifier(this.seed, version, this.generatorFlags, this.customStructureSalts);
    }

    public SeedIdentifier withGeneratorFlag(int generatorFlag) {
        return new SeedIdentifier(this.seed, this.version, this.generatorFlags | generatorFlag, this.customStructureSalts);
    }

    public SeedIdentifier withCustomStructureSalt(int structure, int structureSalt) {
        Map<Integer, Integer> customStructureSalts = ImmutableMap.<Integer, Integer>builder()
            .putAll(this.customStructureSalts)
            .put(structure, structureSalt)
            .buildKeepingLast();
        return new SeedIdentifier(this.seed, this.version, this.generatorFlags, customStructureSalts);
    }
}
