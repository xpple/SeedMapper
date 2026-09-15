package dev.xpple.seedmapper.seedmap;

/// Matches possible values for [com.github.cubiomes.Range#scale].
public enum BiomeScale {
    SCALE_1(1),
    SCALE_4(4),
    SCALE_16(16),
    SCALE_64(64),
    SCALE_256(256);

    public final int val;

    BiomeScale(int val) {
        this.val = val;
    }
}
