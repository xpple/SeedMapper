package dev.xpple.seedmapper.util;

import org.jetbrains.annotations.Nullable;

public final class SpiralLoop {

    private SpiralLoop() {
    }

    public static @Nullable Coordinate spiral(final int centerX, final int centerZ, final int radius, CoordinateCallback callback) {
        return spiral(centerX, centerZ, radius, 1, callback);
    }

    public static @Nullable Coordinate spiral(final int centerX, final int centerZ, final int radius, final int step, CoordinateCallback callback) {
        int x = centerX, dx = 0, z = centerZ, dz = -step;
        final int leftBoundX = centerX - radius, rightBoundX = centerX + radius;
        final int bottomBoundZ = centerZ - radius, topBoundZ = centerZ + radius;
        final long max = (2L * radius + 1) * (2L * radius + 1);
        for (long i = 0; i < max; i++) {
            if (leftBoundX <= x && x <= rightBoundX && bottomBoundZ <= z && z <= topBoundZ) {
                if (callback.consume(x, z)) {
                    return new Coordinate(x, z);
                }
            }
            if (x - centerX == z - centerZ || (x < centerX && x - centerX == centerZ - z) || (x > centerX && x - centerX == centerZ + step - z)) {
                final int temp = dx;
                dx = -dz;
                dz = temp;
            }
            x += dx;
            z += dz;
        }
        return null;
    }

    @FunctionalInterface
    public interface CoordinateCallback {
        boolean consume(int x, int z);
    }

    public record Coordinate(int x, int z) {
    }
}
