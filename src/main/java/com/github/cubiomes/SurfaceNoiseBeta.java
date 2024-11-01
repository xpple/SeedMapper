// Generated by jextract

package com.github.cubiomes;

import java.lang.foreign.Arena;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SequenceLayout;
import java.lang.invoke.MethodHandle;
import java.util.function.Consumer;

import static java.lang.foreign.MemoryLayout.PathElement.*;

/**
 * {@snippet lang=c :
 * struct SurfaceNoiseBeta {
 *     OctaveNoise octmin;
 *     OctaveNoise octmax;
 *     OctaveNoise octmain;
 *     OctaveNoise octcontA;
 *     OctaveNoise octcontB;
 *     PerlinNoise oct[66];
 * }
 * }
 */
public class SurfaceNoiseBeta {

    SurfaceNoiseBeta() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        OctaveNoise.layout().withName("octmin"),
        OctaveNoise.layout().withName("octmax"),
        OctaveNoise.layout().withName("octmain"),
        OctaveNoise.layout().withName("octcontA"),
        OctaveNoise.layout().withName("octcontB"),
        MemoryLayout.sequenceLayout(66, PerlinNoise.layout()).withName("oct")
    ).withName("SurfaceNoiseBeta");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final GroupLayout octmin$LAYOUT = (GroupLayout)$LAYOUT.select(groupElement("octmin"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * OctaveNoise octmin
     * }
     */
    public static final GroupLayout octmin$layout() {
        return octmin$LAYOUT;
    }

    private static final long octmin$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * OctaveNoise octmin
     * }
     */
    public static final long octmin$offset() {
        return octmin$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * OctaveNoise octmin
     * }
     */
    public static MemorySegment octmin(MemorySegment struct) {
        return struct.asSlice(octmin$OFFSET, octmin$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * OctaveNoise octmin
     * }
     */
    public static void octmin(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, octmin$OFFSET, octmin$LAYOUT.byteSize());
    }

    private static final GroupLayout octmax$LAYOUT = (GroupLayout)$LAYOUT.select(groupElement("octmax"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * OctaveNoise octmax
     * }
     */
    public static final GroupLayout octmax$layout() {
        return octmax$LAYOUT;
    }

    private static final long octmax$OFFSET = 16;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * OctaveNoise octmax
     * }
     */
    public static final long octmax$offset() {
        return octmax$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * OctaveNoise octmax
     * }
     */
    public static MemorySegment octmax(MemorySegment struct) {
        return struct.asSlice(octmax$OFFSET, octmax$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * OctaveNoise octmax
     * }
     */
    public static void octmax(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, octmax$OFFSET, octmax$LAYOUT.byteSize());
    }

    private static final GroupLayout octmain$LAYOUT = (GroupLayout)$LAYOUT.select(groupElement("octmain"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * OctaveNoise octmain
     * }
     */
    public static final GroupLayout octmain$layout() {
        return octmain$LAYOUT;
    }

    private static final long octmain$OFFSET = 32;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * OctaveNoise octmain
     * }
     */
    public static final long octmain$offset() {
        return octmain$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * OctaveNoise octmain
     * }
     */
    public static MemorySegment octmain(MemorySegment struct) {
        return struct.asSlice(octmain$OFFSET, octmain$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * OctaveNoise octmain
     * }
     */
    public static void octmain(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, octmain$OFFSET, octmain$LAYOUT.byteSize());
    }

    private static final GroupLayout octcontA$LAYOUT = (GroupLayout)$LAYOUT.select(groupElement("octcontA"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * OctaveNoise octcontA
     * }
     */
    public static final GroupLayout octcontA$layout() {
        return octcontA$LAYOUT;
    }

    private static final long octcontA$OFFSET = 48;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * OctaveNoise octcontA
     * }
     */
    public static final long octcontA$offset() {
        return octcontA$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * OctaveNoise octcontA
     * }
     */
    public static MemorySegment octcontA(MemorySegment struct) {
        return struct.asSlice(octcontA$OFFSET, octcontA$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * OctaveNoise octcontA
     * }
     */
    public static void octcontA(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, octcontA$OFFSET, octcontA$LAYOUT.byteSize());
    }

    private static final GroupLayout octcontB$LAYOUT = (GroupLayout)$LAYOUT.select(groupElement("octcontB"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * OctaveNoise octcontB
     * }
     */
    public static final GroupLayout octcontB$layout() {
        return octcontB$LAYOUT;
    }

    private static final long octcontB$OFFSET = 64;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * OctaveNoise octcontB
     * }
     */
    public static final long octcontB$offset() {
        return octcontB$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * OctaveNoise octcontB
     * }
     */
    public static MemorySegment octcontB(MemorySegment struct) {
        return struct.asSlice(octcontB$OFFSET, octcontB$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * OctaveNoise octcontB
     * }
     */
    public static void octcontB(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, octcontB$OFFSET, octcontB$LAYOUT.byteSize());
    }

    private static final SequenceLayout oct$LAYOUT = (SequenceLayout)$LAYOUT.select(groupElement("oct"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * PerlinNoise oct[66]
     * }
     */
    public static final SequenceLayout oct$layout() {
        return oct$LAYOUT;
    }

    private static final long oct$OFFSET = 80;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * PerlinNoise oct[66]
     * }
     */
    public static final long oct$offset() {
        return oct$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * PerlinNoise oct[66]
     * }
     */
    public static MemorySegment oct(MemorySegment struct) {
        return struct.asSlice(oct$OFFSET, oct$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * PerlinNoise oct[66]
     * }
     */
    public static void oct(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, oct$OFFSET, oct$LAYOUT.byteSize());
    }

    private static long[] oct$DIMS = { 66 };

    /**
     * Dimensions for array field:
     * {@snippet lang=c :
     * PerlinNoise oct[66]
     * }
     */
    public static long[] oct$dimensions() {
        return oct$DIMS;
    }
    private static final MethodHandle oct$ELEM_HANDLE = oct$LAYOUT.sliceHandle(sequenceElement());

    /**
     * Indexed getter for field:
     * {@snippet lang=c :
     * PerlinNoise oct[66]
     * }
     */
    public static MemorySegment oct(MemorySegment struct, long index0) {
        try {
            return (MemorySegment)oct$ELEM_HANDLE.invokeExact(struct, 0L, index0);
        } catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    /**
     * Indexed setter for field:
     * {@snippet lang=c :
     * PerlinNoise oct[66]
     * }
     */
    public static void oct(MemorySegment struct, long index0, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, oct(struct, index0), 0L, PerlinNoise.layout().byteSize());
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

