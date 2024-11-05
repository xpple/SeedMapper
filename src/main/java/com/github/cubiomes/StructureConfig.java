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
 * struct StructureConfig {
 *     int32_t salt;
 *     int8_t regionSize;
 *     int8_t chunkRange;
 *     uint8_t structType;
 *     uint8_t properties;
 *     float rarity;
 * }
 * }
 */
public class StructureConfig {

    StructureConfig() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        Cubiomes.C_INT.withName("salt"),
        Cubiomes.C_CHAR.withName("regionSize"),
        Cubiomes.C_CHAR.withName("chunkRange"),
        Cubiomes.C_CHAR.withName("structType"),
        Cubiomes.C_CHAR.withName("properties"),
        Cubiomes.C_FLOAT.withName("rarity")
    ).withName("StructureConfig");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final OfInt salt$LAYOUT = (OfInt)$LAYOUT.select(groupElement("salt"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * int32_t salt
     * }
     */
    public static final OfInt salt$layout() {
        return salt$LAYOUT;
    }

    private static final long salt$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * int32_t salt
     * }
     */
    public static final long salt$offset() {
        return salt$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * int32_t salt
     * }
     */
    public static int salt(MemorySegment struct) {
        return struct.get(salt$LAYOUT, salt$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * int32_t salt
     * }
     */
    public static void salt(MemorySegment struct, int fieldValue) {
        struct.set(salt$LAYOUT, salt$OFFSET, fieldValue);
    }

    private static final OfByte regionSize$LAYOUT = (OfByte)$LAYOUT.select(groupElement("regionSize"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * int8_t regionSize
     * }
     */
    public static final OfByte regionSize$layout() {
        return regionSize$LAYOUT;
    }

    private static final long regionSize$OFFSET = 4;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * int8_t regionSize
     * }
     */
    public static final long regionSize$offset() {
        return regionSize$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * int8_t regionSize
     * }
     */
    public static byte regionSize(MemorySegment struct) {
        return struct.get(regionSize$LAYOUT, regionSize$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * int8_t regionSize
     * }
     */
    public static void regionSize(MemorySegment struct, byte fieldValue) {
        struct.set(regionSize$LAYOUT, regionSize$OFFSET, fieldValue);
    }

    private static final OfByte chunkRange$LAYOUT = (OfByte)$LAYOUT.select(groupElement("chunkRange"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * int8_t chunkRange
     * }
     */
    public static final OfByte chunkRange$layout() {
        return chunkRange$LAYOUT;
    }

    private static final long chunkRange$OFFSET = 5;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * int8_t chunkRange
     * }
     */
    public static final long chunkRange$offset() {
        return chunkRange$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * int8_t chunkRange
     * }
     */
    public static byte chunkRange(MemorySegment struct) {
        return struct.get(chunkRange$LAYOUT, chunkRange$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * int8_t chunkRange
     * }
     */
    public static void chunkRange(MemorySegment struct, byte fieldValue) {
        struct.set(chunkRange$LAYOUT, chunkRange$OFFSET, fieldValue);
    }

    private static final OfByte structType$LAYOUT = (OfByte)$LAYOUT.select(groupElement("structType"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint8_t structType
     * }
     */
    public static final OfByte structType$layout() {
        return structType$LAYOUT;
    }

    private static final long structType$OFFSET = 6;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint8_t structType
     * }
     */
    public static final long structType$offset() {
        return structType$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint8_t structType
     * }
     */
    public static byte structType(MemorySegment struct) {
        return struct.get(structType$LAYOUT, structType$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint8_t structType
     * }
     */
    public static void structType(MemorySegment struct, byte fieldValue) {
        struct.set(structType$LAYOUT, structType$OFFSET, fieldValue);
    }

    private static final OfByte properties$LAYOUT = (OfByte)$LAYOUT.select(groupElement("properties"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint8_t properties
     * }
     */
    public static final OfByte properties$layout() {
        return properties$LAYOUT;
    }

    private static final long properties$OFFSET = 7;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint8_t properties
     * }
     */
    public static final long properties$offset() {
        return properties$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint8_t properties
     * }
     */
    public static byte properties(MemorySegment struct) {
        return struct.get(properties$LAYOUT, properties$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint8_t properties
     * }
     */
    public static void properties(MemorySegment struct, byte fieldValue) {
        struct.set(properties$LAYOUT, properties$OFFSET, fieldValue);
    }

    private static final OfFloat rarity$LAYOUT = (OfFloat)$LAYOUT.select(groupElement("rarity"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * float rarity
     * }
     */
    public static final OfFloat rarity$layout() {
        return rarity$LAYOUT;
    }

    private static final long rarity$OFFSET = 8;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * float rarity
     * }
     */
    public static final long rarity$offset() {
        return rarity$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * float rarity
     * }
     */
    public static float rarity(MemorySegment struct) {
        return struct.get(rarity$LAYOUT, rarity$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * float rarity
     * }
     */
    public static void rarity(MemorySegment struct, float fieldValue) {
        struct.set(rarity$LAYOUT, rarity$OFFSET, fieldValue);
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

