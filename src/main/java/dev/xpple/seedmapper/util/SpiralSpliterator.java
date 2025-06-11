package dev.xpple.seedmapper.util;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * A stateful version of {@link SpiralLoop}.
 */
public class SpiralSpliterator extends Spliterators.AbstractSpliterator<SpiralLoop.Coordinate> {

    private final int maxX;
    private final int step;

    private int x;
    private int z;

    private int n = 2;
    private int i = 0;
    private int j = 0;

    public SpiralSpliterator(final int centerX, final int centerZ, final int radius) {
        this(centerX, centerZ, radius, 1);
    }

    public SpiralSpliterator(final int centerX, final int centerZ, final int radius, int step) {
        super(((2L * radius) / step + 1) * ((2L * radius) / step + 1), Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.SIZED | Spliterator.NONNULL | Spliterator.IMMUTABLE);
        this.maxX = centerX + radius;
        this.step = step;

        this.x = centerX;
        this.z = centerZ;
    }

    @Override
    public boolean tryAdvance(Consumer<? super SpiralLoop.Coordinate> action) {
        if (this.x > this.maxX) {
            return false;
        }
        SpiralLoop.Coordinate returnValue = new SpiralLoop.Coordinate(this.x, this.z);
        if (this.j < (this.n >> 1)) {
            switch (i & 3) {
                case 0: this.x += this.step; break;
                case 1: this.z += this.step; break;
                case 2: this.x -= this.step; break;
                case 3: this.z -= this.step; break;
            }
            this.j++;
            action.accept(returnValue);
            return true;
        } else {
            this.j = 0;
            this.n++;
            this.i++;
            return tryAdvance(action);
        }
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }
}
