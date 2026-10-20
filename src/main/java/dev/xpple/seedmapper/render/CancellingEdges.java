package dev.xpple.seedmapper.render;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/// Two faster implementations of cancelling out shared block edges for rendering.
/// The baseline implementation in [RenderManager] just uses a hash set:
///
/// ```java
/// if (!lines.add(line)) {
///     lines.remove(line);
/// }
/// ```
///
/// The first implementation uses an occupancy bitmap. Each position is mapped to a
/// bit index in a flattened 3D array. The bit at that index is then XORed, which
/// allows for the cancellation behavior. The visibility of an edge then depends on
/// the four blocks that share that edge. That is, the visibility is the XOR of the
/// bits representing those four blocks. Let `B` be the bitmap, and let the strides
/// `dx`, `dy` and `dz` be `1`, `X` and `X * Y` respectively. Then we can compute
///
/// ```
/// EX = B XOR (B << dy) XOR (B << dz) XOR (B << (dy + dz))
/// EY = B XOR (B <<  1) XOR (B << dz) XOR (B <<  (1 + dz))
/// EZ = B XOR (B <<  1) XOR (B << dy) XOR (B <<  (1 + dy))
/// ```
///
/// Here `EX` for example represents a unit edge in the X-direction that should be
/// rendered. using these formulas we thus compute the visibility of an edge for
/// every block simultaneously. However, since the above pseudocode shifts the
/// entire bitmap, the implementation derives the required `long` shifts from the
/// edge descriptors.
///
/// The second implementation is a fallback implementation for cases where the
/// blocks are sparse or very widely spread. This just packs each [Line] as a
/// `long` and uses a [LongOpenHashSet].
public final class CancellingEdges {
    private CancellingEdges() {
    }

    private static final long MAX_BITMAP_VERTICES = 128_000_000;
    private static final long MAX_VERTICES_PER_BLOCK = 64;

    // (x, y, z, axis)
    private static final int[][] BOX_EDGES = {
        {0, 0, 0, 0}, {0, 0, 0, 1}, {0, 0, 0, 2}, {1, 0, 0, 2},
        {1, 0, 1, 1}, {0, 0, 1, 0}, {1, 0, 0, 1}, {0, 1, 0, 0},
        {1, 1, 0, 2}, {0, 0, 1, 1}, {0, 1, 0, 2}, {0, 1, 1, 0}
    };

    /// Returns null for batches that cannot be packed losslessly.
    public static @Nullable List<Line> create(Collection<BlockPos> positions, int color, @Nullable Direction face) {
        if (positions.isEmpty()) {
            return Collections.emptyList();
        }
        Layout layout = Layout.create(positions);
        if (layout == null) {
            return null;
        }
        int[][] edges = edges(face);
        if (layout.volume <= MAX_BITMAP_VERTICES && layout.volume <= Math.max(4096L, MAX_VERTICES_PER_BLOCK * positions.size())) {
            return bitmap(positions, color, edges, layout);
        }
        if (layout.xBits + layout.yBits + layout.zBits <= Long.SIZE - 2) {
            return packed(positions, color, edges, layout);
        }
        return null;
    }

    private static int[][] edges(@Nullable Direction face) {
        if (face == null) {
            return BOX_EDGES;
        }
        return switch (face) {
            case DOWN -> new int[][]{BOX_EDGES[0], BOX_EDGES[2], BOX_EDGES[3], BOX_EDGES[5]};
            case UP -> new int[][]{BOX_EDGES[7], BOX_EDGES[10], BOX_EDGES[8], BOX_EDGES[11]};
            case NORTH -> new int[][]{BOX_EDGES[0], BOX_EDGES[1], BOX_EDGES[6], BOX_EDGES[7]};
            case SOUTH -> new int[][]{BOX_EDGES[5], BOX_EDGES[9], BOX_EDGES[4], BOX_EDGES[11]};
            case WEST -> new int[][]{BOX_EDGES[1], BOX_EDGES[2], BOX_EDGES[9], BOX_EDGES[10]};
            case EAST -> new int[][]{BOX_EDGES[6], BOX_EDGES[3], BOX_EDGES[4], BOX_EDGES[8]};
        };
    }

