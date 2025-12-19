package dev.xpple.seedmapper.util;

import com.github.cubiomes.Cubiomes;

public record SeedIdentifier(long seed, int version, int generatorFlags) {
    public SeedIdentifier(long seed) {
        this(seed, Cubiomes.MC_UNDEF(), 0);
    }

    public boolean hasVersion() {
        return this.version != Cubiomes.MC_UNDEF();
    }

    public boolean hasFlags() {
        return this.generatorFlags != 0;
    }

    public SeedIdentifier withVersion(int version) {
        return new SeedIdentifier(this.seed, version, this.generatorFlags);
    }

    public SeedIdentifier withGeneratorFlag(int generatorFlag) {
        return new SeedIdentifier(this.seed, this.version, this.generatorFlags | generatorFlag);
    }
}
