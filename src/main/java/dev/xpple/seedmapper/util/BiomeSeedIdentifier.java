package dev.xpple.seedmapper.util;

import com.github.cubiomes.Cubiomes;
import com.google.gson.annotations.JsonAdapter;
import dev.xpple.seedmapper.config.VersionAdapter;

// consider adding adapter for `generatorFlags`
public record BiomeSeedIdentifier(long seed, @JsonAdapter(VersionAdapter.class) int version, int generatorFlags) {
    public BiomeSeedIdentifier(long seed) {
        this(seed, Cubiomes.MC_UNDEF());
    }

    public BiomeSeedIdentifier(long seed, int version) {
        this(seed, version, 0);
    }

    public boolean hasVersion() {
        return this.version != Cubiomes.MC_UNDEF();
    }

    public boolean hasFlags() {
        return this.generatorFlags != 0;
    }

    public BiomeSeedIdentifier withVersion(int version) {
        return new BiomeSeedIdentifier(this.seed, version, this.generatorFlags);
    }

    public BiomeSeedIdentifier withGeneratorFlag(int generatorFlag) {
        return new BiomeSeedIdentifier(this.seed, this.version, this.generatorFlags | generatorFlag);
    }
}
