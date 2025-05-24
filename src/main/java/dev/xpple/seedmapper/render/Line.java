package dev.xpple.seedmapper.render;

import net.minecraft.world.phys.Vec3;

public record Line(Vec3 start, Vec3 end, int colour) {
    public Line {
        // ensure consistent ordering, the ordering itself doesn't matter
        if (start.hashCode() > end.hashCode()) {
            Vec3 temp = start;
            start = end;
            end = temp;
        }
    }

    public Line offset(Vec3 offset) {
        return new Line(this.start.add(offset), this.end.add(offset), this.colour);
    }
}
