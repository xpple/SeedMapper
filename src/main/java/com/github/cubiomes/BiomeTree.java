// Generated by jextract

package com.github.cubiomes;

import java.lang.foreign.AddressLayout;
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
 * struct BiomeTree {
 *     const uint32_t *steps;
 *     const int32_t *param;
 *     const uint64_t *nodes;
 *     uint32_t order;
 *     uint32_t len;
 * }
 * }
 */
public class BiomeTree {

    BiomeTree() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        Cubiomes.C_POINTER.withName("steps"),
        Cubiomes.C_POINTER.withName("param"),
        Cubiomes.C_POINTER.withName("nodes"),
        Cubiomes.C_INT.withName("order"),
        Cubiomes.C_INT.withName("len")
    ).withName("BiomeTree");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final AddressLayout steps$LAYOUT = (AddressLayout)$LAYOUT.select(groupElement("steps"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * const uint32_t *steps
     * }
     */
    public static final AddressLayout steps$layout() {
        return steps$LAYOUT;
    }

    private static final long steps$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * const uint32_t *steps
     * }
     */
    public static final long steps$offset() {
        return steps$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * const uint32_t *steps
     * }
     */
    public static MemorySegment steps(MemorySegment struct) {
        return struct.get(steps$LAYOUT, steps$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * const uint32_t *steps
     * }
     */
    public static void steps(MemorySegment struct, MemorySegment fieldValue) {
        struct.set(steps$LAYOUT, steps$OFFSET, fieldValue);
    }

    private static final AddressLayout param$LAYOUT = (AddressLayout)$LAYOUT.select(groupElement("param"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * const int32_t *param
     * }
     */
    public static final AddressLayout param$layout() {
        return param$LAYOUT;
    }

    private static final long param$OFFSET = 8;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * const int32_t *param
     * }
     */
    public static final long param$offset() {
        return param$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * const int32_t *param
     * }
     */
    public static MemorySegment param(MemorySegment struct) {
        return struct.get(param$LAYOUT, param$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * const int32_t *param
     * }
     */
    public static void param(MemorySegment struct, MemorySegment fieldValue) {
        struct.set(param$LAYOUT, param$OFFSET, fieldValue);
    }

    private static final AddressLayout nodes$LAYOUT = (AddressLayout)$LAYOUT.select(groupElement("nodes"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * const uint64_t *nodes
     * }
     */
    public static final AddressLayout nodes$layout() {
        return nodes$LAYOUT;
    }

    private static final long nodes$OFFSET = 16;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * const uint64_t *nodes
     * }
     */
    public static final long nodes$offset() {
        return nodes$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * const uint64_t *nodes
     * }
     */
    public static MemorySegment nodes(MemorySegment struct) {
        return struct.get(nodes$LAYOUT, nodes$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * const uint64_t *nodes
     * }
     */
    public static void nodes(MemorySegment struct, MemorySegment fieldValue) {
        struct.set(nodes$LAYOUT, nodes$OFFSET, fieldValue);
    }

    private static final OfInt order$LAYOUT = (OfInt)$LAYOUT.select(groupElement("order"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint32_t order
     * }
     */
    public static final OfInt order$layout() {
        return order$LAYOUT;
    }

    private static final long order$OFFSET = 24;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint32_t order
     * }
     */
    public static final long order$offset() {
        return order$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint32_t order
     * }
     */
    public static int order(MemorySegment struct) {
        return struct.get(order$LAYOUT, order$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint32_t order
     * }
     */
    public static void order(MemorySegment struct, int fieldValue) {
        struct.set(order$LAYOUT, order$OFFSET, fieldValue);
    }

    private static final OfInt len$LAYOUT = (OfInt)$LAYOUT.select(groupElement("len"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint32_t len
     * }
     */
    public static final OfInt len$layout() {
        return len$LAYOUT;
    }

    private static final long len$OFFSET = 28;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint32_t len
     * }
     */
    public static final long len$offset() {
        return len$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint32_t len
     * }
     */
    public static int len(MemorySegment struct) {
        return struct.get(len$LAYOUT, len$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint32_t len
     * }
     */
    public static void len(MemorySegment struct, int fieldValue) {
        struct.set(len$LAYOUT, len$OFFSET, fieldValue);
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

