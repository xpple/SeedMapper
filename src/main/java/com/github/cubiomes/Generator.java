// Generated by jextract

package com.github.cubiomes;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.Arena;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SequenceLayout;
import java.lang.invoke.MethodHandle;
import java.util.function.Consumer;

import static java.lang.foreign.MemoryLayout.PathElement.*;
import static java.lang.foreign.ValueLayout.*;

/**
 * {@snippet lang=c :
 * struct Generator {
 *     int mc;
 *     int dim;
 *     uint32_t flags;
 *     uint64_t seed;
 *     uint64_t sha;
 *     union {
 *         struct {
 *             LayerStack ls;
 *             Layer xlayer[5];
 *             Layer *entry;
 *         };
 *         struct {
 *             BiomeNoise bn;
 *         };
 *         struct {
 *             BiomeNoiseBeta bnb;
 *         };
 *     };
 *     NetherNoise nn;
 *     EndNoise en;
 * }
 * }
 */
public class Generator {

    Generator() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        Cubiomes.C_INT.withName("mc"),
        Cubiomes.C_INT.withName("dim"),
        Cubiomes.C_INT.withName("flags"),
        MemoryLayout.paddingLayout(4),
        Cubiomes.C_LONG_LONG.withName("seed"),
        Cubiomes.C_LONG_LONG.withName("sha"),
        MemoryLayout.unionLayout(
            MemoryLayout.structLayout(
                LayerStack.layout().withName("ls"),
                MemoryLayout.sequenceLayout(5, Layer.layout()).withName("xlayer"),
                Cubiomes.C_POINTER.withName("entry")
            ).withName("$anon$24:9"),
            MemoryLayout.structLayout(
                BiomeNoise.layout().withName("bn")
            ).withName("$anon$29:9"),
            MemoryLayout.structLayout(
                BiomeNoiseBeta.layout().withName("bnb")
            ).withName("$anon$32:9")
        ).withName("$anon$23:5"),
        NetherNoise.layout().withName("nn"),
        EndNoise.layout().withName("en")
    ).withName("Generator");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
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

    private static final long mc$OFFSET = 0;

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

    private static final OfInt dim$LAYOUT = (OfInt)$LAYOUT.select(groupElement("dim"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * int dim
     * }
     */
    public static final OfInt dim$layout() {
        return dim$LAYOUT;
    }

    private static final long dim$OFFSET = 4;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * int dim
     * }
     */
    public static final long dim$offset() {
        return dim$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * int dim
     * }
     */
    public static int dim(MemorySegment struct) {
        return struct.get(dim$LAYOUT, dim$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * int dim
     * }
     */
    public static void dim(MemorySegment struct, int fieldValue) {
        struct.set(dim$LAYOUT, dim$OFFSET, fieldValue);
    }

    private static final OfInt flags$LAYOUT = (OfInt)$LAYOUT.select(groupElement("flags"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint32_t flags
     * }
     */
    public static final OfInt flags$layout() {
        return flags$LAYOUT;
    }

    private static final long flags$OFFSET = 8;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint32_t flags
     * }
     */
    public static final long flags$offset() {
        return flags$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint32_t flags
     * }
     */
    public static int flags(MemorySegment struct) {
        return struct.get(flags$LAYOUT, flags$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint32_t flags
     * }
     */
    public static void flags(MemorySegment struct, int fieldValue) {
        struct.set(flags$LAYOUT, flags$OFFSET, fieldValue);
    }

    private static final OfLong seed$LAYOUT = (OfLong)$LAYOUT.select(groupElement("seed"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t seed
     * }
     */
    public static final OfLong seed$layout() {
        return seed$LAYOUT;
    }

    private static final long seed$OFFSET = 16;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t seed
     * }
     */
    public static final long seed$offset() {
        return seed$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t seed
     * }
     */
    public static long seed(MemorySegment struct) {
        return struct.get(seed$LAYOUT, seed$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t seed
     * }
     */
    public static void seed(MemorySegment struct, long fieldValue) {
        struct.set(seed$LAYOUT, seed$OFFSET, fieldValue);
    }

    private static final OfLong sha$LAYOUT = (OfLong)$LAYOUT.select(groupElement("sha"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t sha
     * }
     */
    public static final OfLong sha$layout() {
        return sha$LAYOUT;
    }

    private static final long sha$OFFSET = 24;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t sha
     * }
     */
    public static final long sha$offset() {
        return sha$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t sha
     * }
     */
    public static long sha(MemorySegment struct) {
        return struct.get(sha$LAYOUT, sha$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t sha
     * }
     */
    public static void sha(MemorySegment struct, long fieldValue) {
        struct.set(sha$LAYOUT, sha$OFFSET, fieldValue);
    }

    private static final GroupLayout ls$LAYOUT = (GroupLayout)$LAYOUT.select(groupElement("$anon$23:5"), groupElement("$anon$24:9"), groupElement("ls"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * LayerStack ls
     * }
     */
    public static final GroupLayout ls$layout() {
        return ls$LAYOUT;
    }

    private static final long ls$OFFSET = 32;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * LayerStack ls
     * }
     */
    public static final long ls$offset() {
        return ls$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * LayerStack ls
     * }
     */
    public static MemorySegment ls(MemorySegment struct) {
        return struct.asSlice(ls$OFFSET, ls$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * LayerStack ls
     * }
     */
    public static void ls(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, ls$OFFSET, ls$LAYOUT.byteSize());
    }

    private static final SequenceLayout xlayer$LAYOUT = (SequenceLayout)$LAYOUT.select(groupElement("$anon$23:5"), groupElement("$anon$24:9"), groupElement("xlayer"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * Layer xlayer[5]
     * }
     */
    public static final SequenceLayout xlayer$layout() {
        return xlayer$LAYOUT;
    }

    private static final long xlayer$OFFSET = 4784;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * Layer xlayer[5]
     * }
     */
    public static final long xlayer$offset() {
        return xlayer$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * Layer xlayer[5]
     * }
     */
    public static MemorySegment xlayer(MemorySegment struct) {
        return struct.asSlice(xlayer$OFFSET, xlayer$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * Layer xlayer[5]
     * }
     */
    public static void xlayer(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, xlayer$OFFSET, xlayer$LAYOUT.byteSize());
    }

    private static long[] xlayer$DIMS = { 5 };

    /**
     * Dimensions for array field:
     * {@snippet lang=c :
     * Layer xlayer[5]
     * }
     */
    public static long[] xlayer$dimensions() {
        return xlayer$DIMS;
    }
    private static final MethodHandle xlayer$ELEM_HANDLE = xlayer$LAYOUT.sliceHandle(sequenceElement());

    /**
     * Indexed getter for field:
     * {@snippet lang=c :
     * Layer xlayer[5]
     * }
     */
    public static MemorySegment xlayer(MemorySegment struct, long index0) {
        try {
            return (MemorySegment)xlayer$ELEM_HANDLE.invokeExact(struct, 0L, index0);
        } catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    /**
     * Indexed setter for field:
     * {@snippet lang=c :
     * Layer xlayer[5]
     * }
     */
    public static void xlayer(MemorySegment struct, long index0, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, xlayer(struct, index0), 0L, Layer.layout().byteSize());
    }

    private static final AddressLayout entry$LAYOUT = (AddressLayout)$LAYOUT.select(groupElement("$anon$23:5"), groupElement("$anon$24:9"), groupElement("entry"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * Layer *entry
     * }
     */
    public static final AddressLayout entry$layout() {
        return entry$LAYOUT;
    }

    private static final long entry$OFFSET = 5144;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * Layer *entry
     * }
     */
    public static final long entry$offset() {
        return entry$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * Layer *entry
     * }
     */
    public static MemorySegment entry(MemorySegment struct) {
        return struct.get(entry$LAYOUT, entry$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * Layer *entry
     * }
     */
    public static void entry(MemorySegment struct, MemorySegment fieldValue) {
        struct.set(entry$LAYOUT, entry$OFFSET, fieldValue);
    }

    private static final GroupLayout bn$LAYOUT = (GroupLayout)$LAYOUT.select(groupElement("$anon$23:5"), groupElement("$anon$29:9"), groupElement("bn"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * BiomeNoise bn
     * }
     */
    public static final GroupLayout bn$layout() {
        return bn$LAYOUT;
    }

    private static final long bn$OFFSET = 32;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * BiomeNoise bn
     * }
     */
    public static final long bn$offset() {
        return bn$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * BiomeNoise bn
     * }
     */
    public static MemorySegment bn(MemorySegment struct) {
        return struct.asSlice(bn$OFFSET, bn$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * BiomeNoise bn
     * }
     */
    public static void bn(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, bn$OFFSET, bn$LAYOUT.byteSize());
    }

    private static final GroupLayout bnb$LAYOUT = (GroupLayout)$LAYOUT.select(groupElement("$anon$23:5"), groupElement("$anon$32:9"), groupElement("bnb"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * BiomeNoiseBeta bnb
     * }
     */
    public static final GroupLayout bnb$layout() {
        return bnb$LAYOUT;
    }

    private static final long bnb$OFFSET = 32;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * BiomeNoiseBeta bnb
     * }
     */
    public static final long bnb$offset() {
        return bnb$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * BiomeNoiseBeta bnb
     * }
     */
    public static MemorySegment bnb(MemorySegment struct) {
        return struct.asSlice(bnb$OFFSET, bnb$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * BiomeNoiseBeta bnb
     * }
     */
    public static void bnb(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, bnb$OFFSET, bnb$LAYOUT.byteSize());
    }

    private static final GroupLayout nn$LAYOUT = (GroupLayout)$LAYOUT.select(groupElement("nn"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * NetherNoise nn
     * }
     */
    public static final GroupLayout nn$layout() {
        return nn$LAYOUT;
    }

    private static final long nn$OFFSET = 24624;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * NetherNoise nn
     * }
     */
    public static final long nn$offset() {
        return nn$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * NetherNoise nn
     * }
     */
    public static MemorySegment nn(MemorySegment struct) {
        return struct.asSlice(nn$OFFSET, nn$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * NetherNoise nn
     * }
     */
    public static void nn(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, nn$OFFSET, nn$LAYOUT.byteSize());
    }

    private static final GroupLayout en$LAYOUT = (GroupLayout)$LAYOUT.select(groupElement("en"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * EndNoise en
     * }
     */
    public static final GroupLayout en$layout() {
        return en$LAYOUT;
    }

    private static final long en$OFFSET = 27264;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * EndNoise en
     * }
     */
    public static final long en$offset() {
        return en$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * EndNoise en
     * }
     */
    public static MemorySegment en(MemorySegment struct) {
        return struct.asSlice(en$OFFSET, en$LAYOUT.byteSize());
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * EndNoise en
     * }
     */
    public static void en(MemorySegment struct, MemorySegment fieldValue) {
        MemorySegment.copy(fieldValue, 0L, struct, en$OFFSET, en$LAYOUT.byteSize());
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

