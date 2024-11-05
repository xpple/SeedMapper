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
 * struct PerlinNoise {
 *     uint8_t d[257];
 *     uint8_t h2;
 *     double a;
 *     double b;
 *     double c;
 *     double amplitude;
 *     double lacunarity;
 *     double d2;
 *     double t2;
 * }
 * }
 */
public class PerlinNoise {

    PerlinNoise() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        MemoryLayout.sequenceLayout(257, Cubiomes.C_CHAR).withName("d"),
        Cubiomes.C_CHAR.withName("h2"),
        MemoryLayout.paddingLayout(6),
        Cubiomes.C_DOUBLE.withName("a"),
        Cubiomes.C_DOUBLE.withName("b"),
        Cubiomes.C_DOUBLE.withName("c"),
        Cubiomes.C_DOUBLE.withName("amplitude"),
        Cubiomes.C_DOUBLE.withName("lacunarity"),
        Cubiomes.C_DOUBLE.withName("d2"),
        Cubiomes.C_DOUBLE.withName("t2")
    ).withName("PerlinNoise");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final SequenceLayout d$LAYOUT = (SequenceLayout)$LAYOUT.select(groupElement("d"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint8_t d[257]
     * }
     */
    public static final SequenceLayout d$layout() {
        return d$LAYOUT;
    }

    private static final long d$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint8_t d[257]
     * }
     */
    public static final long d$offset() {
        return d$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint8_t d[257]
     * }
     */
    public static MemorySegment d(MemorySegment struct) {
        return struct.asSlice(d$OFFSET, d$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint8_t d[257]
     * }
     */
    public static void d(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, d$OFFSET, d$LAYOUT.byteSize());
    }

    private static long[] d$DIMS = { 257 };

    /**
     * Dimensions for array field:
     * {@snippet lang=c :
     * uint8_t d[257]
     * }
     */
    public static long[] d$dimensions() {
        return d$DIMS;
    }
    private static final VarHandle d$ELEM_HANDLE = d$LAYOUT.varHandle(sequenceElement());

    /**
     * Indexed getter for field:
     * {@snippet lang=c :
     * uint8_t d[257]
     * }
     */
    public static byte d(MemorySegment struct, long index0) {
        return (byte)d$ELEM_HANDLE.get(struct, 0L, index0);
    }

    /**
     * Indexed setter for field:
     * {@snippet lang=c :
     * uint8_t d[257]
     * }
     */
    public static void d(MemorySegment struct, long index0, byte fieldValue) {
        d$ELEM_HANDLE.set(struct, 0L, index0, fieldValue);
    }

    private static final OfByte h2$LAYOUT = (OfByte)$LAYOUT.select(groupElement("h2"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint8_t h2
     * }
     */
    public static final OfByte h2$layout() {
        return h2$LAYOUT;
    }

    private static final long h2$OFFSET = 257;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint8_t h2
     * }
     */
    public static final long h2$offset() {
        return h2$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint8_t h2
     * }
     */
    public static byte h2(MemorySegment struct) {
        return struct.get(h2$LAYOUT, h2$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint8_t h2
     * }
     */
    public static void h2(MemorySegment struct, byte fieldValue) {
        struct.set(h2$LAYOUT, h2$OFFSET, fieldValue);
    }

    private static final OfDouble a$LAYOUT = (OfDouble)$LAYOUT.select(groupElement("a"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double a
     * }
     */
    public static final OfDouble a$layout() {
        return a$LAYOUT;
    }

    private static final long a$OFFSET = 264;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double a
     * }
     */
    public static final long a$offset() {
        return a$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double a
     * }
     */
    public static double a(MemorySegment struct) {
        return struct.get(a$LAYOUT, a$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double a
     * }
     */
    public static void a(MemorySegment struct, double fieldValue) {
        struct.set(a$LAYOUT, a$OFFSET, fieldValue);
    }

    private static final OfDouble b$LAYOUT = (OfDouble)$LAYOUT.select(groupElement("b"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double b
     * }
     */
    public static final OfDouble b$layout() {
        return b$LAYOUT;
    }

    private static final long b$OFFSET = 272;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double b
     * }
     */
    public static final long b$offset() {
        return b$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double b
     * }
     */
    public static double b(MemorySegment struct) {
        return struct.get(b$LAYOUT, b$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double b
     * }
     */
    public static void b(MemorySegment struct, double fieldValue) {
        struct.set(b$LAYOUT, b$OFFSET, fieldValue);
    }

    private static final OfDouble c$LAYOUT = (OfDouble)$LAYOUT.select(groupElement("c"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double c
     * }
     */
    public static final OfDouble c$layout() {
        return c$LAYOUT;
    }

    private static final long c$OFFSET = 280;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double c
     * }
     */
    public static final long c$offset() {
        return c$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double c
     * }
     */
    public static double c(MemorySegment struct) {
        return struct.get(c$LAYOUT, c$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double c
     * }
     */
    public static void c(MemorySegment struct, double fieldValue) {
        struct.set(c$LAYOUT, c$OFFSET, fieldValue);
    }

    private static final OfDouble amplitude$LAYOUT = (OfDouble)$LAYOUT.select(groupElement("amplitude"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double amplitude
     * }
     */
    public static final OfDouble amplitude$layout() {
        return amplitude$LAYOUT;
    }

    private static final long amplitude$OFFSET = 288;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double amplitude
     * }
     */
    public static final long amplitude$offset() {
        return amplitude$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double amplitude
     * }
     */
    public static double amplitude(MemorySegment struct) {
        return struct.get(amplitude$LAYOUT, amplitude$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double amplitude
     * }
     */
    public static void amplitude(MemorySegment struct, double fieldValue) {
        struct.set(amplitude$LAYOUT, amplitude$OFFSET, fieldValue);
    }

    private static final OfDouble lacunarity$LAYOUT = (OfDouble)$LAYOUT.select(groupElement("lacunarity"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double lacunarity
     * }
     */
    public static final OfDouble lacunarity$layout() {
        return lacunarity$LAYOUT;
    }

    private static final long lacunarity$OFFSET = 296;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double lacunarity
     * }
     */
    public static final long lacunarity$offset() {
        return lacunarity$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double lacunarity
     * }
     */
    public static double lacunarity(MemorySegment struct) {
        return struct.get(lacunarity$LAYOUT, lacunarity$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double lacunarity
     * }
     */
    public static void lacunarity(MemorySegment struct, double fieldValue) {
        struct.set(lacunarity$LAYOUT, lacunarity$OFFSET, fieldValue);
    }

    private static final OfDouble d2$LAYOUT = (OfDouble)$LAYOUT.select(groupElement("d2"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double d2
     * }
     */
    public static final OfDouble d2$layout() {
        return d2$LAYOUT;
    }

    private static final long d2$OFFSET = 304;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double d2
     * }
     */
    public static final long d2$offset() {
        return d2$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double d2
     * }
     */
    public static double d2(MemorySegment struct) {
        return struct.get(d2$LAYOUT, d2$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double d2
     * }
     */
    public static void d2(MemorySegment struct, double fieldValue) {
        struct.set(d2$LAYOUT, d2$OFFSET, fieldValue);
    }

    private static final OfDouble t2$LAYOUT = (OfDouble)$LAYOUT.select(groupElement("t2"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * double t2
     * }
     */
    public static final OfDouble t2$layout() {
        return t2$LAYOUT;
    }

    private static final long t2$OFFSET = 312;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * double t2
     * }
     */
    public static final long t2$offset() {
        return t2$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * double t2
     * }
     */
    public static double t2(MemorySegment struct) {
        return struct.get(t2$LAYOUT, t2$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * double t2
     * }
     */
    public static void t2(MemorySegment struct, double fieldValue) {
        struct.set(t2$LAYOUT, t2$OFFSET, fieldValue);
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

