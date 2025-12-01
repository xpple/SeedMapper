package dev.xpple.seedmapper.util;

public record QuartPos2f(float x, float z) {
    public static QuartPos2f fromQuartPos(QuartPos2 quartPos2) {
        return new QuartPos2f(quartPos2.x(), quartPos2.z());
    }

    public QuartPos2f add(QuartPos2f quartPos) {
        return this.add(quartPos.x, quartPos.z);
    }

    public QuartPos2f add(float quartX, float quartZ) {
        return new QuartPos2f(this.x + quartX, this.z + quartZ);
    }

    public QuartPos2f subtract(QuartPos2f quartPos) {
        return this.add(-quartPos.x, -quartPos.z);
    }
}
