package dev.xpple.seedmapper.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.function.Supplier;

public final class SpiralLoop {

    private SpiralLoop() {
    }

    public static void spiral(final int centerX, final int centerZ, final int radius, CoordinateCallback callback, Supplier<? extends CommandSyntaxException> failure) throws CommandSyntaxException {
        spiral(centerX, centerZ, radius, 1, callback, failure);
    }

    public static void spiral(final int centerX, final int centerZ, final int radius, final int step, CoordinateCallback callback, Supplier<? extends CommandSyntaxException> failure) throws CommandSyntaxException {
        int x = centerX, dx = 0, z = centerZ, dz = -step;
        final int leftBoundX = centerX - radius, rightBoundX = centerX + radius;
        final int bottomBoundZ = centerZ - radius, topBoundZ = centerZ + radius;
        final long max = (2L * radius + 1) * (2L * radius + 1);
        for (int i = 0; i < max ; i++) {
            if (leftBoundX <= x && x <= rightBoundX && bottomBoundZ <= z && z <= topBoundZ) {
                if (callback.consume(x, z)) {
                    return;
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
        throw failure.get();
    }

    @FunctionalInterface
    public interface CoordinateCallback {
        boolean consume(int x, int z);
    }
}
