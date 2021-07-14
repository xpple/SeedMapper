package dev.xpple.seedmapper.util;

import kaptainwutax.mcutils.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;

public record SpiralIterator<T extends Vec3i>(T center, int radius, int step, Builder<T> builder) implements Iterable<T> {

    @Override
    public @NotNull Iterator<T> iterator() {
        return new Iterator<>() {
            int x = center.getX();
            int z = center.getZ();

            float n = 1;
            int floorN = 1;
            int i = 0;
            int j = 0;

            @Override
            public boolean hasNext() {
                return floorN / 2 < radius;
            }

            @Override
            public T next() {
                floorN = (int) Math.floor(n);
                if (j < floorN) {
                    switch (i % 4) {
                        case 0 -> z += step;
                        case 1 -> x += step;
                        case 2 -> z -= step;
                        case 3 -> x -= step;
                    }
                    j++;
                    return builder.build(x, 0, z);
                }
                j = 0;
                n += 0.5;
                i++;
                return next();
            }
        };
    }

    @Override
    public Spliterator<T> spliterator() {
        return Spliterators.spliteratorUnknownSize(this.iterator(), Spliterator.ORDERED);
    }

    @FunctionalInterface
    public interface Builder<T extends Vec3i> {
        T build(int x, int y, int z);
    }
}
