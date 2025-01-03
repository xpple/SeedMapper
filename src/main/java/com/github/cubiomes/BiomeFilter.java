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
 * struct BiomeFilter {
 *     uint64_t tempsToFind;
 *     uint64_t otempToFind;
 *     uint64_t majorToFind;
 *     uint64_t edgesToFind;
 *     uint64_t raresToFind;
 *     uint64_t raresToFindM;
 *     uint64_t shoreToFind;
 *     uint64_t shoreToFindM;
 *     uint64_t riverToFind;
 *     uint64_t riverToFindM;
 *     uint64_t oceanToFind;
 *     int specialCnt;
 *     uint32_t flags;
 *     uint64_t tempsToExcl;
 *     uint64_t majorToExcl;
 *     uint64_t edgesToExcl;
 *     uint64_t raresToExcl;
 *     uint64_t raresToExclM;
 *     uint64_t shoreToExcl;
 *     uint64_t shoreToExclM;
 *     uint64_t riverToExcl;
 *     uint64_t riverToExclM;
 *     uint64_t biomeToExcl;
 *     uint64_t biomeToExclM;
 *     uint64_t biomeToFind;
 *     uint64_t biomeToFindM;
 *     uint64_t biomeToPick;
 *     uint64_t biomeToPickM;
 * }
 * }
 */
public class BiomeFilter {