    private static List<Line> bitmap(Collection<BlockPos> positions, int color, int[][] edges, Layout layout) {
        int words = (int) ((layout.volume + Long.SIZE - 1) >>> 6);
        long[] occupancy = new long[words];
        long yStride = layout.xSize;
        long zStride = layout.xSize * layout.ySize;
        for (BlockPos pos : positions) {
            long index = ((long) pos.getX() - layout.minX)
                + yStride * ((long) pos.getY() - layout.minY)
                + zStride * ((long) pos.getZ() - layout.minZ);
            // xor
            occupancy[(int) (index >>> 6)] ^= 1L << (index & 63);
        }

        List<Line> result = new ArrayList<>();
        for (int axis = 0; axis < 3; axis++) {
            int count = 0;
            for (int[] edge : edges) {
                if (edge[3] == axis) {
                    count++;
                }
            }
            if (count == 0) {
                continue;
            }
            int[] offsets = new int[count];
            int next = 0;
            for (int[] edge : edges) {
                if (edge[3] == axis) {
                    offsets[next++] = (int) (edge[0] + yStride * edge[1] + zStride * edge[2]);
                }
            }
            for (int word = 0; word < words; word++) {
                long visible = 0;
                for (int offset : offsets) {
                    visible ^= shiftedWord(occupancy, word, offset);
                }
                // reconstruct the visible edges from set bits
                while (visible != 0) {
                    long index = ((long) word << 6) + Long.numberOfTrailingZeros(visible);
                    visible &= visible - 1;
                    int x = (int) (index % layout.xSize) + layout.minX;
                    index /= layout.xSize;
                    int y = (int) (index % layout.ySize) + layout.minY;
                    int z = (int) (index / layout.ySize) + layout.minZ;
                    result.add(line(x, y, z, axis, color));
                }
            }
        }
        return result;
    }

    private static long shiftedWord(long[] bits, int word, int offset) {
        int source = word - (offset >>> 6);
        if (source < 0) {
            return 0;
        }
        int shift = offset & 63;
        long value = bits[source] << shift;
        if (shift != 0 && source > 0) {
            value |= bits[source - 1] >>> (Long.SIZE - shift);
        }
        return value;
    }

    private static List<Line> packed(Collection<BlockPos> positions, int color, int[][] edges, Layout layout) {
        LongOpenHashSet parity = new LongOpenHashSet(Math.min(positions.size(), 65536));
        for (BlockPos pos : positions) {
            for (int[] edge : edges) {
                long key = layout.key(pos.getX() + edge[0], pos.getY() + edge[1], pos.getZ() + edge[2], edge[3]);
                if (!parity.add(key)) {
                    parity.remove(key);
                }
            }
        }
        List<Line> result = new ArrayList<>(parity.size());
        for (LongIterator iterator = parity.iterator(); iterator.hasNext();) {
            result.add(layout.line(iterator.nextLong(), color));
        }
        return result;
    }

    private static Line line(int x, int y, int z, int axis, int color) {
        return switch (axis) {
            case 0 -> new Line(new Vec3(x, y, z), new Vec3((double) x + 1, y, z), color);
            case 1 -> new Line(new Vec3(x, y, z), new Vec3(x, (double) y + 1, z), color);
            case 2 -> new Line(new Vec3(x, y, z), new Vec3(x, y, (double) z + 1), color);
            default -> throw new AssertionError();
        };
    }

    private static final class Layout {
        private final int minX;
        private final int minY;
        private final int minZ;
        private final long xSize;
        private final long ySize;
        private final long volume;
        private final int xBits;
        private final int yBits;
        private final int zBits;

        private Layout(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            // pad with zero to prevent shifts wrapping into another row/plane
            this.xSize = (long) maxX - minX + 2;
            this.ySize = (long) maxY - minY + 2;
            long zSize = (long) maxZ - minZ + 2;
            this.xBits = bits(this.xSize);
            this.yBits = bits(this.ySize);
            this.zBits = bits(zSize);
            this.volume = this.xSize <= Long.MAX_VALUE / this.ySize
                && this.xSize * this.ySize <= Long.MAX_VALUE / zSize
                ? this.xSize * this.ySize * zSize : Long.MAX_VALUE;
        }

        private static @Nullable Layout create(Collection<BlockPos> positions) {
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
            for (BlockPos pos : positions) {
                minX = Math.min(minX, pos.getX());
                minY = Math.min(minY, pos.getY());
                minZ = Math.min(minZ, pos.getZ());
                maxX = Math.max(maxX, pos.getX());
                maxY = Math.max(maxY, pos.getY());
                maxZ = Math.max(maxZ, pos.getZ());
            }
            if (maxX == Integer.MAX_VALUE || maxY == Integer.MAX_VALUE || maxZ == Integer.MAX_VALUE) {
                return null;
            }
            return new Layout(minX, minY, minZ, maxX, maxY, maxZ);
        }

        private static int bits(long size) {
            return Long.SIZE - Long.numberOfLeadingZeros(size - 1);
        }

        // compute packed line relative to the minimal coordinate
        private long key(int x, int y, int z, int axis) {
            long position = ((((long) z - this.minZ) << this.yBits) | ((long) y - this.minY)) << this.xBits | ((long) x - this.minX);
            return position << 2 | axis;
        }

        // unpack line
        private Line line(long key, int color) {
            int axis = (int) (key & 3);
            key >>>= 2;
            int x = (int) (key & ((1L << this.xBits) - 1)) + this.minX;
            key >>>= this.xBits;
            int y = (int) (key & ((1L << this.yBits) - 1)) + this.minY;
            key >>>= this.yBits;
            int z = (int) key + this.minZ;
            return CancellingEdges.line(x, y, z, axis, color);
        }
    }
}
