// Generated by jextract

package com.github.cubiomes;

import java.lang.foreign.Arena;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.util.function.Consumer;

import static java.lang.foreign.MemoryLayout.PathElement.*;
import static java.lang.foreign.ValueLayout.*;

/**
 * {@snippet lang=c :
 * struct StrongholdIter {
 *     Pos pos;
 *     Pos nextapprox;
 *     int index;
 *     int ringnum;
 *     int ringmax;
 *     int ringidx;
 *     double angle;
 *     double dist;
 *     uint64_t rnds;
 *     int mc;
 * }
 * }
 */
public class StrongholdIter {

    StrongholdIter() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        Pos.layout().withName("pos"),
        Pos.layout().withName("nextapprox"),
        Cubiomes.C_INT.withName("index"),
        Cubiomes.C_INT.withName("ringnum"),
        Cubiomes.C_INT.withName("ringmax"),
        Cubiomes.C_INT.withName("ringidx"),
        Cubiomes.C_DOUBLE.withName("angle"),
        Cubiomes.C_DOUBLE.withName("dist"),
        Cubiomes.C_LONG_LONG.withName("rnds"),
        Cubiomes.C_INT.withName("mc"),
        MemoryLayout.paddingLayout(4)
    ).withName("StrongholdIter");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final GroupLayout pos$LAYOUT = (GroupLayout)$LAYOUT.select(groupElement("pos"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * Pos pos
     * }
     */
    public static final GroupLayout pos$layout() {
        return pos$LAYOUT;
    }

    private static final long pos$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * Pos pos
     * }
     */
    public static final long pos$offset() {
        return pos$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * Pos pos
     * }
     */
    public static MemorySegment pos(MemorySegment struct) {
        return struct.asSlice(pos$OFFSET, pos$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * Pos pos
     * }
     */
    public static void pos(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, pos$OFFSET, pos$LAYOUT.byteSize());
    }

    private static final GroupLayout nextapprox$LAYOUT = (GroupLayout)$LAYOUT.select(groupElement("nextapprox"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * Pos nextapprox
     * }
     */
    public static final GroupLayout nextapprox$layout() {
        return nextapprox$LAYOUT;
    }

    private static final long nextapprox$OFFSET = 8;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * Pos nextapprox
     * }
     */
    public static final long nextapprox$offset() {
        return nextapprox$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * Pos nextapprox
     * }
     */
    public static MemorySegment nextapprox(MemorySegment struct) {
        return struct.asSlice(nextapprox$OFFSET, nextapprox$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * Pos nextapprox
     * }
     */
    public static void nextapprox(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, nextapprox$OFFSET, nextapprox$LAYOUT.byteSize());
    }

    private static final OfInt index$LAYOUT = (OfInt)$LAYOUT.select(groupElement("index"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * int index
     * }
     */
    public static final OfInt index$layout() {
        return index$LAYOUT;
    }

    private static final long index$OFFSET = 16;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * int index
     * }
     */
    public static final long index$offset() {
        return index$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * int index
     * }
     */
    public static int index(MemorySegment struct) {
        return struct.get(index$LAYOUT, index$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * int index
     * }
     */
    public static void index(MemorySegment struct, int fieldValue) {
        struct.set(index$LAYOUT, index$OFFSET, fieldValue);
    }

    private static final OfInt ringnum$LAYOUT = (OfInt)$LAYOUT.select(groupElement("ringnum"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * int ringnum
     * }
     */
    public static final OfInt ringnum$layout() {
        return ringnum$LAYOUT;
    }

    private static final long ringnum$OFFSET = 20;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * int ringnum
     * }
     */
    public static final long ringnum$offset() {
        return ringnum$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * int ringnum
     * }
     */
    public static int ringnum(MemorySegment struct) {
        return struct.get(ringnum$LAYOUT, ringnum$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * int ringnum
     * }
     */
    public static void ringnum(MemorySegment struct, int fieldValue) {
        struct.set(ringnum$LAYOUT, ringnum$OFFSET, fieldValue);
    }

    private static final OfInt ringmax$LAYOUT = (OfInt)$LAYOUT.select(groupElement("ringmax"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * int ringmax
     * }
     */
    public static final OfInt ringmax$layout() {
        return ringmax$LAYOUT;
    }

    private static final long ringmax$OFFSET = 24;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * int ringmax
     * }
     */
    public static final long ringmax$offset() {
        return ringmax$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * int ringmax
     * }
     */
    public static int ringmax(MemorySegment struct) {
        return struct.get(ringmax$LAYOUT, ringmax$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * int ringmax
     * }
     */
    public static void ringmax(MemorySegment struct, int fieldValue) {
        struct.set(ringmax$LAYOUT, ringmax$OFFSET, fieldValue);
    }

    private static final OfInt ringidx$LAYOUT = (OfInt)$LAYOUT.select(groupElement("ringidx"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * int ringidx
     * }
     */
    public static final OfInt ringidx$layout() {
        return ringidx$LAYOUT;
    }

    private static final long ringidx$OFFSET = 28;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * int ringidx
     * }
     */
    public static final long ringidx$offset() {
        return ringidx$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * int ringidx
     * }
     */
    public static int ringidx(MemorySegment struct) {
        return struct.get(ringidx$LAYOUT, ringidx$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * int ringidx
     * }
     */
    public static void ringidx(MemorySegment struct, int fieldValue) {
        struct.set(ringidx$LAYOUT, ringidx$OFFSET, fieldValue);
    }

    private static final OfDouble angle$LAYOUT = (OfDouble)$LAYOUT.select(groupElement("angle"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double angle
     * }
     */
    public static final OfDouble angle$layout() {
        return angle$LAYOUT;
    }

    private static final long angle$OFFSET = 32;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double angle
     * }
     */
    public static final long angle$offset() {
        return angle$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double angle
     * }
     */
    public static double angle(MemorySegment struct) {
        return struct.get(angle$LAYOUT, angle$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double angle
     * }
     */
    public static void angle(MemorySegment struct, double fieldValue) {
        struct.set(angle$LAYOUT, angle$OFFSET, fieldValue);
    }

    private static final OfDouble dist$LAYOUT = (OfDouble)$LAYOUT.select(groupElement("dist"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double dist
     * }
     */
    public static final OfDouble dist$layout() {
        return dist$LAYOUT;
    }

    private static final long dist$OFFSET = 40;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double dist
     * }
     */
    public static final long dist$offset() {
        return dist$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double dist
     * }
     */
    public static double dist(MemorySegment struct) {
        return struct.get(dist$LAYOUT, dist$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double dist
     * }
     */
    public static void dist(MemorySegment struct, double fieldValue) {
        struct.set(dist$LAYOUT, dist$OFFSET, fieldValue);
    }

    private static final OfLong rnds$LAYOUT = (OfLong)$LAYOUT.select(groupElement("rnds"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t rnds
     * }
     */
    public static final OfLong rnds$layout() {
        return rnds$LAYOUT;
    }

    private static final long rnds$OFFSET = 48;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t rnds
     * }
     */
    public static final long rnds$offset() {
        return rnds$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t rnds
     * }
     */
    public static long rnds(MemorySegment struct) {
        return struct.get(rnds$LAYOUT, rnds$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t rnds
     * }
     */
    public static void rnds(MemorySegment struct, long fieldValue) {
        struct.set(rnds$LAYOUT, rnds$OFFSET, fieldValue);
    }

    private static final OfInt mc$LAYOUT = (OfInt)$LAYOUT.select(groupElement("mc"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * int mc
     * }
     */
    public static final OfInt mc$layout() {
        return mc$LAYOUT;
    }

    private static final long mc$OFFSET = 56;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * int mc
     * }
     */
    public static final long mc$offset() {
        return mc$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * int mc
     * }
     */
    public static int mc(MemorySegment struct) {
        return struct.get(mc$LAYOUT, mc$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * int mc
     * }
     */
    public static void mc(MemorySegment struct, int fieldValue) {
        struct.set(mc$LAYOUT, mc$OFFSET, fieldValue);
    }

    /**
     * Obtains a slice of {@code arrayParam} which selects the array element at {@code index}.
     * The returned segment has address {@code arrayParam.address() + index * layout().byteSize()}
     */
    public static MemorySegment asSlice(MemorySegment array, long index) {
        return array.asSlice(layout().byteSize() * index);
    }

    /**
     * The size (in bytes) of this struct
     */
    public static long sizeof() { return layout().byteSize(); }

    /**
     * Allocate a segment of size {@code layout().byteSize()} using {@code allocator}
     */
    public static MemorySegment allocate(SegmentAllocator allocator) {
        return allocator.allocate(layout());
    }

    /**
     * Allocate an array of size {@code elementCount} using {@code allocator}.
     * The returned segment has size {@code elementCount * layout().byteSize()}.
     */
    public static MemorySegment allocateArray(long elementCount, SegmentAllocator allocator) {
        return allocator.allocate(MemoryLayout.sequenceLayout(elementCount, layout()));
    }

    /**
     * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction} (if any).
     * The returned segment has size {@code layout().byteSize()}
     */
    public static MemorySegment reinterpret(MemorySegment addr, Arena arena, Consumer<MemorySegment> cleanup) {
        return reinterpret(addr, 1, arena, cleanup);
    }

    /**
     * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction} (if any).
     * The returned segment has size {@code elementCount * layout().byteSize()}
     */
    public static MemorySegment reinterpret(MemorySegment addr, long elementCount, Arena arena, Consumer<MemorySegment> cleanup) {
        return addr.reinterpret(layout().byteSize() * elementCount, arena, cleanup);
    }
}

