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
 * union {
 *     unsigned short _Word[4];
 *     float _Float;
 *     double _Double;
 *     long double _Long_double;
 * }
 * }
 */
public class _float_const {

    _float_const() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.unionLayout(
        MemoryLayout.sequenceLayout(4, Cubiomes.C_SHORT).withName("_Word"),
        Cubiomes.C_FLOAT.withName("_Float"),
        Cubiomes.C_DOUBLE.withName("_Double"),
        Cubiomes.C_LONG_DOUBLE.withName("_Long_double")
    ).withName("$anon$239:9");

    /**
     * The layout of this union
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final SequenceLayout _Word$LAYOUT = (SequenceLayout)$LAYOUT.select(groupElement("_Word"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned short _Word[4]
     * }
     */
    public static final SequenceLayout _Word$layout() {
        return _Word$LAYOUT;
    }

    private static final long _Word$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned short _Word[4]
     * }
     */
    public static final long _Word$offset() {
        return _Word$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned short _Word[4]
     * }
     */
    public static MemorySegment _Word(MemorySegment union) {
        return union.asSlice(_Word$OFFSET, _Word$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned short _Word[4]
     * }
     */
    public static void _Word(MemorySegment union, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, union, _Word$OFFSET, _Word$LAYOUT.byteSize());
    }

    private static long[] _Word$DIMS = { 4 };

    /**
     * Dimensions for array field:
     * {@snippet lang=c :
     * unsigned short _Word[4]
     * }
     */
    public static long[] _Word$dimensions() {
        return _Word$DIMS;
    }
    private static final VarHandle _Word$ELEM_HANDLE = _Word$LAYOUT.varHandle(sequenceElement());

    /**
     * Indexed getter for field:
     * {@snippet lang=c :
     * unsigned short _Word[4]
     * }
     */
    public static short _Word(MemorySegment union, long index0) {
        return (short)_Word$ELEM_HANDLE.get(union, 0L, index0);
    }

    /**
     * Indexed setter for field:
     * {@snippet lang=c :
     * unsigned short _Word[4]
     * }
     */
    public static void _Word(MemorySegment union, long index0, short fieldValue) {
        _Word$ELEM_HANDLE.set(union, 0L, index0, fieldValue);
    }

    private static final OfFloat _Float$LAYOUT = (OfFloat)$LAYOUT.select(groupElement("_Float"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * float _Float
     * }
     */
    public static final OfFloat _Float$layout() {
        return _Float$LAYOUT;
    }

    private static final long _Float$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * float _Float
     * }
     */
    public static final long _Float$offset() {
        return _Float$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * float _Float
     * }
     */
    public static float _Float(MemorySegment union) {
        return union.get(_Float$LAYOUT, _Float$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * float _Float
     * }
     */
    public static void _Float(MemorySegment union, float fieldValue) {
        union.set(_Float$LAYOUT, _Float$OFFSET, fieldValue);
    }

    private static final OfDouble _Double$LAYOUT = (OfDouble)$LAYOUT.select(groupElement("_Double"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double _Double
     * }
     */
    public static final OfDouble _Double$layout() {
        return _Double$LAYOUT;
    }

    private static final long _Double$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double _Double
     * }
     */
    public static final long _Double$offset() {
        return _Double$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double _Double
     * }
     */
    public static double _Double(MemorySegment union) {
        return union.get(_Double$LAYOUT, _Double$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double _Double
     * }
     */
    public static void _Double(MemorySegment union, double fieldValue) {
        union.set(_Double$LAYOUT, _Double$OFFSET, fieldValue);
    }

    private static final OfDouble _Long_double$LAYOUT = (OfDouble)$LAYOUT.select(groupElement("_Long_double"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * long double _Long_double
     * }
     */
    public static final OfDouble _Long_double$layout() {
        return _Long_double$LAYOUT;
    }

    private static final long _Long_double$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * long double _Long_double
     * }
     */
    public static final long _Long_double$offset() {
        return _Long_double$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * long double _Long_double
     * }
     */
    public static double _Long_double(MemorySegment union) {
        return union.get(_Long_double$LAYOUT, _Long_double$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * long double _Long_double
     * }
     */
    public static void _Long_double(MemorySegment union, double fieldValue) {
        union.set(_Long_double$LAYOUT, _Long_double$OFFSET, fieldValue);
    }

    /**
     * Obtains a slice of {@code arrayParam} which selects the array element at {@code index}.
     * The returned segment has address {@code arrayParam.address() + index * layout().byteSize()}
     */
    public static MemorySegment asSlice(MemorySegment array, long index) {
        return array.asSlice(layout().byteSize() * index);
    }

    /**
     * The size (in bytes) of this union
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

