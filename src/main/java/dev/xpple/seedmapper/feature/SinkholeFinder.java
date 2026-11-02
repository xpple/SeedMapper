package dev.xpple.seedmapper.feature;

import com.github.cubiomes.BiomeNoise;
import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.DoublePerlinNoise;
import com.github.cubiomes.OctaveNoise;
import com.github.cubiomes.PerlinNoise;
import com.github.cubiomes.TerrainNoise;
import dev.xpple.seedmapper.thread.LocatorThreadHelper;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.function.DoubleBinaryOperator;

/// Finds "sinkholes", which are formations that occur at very low depth values, where
/// additionally the weirdness climate is extremely high in magnitude. The resulting
/// terrain forms a very large open pit with steep sides. Up until 1.19.2 they would
/// generate with lava pits in the center. After 1.19.2 the lava no longer generates
/// but the large water lakes remain.
///
/// The implementation is based on M33_Galaxy's CUDA sinkhole program. This code
/// searches _within_ seeds, whereas his searches _for_ seeds.
public final class SinkholeFinder {
    private SinkholeFinder() {
    }

    private static final int WORLD_LIMIT = Level.MAX_LEVEL_SIZE;

    // weirdness octave 0A repeats every 131072 blocks along each axis
    private static final int TILE = 32_768;
    private static final int BLOCK_TILE = TILE * 4;

    // blocks around the raw climate peak
    private static final int OPENING_RADIUS = 32;
    private static final int REFINE_MARGIN = (32 + 8 + 2) * 4 + OPENING_RADIUS;

    private static final double B_SCALE = 337.0 / 331.0;

    private static final int MAX_GRADIENT_FAILURES = 3;

    private static final double A_PEAK_MIN = 0.65;
    private static final double PARTIAL_WEIRDNESS_MIN = 1.15;
    private static final double COARSE_WEIRDNESS_MIN = 1.4;
    private static final double WEIRDNESS_MIN = 1.5;
    private static final double CANDIDATE_DEPTH_MAX = 0.0;

    private static final int WORLD_MIN_Y = -64;
    private static final int NO_SOLID_FLOOR = WORLD_MIN_Y - 1;
    // reaches the default lava level
    private static final int MAX_FLOOR_Y = -54;

