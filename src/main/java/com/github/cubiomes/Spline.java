// Generated by jextract

package com.github.cubiomes;

import java.lang.foreign.Arena;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SequenceLayout;
import java.lang.invoke.VarHandle;
import java.util.function.Consumer;

import static java.lang.foreign.MemoryLayout.PathElement.*;
import static java.lang.foreign.ValueLayout.*;

/**
 * {@snippet lang=c :
 * struct Spline {
 *     int len;
 *     int typ;
 *     float loc[12];
 *     float der[12];
 *     Spline *val[12];
 * }
 * }
 */
public class Spline {

    Spline() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        CubiomesHeaders.C_INT.withName("len"),
        CubiomesHeaders.C_INT.withName("typ"),
        MemoryLayout.sequenceLayout(12, CubiomesHeaders.C_FLOAT).withName("loc"),
        MemoryLayout.sequenceLayout(12, CubiomesHeaders.C_FLOAT).withName("der"),
        MemoryLayout.sequenceLayout(12, CubiomesHeaders.C_POINTER).withName("val")
    ).withName("Spline");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final OfInt len$LAYOUT = (OfInt)$LAYOUT.select(groupElement("len"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * int len
     * }
     */
    public static final OfInt len$layout() {
        return len$LAYOUT;
    }

    private static final long len$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * int len
     * }
     */
    public static final long len$offset() {
        return len$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * int len
     * }
     */
    public static int len(MemorySegment struct) {
        return struct.get(len$LAYOUT, len$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * int len
     * }
     */
    public static void len(MemorySegment struct, int fieldValue) {
        struct.set(len$LAYOUT, len$OFFSET, fieldValue);
    }

    private static final OfInt typ$LAYOUT = (OfInt)$LAYOUT.select(groupElement("typ"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * int typ
     * }
     */
    public static final OfInt typ$layout() {
        return typ$LAYOUT;
    }

    private static final long typ$OFFSET = 4;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * int typ
     * }
     */
    public static final long typ$offset() {
        return typ$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * int typ
     * }
     */
    public static int typ(MemorySegment struct) {
        return struct.get(typ$LAYOUT, typ$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * int typ
     * }
     */
    public static void typ(MemorySegment struct, int fieldValue) {
        struct.set(typ$LAYOUT, typ$OFFSET, fieldValue);
    }

    private static final SequenceLayout loc$LAYOUT = (SequenceLayout)$LAYOUT.select(groupElement("loc"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * float loc[12]
     * }
     */
    public static final SequenceLayout loc$layout() {
        return loc$LAYOUT;
    }

    private static final long loc$OFFSET = 8;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * float loc[12]
     * }
     */
    public static final long loc$offset() {
        return loc$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * float loc[12]
     * }
     */
    public static MemorySegment loc(MemorySegment struct) {
        return struct.asSlice(loc$OFFSET, loc$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * float loc[12]
     * }
     */
    public static void loc(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, loc$OFFSET, loc$LAYOUT.byteSize());
    }

    private static long[] loc$DIMS = { 12 };

    /**
     * Dimensions for array field:
     * {@snippet lang=c :
     * float loc[12]
     * }
     */
    public static long[] loc$dimensions() {
        return loc$DIMS;
    }
    private static final VarHandle loc$ELEM_HANDLE = loc$LAYOUT.varHandle(sequenceElement());

    /**
     * Indexed getter for field:
     * {@snippet lang=c :
     * float loc[12]
     * }
     */
    public static float loc(MemorySegment struct, long index0) {
        return (float)loc$ELEM_HANDLE.get(struct, 0L, index0);
    }

    /**
     * Indexed setter for field:
     * {@snippet lang=c :
     * float loc[12]
     * }
     */
    public static void loc(MemorySegment struct, long index0, float fieldValue) {
        loc$ELEM_HANDLE.set(struct, 0L, index0, fieldValue);
    }

    private static final SequenceLayout der$LAYOUT = (SequenceLayout)$LAYOUT.select(groupElement("der"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * float der[12]
     * }
     */
    public static final SequenceLayout der$layout() {
        return der$LAYOUT;
    }

    private static final long der$OFFSET = 56;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * float der[12]
     * }
     */
    public static final long der$offset() {
        return der$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * float der[12]
     * }
     */
    public static MemorySegment der(MemorySegment struct) {
        return struct.asSlice(der$OFFSET, der$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * float der[12]
     * }
     */
    public static void der(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, der$OFFSET, der$LAYOUT.byteSize());
    }

    private static long[] der$DIMS = { 12 };

    /**
     * Dimensions for array field:
     * {@snippet lang=c :
     * float der[12]
     * }
     */
    public static long[] der$dimensions() {
        return der$DIMS;
    }
    private static final VarHandle der$ELEM_HANDLE = der$LAYOUT.varHandle(sequenceElement());

    /**
     * Indexed getter for field:
     * {@snippet lang=c :
     * float der[12]
     * }
     */
    public static float der(MemorySegment struct, long index0) {
        return (float)der$ELEM_HANDLE.get(struct, 0L, index0);
    }

    /**
     * Indexed setter for field:
     * {@snippet lang=c :
     * float der[12]
     * }
     */
    public static void der(MemorySegment struct, long index0, float fieldValue) {
        der$ELEM_HANDLE.set(struct, 0L, index0, fieldValue);
    }

    private static final SequenceLayout val$LAYOUT = (SequenceLayout)$LAYOUT.select(groupElement("val"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * Spline *val[12]
     * }
     */
    public static final SequenceLayout val$layout() {
        return val$LAYOUT;
    }

    private static final long val$OFFSET = 104;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * Spline *val[12]
     * }
     */
    public static final long val$offset() {
        return val$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * Spline *val[12]
     * }
     */
    public static MemorySegment val(MemorySegment struct) {
        return struct.asSlice(val$OFFSET, val$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * Spline *val[12]
     * }
     */
    public static void val(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, val$OFFSET, val$LAYOUT.byteSize());
    }

    private static long[] val$DIMS = { 12 };

    /**
     * Dimensions for array field:
     * {@snippet lang=c :
     * Spline *val[12]
     * }
     */
    public static long[] val$dimensions() {
        return val$DIMS;
    }
    private static final VarHandle val$ELEM_HANDLE = val$LAYOUT.varHandle(sequenceElement());

    /**
     * Indexed getter for field:
     * {@snippet lang=c :
     * Spline *val[12]
     * }
     */
    public static MemorySegment val(MemorySegment struct, long index0) {
        return (MemorySegment)val$ELEM_HANDLE.get(struct, 0L, index0);
    }

    /**
     * Indexed setter for field:
     * {@snippet lang=c :
     * Spline *val[12]
     * }
     */
    public static void val(MemorySegment struct, long index0, MemorySegment fieldValue) {
        val$ELEM_HANDLE.set(struct, 0L, index0, fieldValue);
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

