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

/**
 * {@snippet lang=c :
 * struct {
 *     unsigned char ld12[12];
 * }
 * }
 */
public class _LDBL12 {

    _LDBL12() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        MemoryLayout.sequenceLayout(12, Cubiomes.C_CHAR).withName("ld12")
    ).withName("$anon$437:9");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final SequenceLayout ld12$LAYOUT = (SequenceLayout)$LAYOUT.select(groupElement("ld12"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * unsigned char ld12[12]
     * }
     */
    public static final SequenceLayout ld12$layout() {
        return ld12$LAYOUT;
    }

    private static final long ld12$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * unsigned char ld12[12]
     * }
     */
    public static final long ld12$offset() {
        return ld12$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * unsigned char ld12[12]
     * }
     */
    public static MemorySegment ld12(MemorySegment struct) {
        return struct.asSlice(ld12$OFFSET, ld12$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * unsigned char ld12[12]
     * }
     */
    public static void ld12(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, ld12$OFFSET, ld12$LAYOUT.byteSize());
    }

    private static long[] ld12$DIMS = { 12 };

    /**
     * Dimensions for array field:
     * {@snippet lang=c :
     * unsigned char ld12[12]
     * }
     */
    public static long[] ld12$dimensions() {
        return ld12$DIMS;
    }
    private static final VarHandle ld12$ELEM_HANDLE = ld12$LAYOUT.varHandle(sequenceElement());

    /**
     * Indexed getter for field:
     * {@snippet lang=c :
     * unsigned char ld12[12]
     * }
     */
    public static byte ld12(MemorySegment struct, long index0) {
        return (byte)ld12$ELEM_HANDLE.get(struct, 0L, index0);
    }

    /**
     * Indexed setter for field:
     * {@snippet lang=c :
     * unsigned char ld12[12]
     * }
     */
    public static void ld12(MemorySegment struct, long index0, byte fieldValue) {
        ld12$ELEM_HANDLE.set(struct, 0L, index0, fieldValue);
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