    // gradient masks for octave A0
    // corner index = dx + 2*dz + 4*dy
    private static final int[] POSITIVE = {
        0x0111, 0x0122, 0x0441, 0x0482, 0x0214, 0x0228, 0x0844, 0x0888
    };
    private static final int[] NEGATIVE = {
        0x0888, 0x0844, 0x0228, 0x0214, 0x0482, 0x0441, 0x0122, 0x0111
    };
    private static final int[] GRADIENT = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 0, 9, 1, 11
    };

    public static Optional<Hit> find(long seed, int version, boolean largeBiomes, int blockX, int blockZ) {
        assert version >= Cubiomes.MC_1_18();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment bn = BiomeNoise.allocate(arena);
            Cubiomes.initBiomeNoise(bn, version);
            Cubiomes.setClimateParaSeed(bn, seed, largeBiomes ? 1 : 0, Cubiomes.NP_DEPTH(), -1);
            MemorySegment w = climate(bn, Cubiomes.NP_WEIRDNESS());
            MemorySegment a = DoublePerlinNoise.octA(w);
            double amplitude = DoublePerlinNoise.amplitude(w);

            List<Peak> templates = templates(a, amplitude);
            if (templates.isEmpty()) {
                return Optional.empty();
            }

            // estimate weirdness using A0, A1, A2 and B0, B1
            MemorySegment b = DoublePerlinNoise.octB(w);
            MemorySegment bFirstTwo = OctaveNoise.allocate(arena);
            OctaveNoise.octcnt(bFirstTwo, 2);
            OctaveNoise.octaves(bFirstTwo, OctaveNoise.octaves(b));

            PriorityQueue<Tile> tiles = tiles(blockX, blockZ);
            long bestDistance = Long.MAX_VALUE;
            Hit best = null;
            MemorySegment terrain = MemorySegment.NULL;
            DoubleBinaryOperator weirdness = (x, z) -> Cubiomes.sampleDoublePerlin(w, x, 0, z);
            while (!tiles.isEmpty()) {
                LocatorThreadHelper.checkCancelled();
                Tile tile = tiles.remove();
                if (tile.distanceSquared >= bestDistance) {
                    break;
                }
                for (Peak template : templates) {
                    LocatorThreadHelper.checkCancelled();
                    int qx = tile.x * TILE + template.x;
                    int qz = tile.z * TILE + template.z;
                    double partial = template.value + amplitude * Cubiomes.sampleOctave(bFirstTwo, qx * B_SCALE, 0, qz * B_SCALE);
                    if (Math.abs(partial) < PARTIAL_WEIRDNESS_MIN) {
                        continue;
                    }
                    Peak peak = grid(weirdness, qx, qz, 32, 8, 0);
                    if (Math.abs(peak.value) < COARSE_WEIRDNESS_MIN) {
                        continue;
                    }
                    peak = grid(weirdness, peak.x, peak.z, 8, 2, 0);
                    peak = grid(weirdness, peak.x, peak.z, 2, 1, 0);
                    if (Math.abs(peak.value) < WEIRDNESS_MIN) {
                        continue;
                    }
                    int x = peak.x * 4;
                    int z = peak.z * 4;
                    long dx = axisDistance(blockX, x - OPENING_RADIUS, x + OPENING_RADIUS);
                    long dz = axisDistance(blockZ, z - OPENING_RADIUS, z + OPENING_RADIUS);
                    if (dx * dx + dz * dz >= bestDistance) {
                        continue;
                    }
                    double depth = Cubiomes.sampleClimatePara(bn, MemorySegment.NULL, peak.x, peak.z);
                    if (depth >= CANDIDATE_DEPTH_MAX) {
                        continue;
                    }
                    if (terrain.equals(MemorySegment.NULL)) {
                        terrain = TerrainNoise.allocate(arena);
                        Cubiomes.setupTerrainNoise(terrain, version, largeBiomes ? Cubiomes.LARGE_BIOMES() : 0);
                        Cubiomes.initTerrainNoise(terrain, seed, Cubiomes.DIM_OVERWORLD());
                    }
                    Hit opening = surfaceOpening(terrain, x, z, peak.value, depth, blockX, blockZ, bestDistance);
                    if (opening != null) {
                        best = opening;
                        bestDistance = distanceSquared(blockX, blockZ, opening.x, opening.z);
                    }
                }
            }
            return Optional.ofNullable(best);
        }
    }

    private static MemorySegment climate(MemorySegment bn, int parameter) {
        return BiomeNoise.climate(bn).asSlice(parameter * DoublePerlinNoise.sizeof(), DoublePerlinNoise.sizeof());
    }

    private static List<Peak> templates(MemorySegment a, double amplitude) {
        MemorySegment first = OctaveNoise.octaves(a).reinterpret(PerlinNoise.sizeof());
        byte[] permutation = PerlinNoise.d(first).toArray(Cubiomes.C_CHAR);
        int y = (int) Math.floor(PerlinNoise.b(first));
        double offsetX = PerlinNoise.a(first);
        double offsetZ = PerlinNoise.c(first);
        double frequency = PerlinNoise.lacunarity(first);
        if (OctaveNoise.octcnt(a) != 3 || Math.abs(frequency - 1.0 / 128.0) > 1e-15) {
            throw new IllegalStateException("Unsupported weirdness noise configuration");
        }
        DoubleBinaryOperator sampleA = (x, z) -> amplitude * Cubiomes.sampleOctave(a, x, 0, z);
        List<Peak> result = new ArrayList<>();
        LongSet seen = new LongOpenHashSet();
        // visit cells 0..255, the upper corner of cell 255 wraps to lattice index 0
        for (int x = 0; x < 256; x++) {
            LocatorThreadHelper.checkCancelled();
            for (int z = 0; z < 256; z++) {
                int sign = gradientSign(permutation, x, y, z);
                if (sign == 0) {
                    continue;
                }
                int qx = (int) Math.round((x - offsetX + 0.5) / frequency);
                int qz = (int) Math.round((z - offsetZ + 0.5) / frequency);
                // search the A0 lattice cell, then refine
                Peak p = grid(sampleA, qx, qz, 64, 8, sign);
                p = grid(sampleA, p.x, p.z, 8, 2, sign);
                p = grid(sampleA, p.x, p.z, 2, 1, sign);
                if (Math.abs(p.value) < A_PEAK_MIN) {
                    continue;
                }
                int px = Math.floorMod(p.x, TILE);
                int pz = Math.floorMod(p.z, TILE);
                long key = ((long) px << 32) ^ (pz & 0xffffffffL);
                if (seen.add(key)) {
                    result.add(new Peak(px, pz, sampleA.applyAsDouble(px, pz)));
                }
            }
        }
        return result;
    }

    private static int gradientSign(byte[] p, int x, int y, int z) {
        int failPositive = 0;
        int failNegative = 0;
        for (int i = 0; i < 8; i++) {
            int dx = i & 1;
            int dz = (i >> 1) & 1;
            int dy = (i >> 2) & 1;
            int hx = p[(x + dx) & 255] & 255;
            int hxy = p[(hx + y + dy) & 255] & 255;
            int g = GRADIENT[(p[(hxy + z + dz) & 255] & 255) & 15];
            if ((POSITIVE[i] & (1 << g)) == 0) {
                failPositive++;
            }
            if ((NEGATIVE[i] & (1 << g)) == 0) {
                failNegative++;
            }
            if (failPositive > MAX_GRADIENT_FAILURES && failNegative > MAX_GRADIENT_FAILURES) {
                return 0;
            }
        }
        return failPositive <= MAX_GRADIENT_FAILURES ? 1 : -1;
    }

    // sign == 0 maximizes absolute value, otherwise maximize sign * value
    private static Peak grid(DoubleBinaryOperator sample, int x, int z, int radius, int step, int sign) {
        Peak best = new Peak(x, z, sample.applyAsDouble(x, z));
        double score = sign == 0 ? Math.abs(best.value) : sign * best.value;
        for (int dx = -radius; dx <= radius; dx += step) {
            for (int dz = -radius; dz <= radius; dz += step) {
                double value = sample.applyAsDouble(x + dx, z + dz);
                double candidate = sign == 0 ? Math.abs(value) : sign * value;
                if (candidate > score) {
                    best = new Peak(x + dx, z + dz, value);
                    score = candidate;
                }
            }
        }
        return best;
    }

    private static PriorityQueue<Tile> tiles(int x, int z) {
        PriorityQueue<Tile> result = new PriorityQueue<>(Comparator.comparingLong(Tile::distanceSquared));
        for (int tx = Math.floorDiv(-WORLD_LIMIT - REFINE_MARGIN, BLOCK_TILE); tx <= Math.floorDiv(WORLD_LIMIT + REFINE_MARGIN, BLOCK_TILE); tx++) {
            LocatorThreadHelper.checkCancelled();
            for (int tz = Math.floorDiv(-WORLD_LIMIT - REFINE_MARGIN, BLOCK_TILE); tz <= Math.floorDiv(WORLD_LIMIT + REFINE_MARGIN, BLOCK_TILE); tz++) {
                long dx = axisDistance(x, tx * BLOCK_TILE - REFINE_MARGIN, (tx + 1) * BLOCK_TILE - 4 + REFINE_MARGIN);
                long dz = axisDistance(z, tz * BLOCK_TILE - REFINE_MARGIN, (tz + 1) * BLOCK_TILE - 4 + REFINE_MARGIN);
                long distanceSquared = dx * dx + dz * dz;
                result.add(new Tile(tx, tz, distanceSquared));
            }
        }
        return result;
    }

    private static long axisDistance(int position, int min, int max) {
        return Math.max(0L, Math.max((long) min - position, (long) position - max));
    }

    private static long distanceSquared(int ax, int az, int bx, int bz) {
        long dx = (long) ax - bx;
        long dz = (long) az - bz;
        return dx * dx + dz * dz;
    }

    /// Check if the sinkhole is surface-exposed.
    private static @Nullable Hit surfaceOpening(MemorySegment terrain, int x, int z, double weirdness, double depth, int originX, int originZ, long maxDistance) {
        try (Arena arena = Arena.ofConfined()) {
            Long2ObjectMap<MemorySegment> columns = new Long2ObjectOpenHashMap<>();
            Long2ObjectMap<MemorySegment> probes = new Long2ObjectOpenHashMap<>();
            for (int r = 0; r <= OPENING_RADIUS; r++) {
                LocatorThreadHelper.checkCancelled();
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != r) {
                            continue;
                        }
                        int px = x + dx;
                        int pz = z + dz;
                        if (Math.abs(px) > WORLD_LIMIT || Math.abs(pz) > WORLD_LIMIT || distanceSquared(originX, originZ, px, pz) >= maxDistance) {
                            continue;
                        }
                        // any solid terrain at -48 <= y <= -41 rules out a column open down to lava level
                        if (floor(terrain, arena, probes, px, pz, 2, 3) != NO_SOLID_FLOOR) {
                            continue;
                        }

                        int floorY = Math.max(WORLD_MIN_Y, floor(terrain, arena, columns, px, pz, 0, 48));
                        if (floorY <= MAX_FLOOR_Y) {
                            return new Hit(px, pz, weirdness, depth, floorY);
                        }
                    }
                }
            }
        }
        return null;
    }

    private static int floor(MemorySegment terrain, Arena arena, Long2ObjectMap<MemorySegment> columns, int x, int z, int minYCell, int maxYCell) {
        int qx = x >> 2;
        int qz = z >> 2;
        int rx = x & 3;
        int rz = z & 3;
        MemorySegment ds00 = column(terrain, arena, columns, qx, qz, minYCell, maxYCell);
        MemorySegment ds01 = rz == 0 ? ds00 : column(terrain, arena, columns, qx, qz + 1, minYCell, maxYCell);
        MemorySegment ds10 = rx == 0 ? ds00 : column(terrain, arena, columns, qx + 1, qz, minYCell, maxYCell);
        MemorySegment ds11 = rx == 0 ? ds01 : rz == 0 ? ds10 : column(terrain, arena, columns, qx + 1, qz + 1, minYCell, maxYCell);
        return Cubiomes.generateColumn(MemorySegment.NULL, ds00, ds01, ds10, ds11, minYCell, maxYCell, 8, rx / 4.0, rz / 4.0, Cubiomes.INTERP_1_18(), WORLD_MIN_Y, 1) - 1;
    }

    private static MemorySegment column(MemorySegment terrain, Arena arena, Long2ObjectMap<MemorySegment> columns, int qx, int qz, int minYCell, int maxYCell) {
        long key = ((long) qx << 32) ^ (qz & 0xffffffffL);
        return columns.computeIfAbsent(key, ignored -> {
            MemorySegment column = arena.allocate(Cubiomes.C_DOUBLE, maxYCell - minYCell + 1);
            Cubiomes.sampleNoiseColumn(terrain, qx, qz, minYCell, maxYCell, column);
            return column;
        });
    }

    public record Hit(int x, int z, double weirdness, double depth, int floorY) {
    }

    private record Peak(int x, int z, double value) {
    }

    private record Tile(int x, int z, long distanceSquared) {
    }
}