    BiomeFilter() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        Cubiomes.C_LONG_LONG.withName("tempsToFind"),
        Cubiomes.C_LONG_LONG.withName("otempToFind"),
        Cubiomes.C_LONG_LONG.withName("majorToFind"),
        Cubiomes.C_LONG_LONG.withName("edgesToFind"),
        Cubiomes.C_LONG_LONG.withName("raresToFind"),
        Cubiomes.C_LONG_LONG.withName("raresToFindM"),
        Cubiomes.C_LONG_LONG.withName("shoreToFind"),
        Cubiomes.C_LONG_LONG.withName("shoreToFindM"),
        Cubiomes.C_LONG_LONG.withName("riverToFind"),
        Cubiomes.C_LONG_LONG.withName("riverToFindM"),
        Cubiomes.C_LONG_LONG.withName("oceanToFind"),
        Cubiomes.C_INT.withName("specialCnt"),
        Cubiomes.C_INT.withName("flags"),
        Cubiomes.C_LONG_LONG.withName("tempsToExcl"),
        Cubiomes.C_LONG_LONG.withName("majorToExcl"),
        Cubiomes.C_LONG_LONG.withName("edgesToExcl"),
        Cubiomes.C_LONG_LONG.withName("raresToExcl"),
        Cubiomes.C_LONG_LONG.withName("raresToExclM"),
        Cubiomes.C_LONG_LONG.withName("shoreToExcl"),
        Cubiomes.C_LONG_LONG.withName("shoreToExclM"),
        Cubiomes.C_LONG_LONG.withName("riverToExcl"),
        Cubiomes.C_LONG_LONG.withName("riverToExclM"),
        Cubiomes.C_LONG_LONG.withName("biomeToExcl"),
        Cubiomes.C_LONG_LONG.withName("biomeToExclM"),
        Cubiomes.C_LONG_LONG.withName("biomeToFind"),
        Cubiomes.C_LONG_LONG.withName("biomeToFindM"),
        Cubiomes.C_LONG_LONG.withName("biomeToPick"),
        Cubiomes.C_LONG_LONG.withName("biomeToPickM")
    ).withName("BiomeFilter");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final OfLong tempsToFind$LAYOUT = (OfLong)$LAYOUT.select(groupElement("tempsToFind"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t tempsToFind
     * }
     */
    public static final OfLong tempsToFind$layout() {
        return tempsToFind$LAYOUT;
    }

    private static final long tempsToFind$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t tempsToFind
     * }
     */
    public static final long tempsToFind$offset() {
        return tempsToFind$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t tempsToFind
     * }
     */
    public static long tempsToFind(MemorySegment struct) {
        return struct.get(tempsToFind$LAYOUT, tempsToFind$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t tempsToFind
     * }
     */
    public static void tempsToFind(MemorySegment struct, long fieldValue) {
        struct.set(tempsToFind$LAYOUT, tempsToFind$OFFSET, fieldValue);
    }

    private static final OfLong otempToFind$LAYOUT = (OfLong)$LAYOUT.select(groupElement("otempToFind"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t otempToFind
     * }
     */
    public static final OfLong otempToFind$layout() {
        return otempToFind$LAYOUT;
    }

    private static final long otempToFind$OFFSET = 8;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t otempToFind
     * }
     */
    public static final long otempToFind$offset() {
        return otempToFind$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t otempToFind
     * }
     */
    public static long otempToFind(MemorySegment struct) {
        return struct.get(otempToFind$LAYOUT, otempToFind$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t otempToFind
     * }
     */
    public static void otempToFind(MemorySegment struct, long fieldValue) {
        struct.set(otempToFind$LAYOUT, otempToFind$OFFSET, fieldValue);
    }

    private static final OfLong majorToFind$LAYOUT = (OfLong)$LAYOUT.select(groupElement("majorToFind"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t majorToFind
     * }
     */
    public static final OfLong majorToFind$layout() {
        return majorToFind$LAYOUT;
    }

    private static final long majorToFind$OFFSET = 16;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t majorToFind
     * }
     */
    public static final long majorToFind$offset() {
        return majorToFind$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t majorToFind
     * }
     */
    public static long majorToFind(MemorySegment struct) {
        return struct.get(majorToFind$LAYOUT, majorToFind$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t majorToFind
     * }
     */
    public static void majorToFind(MemorySegment struct, long fieldValue) {
        struct.set(majorToFind$LAYOUT, majorToFind$OFFSET, fieldValue);
    }

    private static final OfLong edgesToFind$LAYOUT = (OfLong)$LAYOUT.select(groupElement("edgesToFind"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t edgesToFind
     * }
     */
    public static final OfLong edgesToFind$layout() {
        return edgesToFind$LAYOUT;
    }

    private static final long edgesToFind$OFFSET = 24;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t edgesToFind
     * }
     */
    public static final long edgesToFind$offset() {
        return edgesToFind$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t edgesToFind
     * }
     */
    public static long edgesToFind(MemorySegment struct) {
        return struct.get(edgesToFind$LAYOUT, edgesToFind$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t edgesToFind
     * }
     */
    public static void edgesToFind(MemorySegment struct, long fieldValue) {
        struct.set(edgesToFind$LAYOUT, edgesToFind$OFFSET, fieldValue);
    }

    private static final OfLong raresToFind$LAYOUT = (OfLong)$LAYOUT.select(groupElement("raresToFind"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t raresToFind
     * }
     */
    public static final OfLong raresToFind$layout() {
        return raresToFind$LAYOUT;
    }

    private static final long raresToFind$OFFSET = 32;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t raresToFind
     * }
     */
    public static final long raresToFind$offset() {
        return raresToFind$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t raresToFind
     * }
     */
    public static long raresToFind(MemorySegment struct) {
        return struct.get(raresToFind$LAYOUT, raresToFind$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t raresToFind
     * }
     */
    public static void raresToFind(MemorySegment struct, long fieldValue) {
        struct.set(raresToFind$LAYOUT, raresToFind$OFFSET, fieldValue);
    }

    private static final OfLong raresToFindM$LAYOUT = (OfLong)$LAYOUT.select(groupElement("raresToFindM"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t raresToFindM
     * }
     */
    public static final OfLong raresToFindM$layout() {
        return raresToFindM$LAYOUT;
    }

    private static final long raresToFindM$OFFSET = 40;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t raresToFindM
     * }
     */
    public static final long raresToFindM$offset() {
        return raresToFindM$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t raresToFindM
     * }
     */
    public static long raresToFindM(MemorySegment struct) {
        return struct.get(raresToFindM$LAYOUT, raresToFindM$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t raresToFindM
     * }
     */
    public static void raresToFindM(MemorySegment struct, long fieldValue) {
        struct.set(raresToFindM$LAYOUT, raresToFindM$OFFSET, fieldValue);
    }

    private static final OfLong shoreToFind$LAYOUT = (OfLong)$LAYOUT.select(groupElement("shoreToFind"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t shoreToFind
     * }
     */
    public static final OfLong shoreToFind$layout() {
        return shoreToFind$LAYOUT;
    }

    private static final long shoreToFind$OFFSET = 48;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t shoreToFind
     * }
     */
    public static final long shoreToFind$offset() {
        return shoreToFind$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t shoreToFind
     * }
     */
    public static long shoreToFind(MemorySegment struct) {
        return struct.get(shoreToFind$LAYOUT, shoreToFind$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t shoreToFind
     * }
     */
    public static void shoreToFind(MemorySegment struct, long fieldValue) {
        struct.set(shoreToFind$LAYOUT, shoreToFind$OFFSET, fieldValue);
    }

    private static final OfLong shoreToFindM$LAYOUT = (OfLong)$LAYOUT.select(groupElement("shoreToFindM"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t shoreToFindM
     * }
     */
    public static final OfLong shoreToFindM$layout() {
        return shoreToFindM$LAYOUT;
    }

    private static final long shoreToFindM$OFFSET = 56;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t shoreToFindM
     * }
     */
    public static final long shoreToFindM$offset() {
        return shoreToFindM$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t shoreToFindM
     * }
     */
    public static long shoreToFindM(MemorySegment struct) {
        return struct.get(shoreToFindM$LAYOUT, shoreToFindM$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t shoreToFindM
     * }
     */
    public static void shoreToFindM(MemorySegment struct, long fieldValue) {
        struct.set(shoreToFindM$LAYOUT, shoreToFindM$OFFSET, fieldValue);
    }

    private static final OfLong riverToFind$LAYOUT = (OfLong)$LAYOUT.select(groupElement("riverToFind"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t riverToFind
     * }
     */
    public static final OfLong riverToFind$layout() {
        return riverToFind$LAYOUT;
    }

    private static final long riverToFind$OFFSET = 64;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t riverToFind
     * }
     */
    public static final long riverToFind$offset() {
        return riverToFind$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t riverToFind
     * }
     */
    public static long riverToFind(MemorySegment struct) {
        return struct.get(riverToFind$LAYOUT, riverToFind$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t riverToFind
     * }
     */
    public static void riverToFind(MemorySegment struct, long fieldValue) {
        struct.set(riverToFind$LAYOUT, riverToFind$OFFSET, fieldValue);
    }

    private static final OfLong riverToFindM$LAYOUT = (OfLong)$LAYOUT.select(groupElement("riverToFindM"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t riverToFindM
     * }
     */
    public static final OfLong riverToFindM$layout() {
        return riverToFindM$LAYOUT;
    }

    private static final long riverToFindM$OFFSET = 72;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t riverToFindM
     * }
     */
    public static final long riverToFindM$offset() {
        return riverToFindM$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t riverToFindM
     * }
     */
    public static long riverToFindM(MemorySegment struct) {
        return struct.get(riverToFindM$LAYOUT, riverToFindM$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t riverToFindM
     * }
     */
    public static void riverToFindM(MemorySegment struct, long fieldValue) {
        struct.set(riverToFindM$LAYOUT, riverToFindM$OFFSET, fieldValue);
    }

    private static final OfLong oceanToFind$LAYOUT = (OfLong)$LAYOUT.select(groupElement("oceanToFind"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t oceanToFind
     * }
     */
    public static final OfLong oceanToFind$layout() {
        return oceanToFind$LAYOUT;
    }

    private static final long oceanToFind$OFFSET = 80;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t oceanToFind
     * }
     */
    public static final long oceanToFind$offset() {
        return oceanToFind$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t oceanToFind
     * }
     */
    public static long oceanToFind(MemorySegment struct) {
        return struct.get(oceanToFind$LAYOUT, oceanToFind$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t oceanToFind
     * }
     */
    public static void oceanToFind(MemorySegment struct, long fieldValue) {
        struct.set(oceanToFind$LAYOUT, oceanToFind$OFFSET, fieldValue);
    }

    private static final OfInt specialCnt$LAYOUT = (OfInt)$LAYOUT.select(groupElement("specialCnt"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * int specialCnt
     * }
     */
    public static final OfInt specialCnt$layout() {
        return specialCnt$LAYOUT;
    }

    private static final long specialCnt$OFFSET = 88;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * int specialCnt
     * }
     */
    public static final long specialCnt$offset() {
        return specialCnt$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * int specialCnt
     * }
     */
    public static int specialCnt(MemorySegment struct) {
        return struct.get(specialCnt$LAYOUT, specialCnt$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * int specialCnt
     * }
     */
    public static void specialCnt(MemorySegment struct, int fieldValue) {
        struct.set(specialCnt$LAYOUT, specialCnt$OFFSET, fieldValue);
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

    private static final long flags$OFFSET = 92;

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

    private static final OfLong tempsToExcl$LAYOUT = (OfLong)$LAYOUT.select(groupElement("tempsToExcl"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t tempsToExcl
     * }
     */
    public static final OfLong tempsToExcl$layout() {
        return tempsToExcl$LAYOUT;
    }

    private static final long tempsToExcl$OFFSET = 96;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t tempsToExcl
     * }
     */
    public static final long tempsToExcl$offset() {
        return tempsToExcl$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t tempsToExcl
     * }
     */
    public static long tempsToExcl(MemorySegment struct) {
        return struct.get(tempsToExcl$LAYOUT, tempsToExcl$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t tempsToExcl
     * }
     */
    public static void tempsToExcl(MemorySegment struct, long fieldValue) {
        struct.set(tempsToExcl$LAYOUT, tempsToExcl$OFFSET, fieldValue);
    }

    private static final OfLong majorToExcl$LAYOUT = (OfLong)$LAYOUT.select(groupElement("majorToExcl"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t majorToExcl
     * }
     */
    public static final OfLong majorToExcl$layout() {
        return majorToExcl$LAYOUT;
    }

    private static final long majorToExcl$OFFSET = 104;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t majorToExcl
     * }
     */
    public static final long majorToExcl$offset() {
        return majorToExcl$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t majorToExcl
     * }
     */
    public static long majorToExcl(MemorySegment struct) {
        return struct.get(majorToExcl$LAYOUT, majorToExcl$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t majorToExcl
     * }
     */
    public static void majorToExcl(MemorySegment struct, long fieldValue) {
        struct.set(majorToExcl$LAYOUT, majorToExcl$OFFSET, fieldValue);
    }

    private static final OfLong edgesToExcl$LAYOUT = (OfLong)$LAYOUT.select(groupElement("edgesToExcl"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t edgesToExcl
     * }
     */
    public static final OfLong edgesToExcl$layout() {
        return edgesToExcl$LAYOUT;
    }

    private static final long edgesToExcl$OFFSET = 112;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t edgesToExcl
     * }
     */
    public static final long edgesToExcl$offset() {
        return edgesToExcl$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t edgesToExcl
     * }
     */
    public static long edgesToExcl(MemorySegment struct) {
        return struct.get(edgesToExcl$LAYOUT, edgesToExcl$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t edgesToExcl
     * }
     */
    public static void edgesToExcl(MemorySegment struct, long fieldValue) {
        struct.set(edgesToExcl$LAYOUT, edgesToExcl$OFFSET, fieldValue);
    }

    private static final OfLong raresToExcl$LAYOUT = (OfLong)$LAYOUT.select(groupElement("raresToExcl"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t raresToExcl
     * }
     */
    public static final OfLong raresToExcl$layout() {
        return raresToExcl$LAYOUT;
    }

    private static final long raresToExcl$OFFSET = 120;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t raresToExcl
     * }
     */
    public static final long raresToExcl$offset() {
        return raresToExcl$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t raresToExcl
     * }
     */
    public static long raresToExcl(MemorySegment struct) {
        return struct.get(raresToExcl$LAYOUT, raresToExcl$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t raresToExcl
     * }
     */
    public static void raresToExcl(MemorySegment struct, long fieldValue) {
        struct.set(raresToExcl$LAYOUT, raresToExcl$OFFSET, fieldValue);
    }

    private static final OfLong raresToExclM$LAYOUT = (OfLong)$LAYOUT.select(groupElement("raresToExclM"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t raresToExclM
     * }
     */
    public static final OfLong raresToExclM$layout() {
        return raresToExclM$LAYOUT;
    }

    private static final long raresToExclM$OFFSET = 128;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t raresToExclM
     * }
     */
    public static final long raresToExclM$offset() {
        return raresToExclM$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t raresToExclM
     * }
     */
    public static long raresToExclM(MemorySegment struct) {
        return struct.get(raresToExclM$LAYOUT, raresToExclM$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t raresToExclM
     * }
     */
    public static void raresToExclM(MemorySegment struct, long fieldValue) {
        struct.set(raresToExclM$LAYOUT, raresToExclM$OFFSET, fieldValue);
    }

    private static final OfLong shoreToExcl$LAYOUT = (OfLong)$LAYOUT.select(groupElement("shoreToExcl"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t shoreToExcl
     * }
     */
    public static final OfLong shoreToExcl$layout() {
        return shoreToExcl$LAYOUT;
    }

    private static final long shoreToExcl$OFFSET = 136;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t shoreToExcl
     * }
     */
    public static final long shoreToExcl$offset() {
        return shoreToExcl$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t shoreToExcl
     * }
     */
    public static long shoreToExcl(MemorySegment struct) {
        return struct.get(shoreToExcl$LAYOUT, shoreToExcl$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t shoreToExcl
     * }
     */
    public static void shoreToExcl(MemorySegment struct, long fieldValue) {
        struct.set(shoreToExcl$LAYOUT, shoreToExcl$OFFSET, fieldValue);
    }

    private static final OfLong shoreToExclM$LAYOUT = (OfLong)$LAYOUT.select(groupElement("shoreToExclM"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t shoreToExclM
     * }
     */
    public static final OfLong shoreToExclM$layout() {
        return shoreToExclM$LAYOUT;
    }

    private static final long shoreToExclM$OFFSET = 144;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t shoreToExclM
     * }
     */
    public static final long shoreToExclM$offset() {
        return shoreToExclM$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t shoreToExclM
     * }
     */
    public static long shoreToExclM(MemorySegment struct) {
        return struct.get(shoreToExclM$LAYOUT, shoreToExclM$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t shoreToExclM
     * }
     */
    public static void shoreToExclM(MemorySegment struct, long fieldValue) {
        struct.set(shoreToExclM$LAYOUT, shoreToExclM$OFFSET, fieldValue);
    }

    private static final OfLong riverToExcl$LAYOUT = (OfLong)$LAYOUT.select(groupElement("riverToExcl"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t riverToExcl
     * }
     */
    public static final OfLong riverToExcl$layout() {
        return riverToExcl$LAYOUT;
    }

    private static final long riverToExcl$OFFSET = 152;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t riverToExcl
     * }
     */
    public static final long riverToExcl$offset() {
        return riverToExcl$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t riverToExcl
     * }
     */
    public static long riverToExcl(MemorySegment struct) {
        return struct.get(riverToExcl$LAYOUT, riverToExcl$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t riverToExcl
     * }
     */
    public static void riverToExcl(MemorySegment struct, long fieldValue) {
        struct.set(riverToExcl$LAYOUT, riverToExcl$OFFSET, fieldValue);
    }

    private static final OfLong riverToExclM$LAYOUT = (OfLong)$LAYOUT.select(groupElement("riverToExclM"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t riverToExclM
     * }
     */
    public static final OfLong riverToExclM$layout() {
        return riverToExclM$LAYOUT;
    }

    private static final long riverToExclM$OFFSET = 160;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t riverToExclM
     * }
     */
    public static final long riverToExclM$offset() {
        return riverToExclM$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t riverToExclM
     * }
     */
    public static long riverToExclM(MemorySegment struct) {
        return struct.get(riverToExclM$LAYOUT, riverToExclM$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t riverToExclM
     * }
     */
    public static void riverToExclM(MemorySegment struct, long fieldValue) {
        struct.set(riverToExclM$LAYOUT, riverToExclM$OFFSET, fieldValue);
    }

    private static final OfLong biomeToExcl$LAYOUT = (OfLong)$LAYOUT.select(groupElement("biomeToExcl"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t biomeToExcl
     * }
     */
    public static final OfLong biomeToExcl$layout() {
        return biomeToExcl$LAYOUT;
    }

    private static final long biomeToExcl$OFFSET = 168;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t biomeToExcl
     * }
     */
    public static final long biomeToExcl$offset() {
        return biomeToExcl$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t biomeToExcl
     * }
     */
    public static long biomeToExcl(MemorySegment struct) {
        return struct.get(biomeToExcl$LAYOUT, biomeToExcl$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t biomeToExcl
     * }
     */
    public static void biomeToExcl(MemorySegment struct, long fieldValue) {
        struct.set(biomeToExcl$LAYOUT, biomeToExcl$OFFSET, fieldValue);
    }

    private static final OfLong biomeToExclM$LAYOUT = (OfLong)$LAYOUT.select(groupElement("biomeToExclM"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t biomeToExclM
     * }
     */
    public static final OfLong biomeToExclM$layout() {
        return biomeToExclM$LAYOUT;
    }

    private static final long biomeToExclM$OFFSET = 176;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t biomeToExclM
     * }
     */
    public static final long biomeToExclM$offset() {
        return biomeToExclM$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t biomeToExclM
     * }
     */
    public static long biomeToExclM(MemorySegment struct) {
        return struct.get(biomeToExclM$LAYOUT, biomeToExclM$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t biomeToExclM
     * }
     */
    public static void biomeToExclM(MemorySegment struct, long fieldValue) {
        struct.set(biomeToExclM$LAYOUT, biomeToExclM$OFFSET, fieldValue);
    }

    private static final OfLong biomeToFind$LAYOUT = (OfLong)$LAYOUT.select(groupElement("biomeToFind"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t biomeToFind
     * }
     */
    public static final OfLong biomeToFind$layout() {
        return biomeToFind$LAYOUT;
    }

    private static final long biomeToFind$OFFSET = 184;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t biomeToFind
     * }
     */
    public static final long biomeToFind$offset() {
        return biomeToFind$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t biomeToFind
     * }
     */
    public static long biomeToFind(MemorySegment struct) {
        return struct.get(biomeToFind$LAYOUT, biomeToFind$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t biomeToFind
     * }
     */
    public static void biomeToFind(MemorySegment struct, long fieldValue) {
        struct.set(biomeToFind$LAYOUT, biomeToFind$OFFSET, fieldValue);
    }

    private static final OfLong biomeToFindM$LAYOUT = (OfLong)$LAYOUT.select(groupElement("biomeToFindM"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t biomeToFindM
     * }
     */
    public static final OfLong biomeToFindM$layout() {
        return biomeToFindM$LAYOUT;
    }

    private static final long biomeToFindM$OFFSET = 192;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t biomeToFindM
     * }
     */
    public static final long biomeToFindM$offset() {
        return biomeToFindM$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t biomeToFindM
     * }
     */
    public static long biomeToFindM(MemorySegment struct) {
        return struct.get(biomeToFindM$LAYOUT, biomeToFindM$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t biomeToFindM
     * }
     */
    public static void biomeToFindM(MemorySegment struct, long fieldValue) {
        struct.set(biomeToFindM$LAYOUT, biomeToFindM$OFFSET, fieldValue);
    }

    private static final OfLong biomeToPick$LAYOUT = (OfLong)$LAYOUT.select(groupElement("biomeToPick"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t biomeToPick
     * }
     */
    public static final OfLong biomeToPick$layout() {
        return biomeToPick$LAYOUT;
    }

    private static final long biomeToPick$OFFSET = 200;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t biomeToPick
     * }
     */
    public static final long biomeToPick$offset() {
        return biomeToPick$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t biomeToPick
     * }
     */
    public static long biomeToPick(MemorySegment struct) {
        return struct.get(biomeToPick$LAYOUT, biomeToPick$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t biomeToPick
     * }
     */
    public static void biomeToPick(MemorySegment struct, long fieldValue) {
        struct.set(biomeToPick$LAYOUT, biomeToPick$OFFSET, fieldValue);
    }

    private static final OfLong biomeToPickM$LAYOUT = (OfLong)$LAYOUT.select(groupElement("biomeToPickM"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * uint64_t biomeToPickM
     * }
     */
    public static final OfLong biomeToPickM$layout() {
        return biomeToPickM$LAYOUT;
    }

    private static final long biomeToPickM$OFFSET = 208;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * uint64_t biomeToPickM
     * }
     */
    public static final long biomeToPickM$offset() {
        return biomeToPickM$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * uint64_t biomeToPickM
     * }
     */
    public static long biomeToPickM(MemorySegment struct) {
        return struct.get(biomeToPickM$LAYOUT, biomeToPickM$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * uint64_t biomeToPickM
     * }
     */
    public static void biomeToPickM(MemorySegment struct, long fieldValue) {
        struct.set(biomeToPickM$LAYOUT, biomeToPickM$OFFSET, fieldValue);
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

