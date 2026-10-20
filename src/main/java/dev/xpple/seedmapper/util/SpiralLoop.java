package dev.xpple.seedmapper.util;

import org.jspecify.annotations.Nullable;

public final class SpiralLoop {

    private SpiralLoop() {
    }

    public static <T> @Nullable T spiral(final int centerX, final int centerZ, final int radius, CoordinateCallback<T> callback) {
        return spiral(centerX, centerZ, radius, 1, callback);
    }

    public static <T> @Nullable T spiral(final int centerX, final int centerZ, final int radius, final int step, CoordinateCallback<T> callback) {
        int x = centerX, dx = 0, z = centerZ, dz = -step;
        final int leftBoundX = centerX - radius, rightBoundX = centerX + radius;
        final int bottomBoundZ = centerZ - radius, topBoundZ = centerZ + radius;
        final long max = (2L * radius + 1) * (2L * radius + 1);
        for (long i = 0; i < max; i++) {
            if (leftBoundX <= x && x <= rightBoundX && bottomBoundZ <= z && z <= topBoundZ) {
                T obj = callback.produce(x, z);
                if (obj != null) {
                    return obj;
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
    public interface CoordinateCallback<T> {
        @Nullable T produce(int x, int z);
    }
}
