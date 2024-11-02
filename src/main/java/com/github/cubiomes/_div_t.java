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
 * struct _div_t {
 *     int quot;
 *     int rem;
 * }
 * }
 */
public class _div_t {

    _div_t() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        CubiomesHeaders.C_INT.withName("quot"),
        CubiomesHeaders.C_INT.withName("rem")
    ).withName("_div_t");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final OfInt quot$LAYOUT = (OfInt)$LAYOUT.select(groupElement("quot"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * int quot
     * }
     */
    public static final OfInt quot$layout() {
        return quot$LAYOUT;
    }

    private static final long quot$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * int quot
     * }
     */
    public static final long quot$offset() {
        return quot$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * int quot
     * }
     */
    public static int quot(MemorySegment struct) {
        return struct.get(quot$LAYOUT, quot$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * int quot
     * }
     */
    public static void quot(MemorySegment struct, int fieldValue) {
        struct.set(quot$LAYOUT, quot$OFFSET, fieldValue);
    }

    private static final OfInt rem$LAYOUT = (OfInt)$LAYOUT.select(groupElement("rem"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * int rem
     * }
     */
    public static final OfInt rem$layout() {
        return rem$LAYOUT;
    }

    private static final long rem$OFFSET = 4;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * int rem
     * }
     */
    public static final long rem$offset() {
        return rem$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * int rem
     * }
     */
    public static int rem(MemorySegment struct) {
        return struct.get(rem$LAYOUT, rem$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * int rem
     * }
     */
    public static void rem(MemorySegment struct, int fieldValue) {
        struct.set(rem$LAYOUT, rem$OFFSET, fieldValue);
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

