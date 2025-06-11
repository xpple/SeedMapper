package dev.xpple.seedmapper;

import com.mojang.datafixers.util.Pair;
import dev.xpple.seedmapper.util.SpiralSpliterator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SpiralSpliteratorTest {
    @Test
    public void testOriginCentred() {
        List<Pair<Integer, Integer>> coords = new ArrayList<>();
        new SpiralSpliterator(0, 0, 2, 1).forEachRemaining(coordinate -> {
            coords.add(Pair.of(coordinate.x(), coordinate.z()));
        });
        Iterator<Pair<Integer, Integer>> iterator = coords.iterator();
        assertEquals(Pair.of(0, 0), iterator.next());
        assertEquals(Pair.of(1, 0), iterator.next());
        assertEquals(Pair.of(1, 1), iterator.next());
        assertEquals(Pair.of(0, 1), iterator.next());
        assertEquals(Pair.of(-1, 1), iterator.next());
        assertEquals(Pair.of(-1, 0), iterator.next());
        assertEquals(Pair.of(-1, -1), iterator.next());
        assertEquals(Pair.of(0, -1), iterator.next());
        assertEquals(Pair.of(1, -1), iterator.next());
        assertEquals(Pair.of(2, -1), iterator.next());
        assertEquals(Pair.of(2, 0), iterator.next());
        assertEquals(Pair.of(2, 1), iterator.next());
        assertEquals(Pair.of(2, 2), iterator.next());
        assertEquals(Pair.of(1, 2), iterator.next());
        assertEquals(Pair.of(0, 2), iterator.next());
        assertEquals(Pair.of(-1, 2), iterator.next());
        assertEquals(Pair.of(-2, 2), iterator.next());
        assertEquals(Pair.of(-2, 1), iterator.next());
        assertEquals(Pair.of(-2, 0), iterator.next());
        assertEquals(Pair.of(-2, -1), iterator.next());
        assertEquals(Pair.of(-2, -2), iterator.next());
        assertEquals(Pair.of(-1, -2), iterator.next());
        assertEquals(Pair.of(0, -2), iterator.next());
        assertEquals(Pair.of(1, -2), iterator.next());
        assertEquals(Pair.of(2, -2), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testOffset5m10() {
        List<Pair<Integer, Integer>> coords = new ArrayList<>();
        new SpiralSpliterator(5, -10, 2, 1).forEachRemaining(coordinate -> {
            coords.add(Pair.of(coordinate.x(), coordinate.z()));
        });
        Iterator<Pair<Integer, Integer>> iterator = coords.iterator();
        assertEquals(Pair.of(5, -10), iterator.next());
        assertEquals(Pair.of(6, -10), iterator.next());
        assertEquals(Pair.of(6, -9), iterator.next());
        assertEquals(Pair.of(5, -9), iterator.next());
        assertEquals(Pair.of(4, -9), iterator.next());
        assertEquals(Pair.of(4, -10), iterator.next());
        assertEquals(Pair.of(4, -11), iterator.next());
        assertEquals(Pair.of(5, -11), iterator.next());
        assertEquals(Pair.of(6, -11), iterator.next());
        assertEquals(Pair.of(7, -11), iterator.next());
        assertEquals(Pair.of(7, -10), iterator.next());
        assertEquals(Pair.of(7, -9), iterator.next());
        assertEquals(Pair.of(7, -8), iterator.next());
        assertEquals(Pair.of(6, -8), iterator.next());
        assertEquals(Pair.of(5, -8), iterator.next());
        assertEquals(Pair.of(4, -8), iterator.next());
        assertEquals(Pair.of(3, -8), iterator.next());
        assertEquals(Pair.of(3, -9), iterator.next());
        assertEquals(Pair.of(3, -10), iterator.next());
        assertEquals(Pair.of(3, -11), iterator.next());
        assertEquals(Pair.of(3, -12), iterator.next());
        assertEquals(Pair.of(4, -12), iterator.next());
        assertEquals(Pair.of(5, -12), iterator.next());
        assertEquals(Pair.of(6, -12), iterator.next());
        assertEquals(Pair.of(7, -12), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testStep10() {
        List<Pair<Integer, Integer>> coords = new ArrayList<>();
        new SpiralSpliterator(0, 0, 20, 10).forEachRemaining(coordinate -> {
            coords.add(Pair.of(coordinate.x(), coordinate.z()));
        });
        Iterator<Pair<Integer, Integer>> iterator = coords.iterator();
        assertEquals(Pair.of(0, 0), iterator.next());
        assertEquals(Pair.of(10, 0), iterator.next());
        assertEquals(Pair.of(10, 10), iterator.next());
        assertEquals(Pair.of(0, 10), iterator.next());
        assertEquals(Pair.of(-10, 10), iterator.next());
        assertEquals(Pair.of(-10, 0), iterator.next());
        assertEquals(Pair.of(-10, -10), iterator.next());
        assertEquals(Pair.of(0, -10), iterator.next());
        assertEquals(Pair.of(10, -10), iterator.next());
        assertEquals(Pair.of(20, -10), iterator.next());
        assertEquals(Pair.of(20, 0), iterator.next());
        assertEquals(Pair.of(20, 10), iterator.next());
        assertEquals(Pair.of(20, 20), iterator.next());
        assertEquals(Pair.of(10, 20), iterator.next());
        assertEquals(Pair.of(0, 20), iterator.next());
        assertEquals(Pair.of(-10, 20), iterator.next());
        assertEquals(Pair.of(-20, 20), iterator.next());
        assertEquals(Pair.of(-20, 10), iterator.next());
        assertEquals(Pair.of(-20, 0), iterator.next());
        assertEquals(Pair.of(-20, -10), iterator.next());
        assertEquals(Pair.of(-20, -20), iterator.next());
        assertEquals(Pair.of(-10, -20), iterator.next());
        assertEquals(Pair.of(0, -20), iterator.next());
        assertEquals(Pair.of(10, -20), iterator.next());
        assertEquals(Pair.of(20, -20), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testOffset10m5Step32() {
        List<Pair<Integer, Integer>> coords = new ArrayList<>();
        new SpiralSpliterator(10, -5, 64, 32).forEachRemaining(coordinate -> {
            coords.add(Pair.of(coordinate.x(), coordinate.z()));
        });
        Iterator<Pair<Integer, Integer>> iterator = coords.iterator();
        assertEquals(Pair.of(10, -5), iterator.next());
        assertEquals(Pair.of(42, -5), iterator.next());
        assertEquals(Pair.of(42, 27), iterator.next());
        assertEquals(Pair.of(10, 27), iterator.next());
        assertEquals(Pair.of(-22, 27), iterator.next());
        assertEquals(Pair.of(-22, -5), iterator.next());
        assertEquals(Pair.of(-22, -37), iterator.next());
        assertEquals(Pair.of(10, -37), iterator.next());
        assertEquals(Pair.of(42, -37), iterator.next());
        assertEquals(Pair.of(74, -37), iterator.next());
        assertEquals(Pair.of(74, -5), iterator.next());
        assertEquals(Pair.of(74, 27), iterator.next());
        assertEquals(Pair.of(74, 59), iterator.next());
        assertEquals(Pair.of(42, 59), iterator.next());
        assertEquals(Pair.of(10, 59), iterator.next());
        assertEquals(Pair.of(-22, 59), iterator.next());
        assertEquals(Pair.of(-54, 59), iterator.next());
        assertEquals(Pair.of(-54, 27), iterator.next());
        assertEquals(Pair.of(-54, -5), iterator.next());
        assertEquals(Pair.of(-54, -37), iterator.next());
        assertEquals(Pair.of(-54, -69), iterator.next());
        assertEquals(Pair.of(-22, -69), iterator.next());
        assertEquals(Pair.of(10, -69), iterator.next());
        assertEquals(Pair.of(42, -69), iterator.next());
        assertEquals(Pair.of(74, -69), iterator.next());
        assertFalse(iterator.hasNext());
    }
}
