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
 * struct {
 *     float f;
 * }
 * }
 */
public class _CRT_FLOAT {

    _CRT_FLOAT() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        Cubiomes.C_FLOAT.withName("f")
    ).withName("$anon$420:9");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final OfFloat f$LAYOUT = (OfFloat)$LAYOUT.select(groupElement("f"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * float f
     * }
     */
    public static final OfFloat f$layout() {
        return f$LAYOUT;
    }

    private static final long f$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * float f
     * }
     */
    public static final long f$offset() {
        return f$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * float f
     * }
     */
    public static float f(MemorySegment struct) {
        return struct.get(f$LAYOUT, f$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * float f
     * }
     */
    public static void f(MemorySegment struct, float fieldValue) {
        struct.set(f$LAYOUT, f$OFFSET, fieldValue);
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

