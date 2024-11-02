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
 * struct EndNoise {
 *     PerlinNoise perlin;
 *     int mc;
 * }
 * }
 */
public class EndNoise {

    EndNoise() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        PerlinNoise.layout().withName("perlin"),
        CubiomesHeaders.C_INT.withName("mc"),
        MemoryLayout.paddingLayout(4)
    ).withName("EndNoise");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final GroupLayout perlin$LAYOUT = (GroupLayout)$LAYOUT.select(groupElement("perlin"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * PerlinNoise perlin
     * }
     */
    public static final GroupLayout perlin$layout() {
        return perlin$LAYOUT;
    }

    private static final long perlin$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * PerlinNoise perlin
     * }
     */
    public static final long perlin$offset() {
        return perlin$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * PerlinNoise perlin
     * }
     */
    public static MemorySegment perlin(MemorySegment struct) {
        return struct.asSlice(perlin$OFFSET, perlin$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * PerlinNoise perlin
     * }
     */
    public static void perlin(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, perlin$OFFSET, perlin$LAYOUT.byteSize());
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

    private static final long mc$OFFSET = 320;

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

