package dev.xpple.seedmapper.util;

import net.minecraft.world.phys.Vec3;

public record QuartPos2f(float x, float z) {
    public static QuartPos2f fromQuartPos(QuartPos2 quartPos2) {
        return new QuartPos2f(quartPos2.x(), quartPos2.z());
    }

    public static QuartPos2f fromVec3(Vec3 vec3) {
        return new QuartPos2f((float) vec3.x / 4.0f, (float) vec3.z / 4.0f);
    }

    public Vec3 toVec3() {
        return new Vec3(this.x * 4.0f, 0, this.z * 4.0f);
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
