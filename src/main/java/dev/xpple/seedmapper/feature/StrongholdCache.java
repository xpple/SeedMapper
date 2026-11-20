package dev.xpple.seedmapper.feature;

import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.Generator;
import com.github.cubiomes.Pos;
import com.github.cubiomes.StrongholdIter;
import dev.xpple.seedmapper.util.BiomeSeedIdentifier;
import dev.xpple.seedmapper.util.RenewableSoftReference;
import dev.xpple.seedmapper.util.TwoDTree;
import net.minecraft.core.BlockPos;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.concurrent.ConcurrentHashMap;

public final class StrongholdCache {
    private StrongholdCache() {
    }

    public static final RenewableSoftReference<ConcurrentHashMap<BiomeSeedIdentifier, TwoDTree>> cache = new RenewableSoftReference<>(ConcurrentHashMap::new);

    public static TwoDTree calculateStrongholds(long seed, int version, int generatorFlags) {
        TwoDTree tree = new TwoDTree();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment strongholdIter = StrongholdIter.allocate(arena);
            Cubiomes.initFirstStronghold(arena, strongholdIter, version, seed);
            MemorySegment generator = Generator.allocate(arena);
            Cubiomes.setupGenerator(generator, version, generatorFlags);
            Cubiomes.applySeed(generator, Cubiomes.DIM_OVERWORLD(), seed);

            final int count = version <= Cubiomes.MC_1_8() ? 3 : 128;
            for (int i = 0; i < count; i++) {
                if (Cubiomes.nextStronghold(strongholdIter, generator) == 0) {
                    break;
                }
                MemorySegment pos = StrongholdIter.pos(strongholdIter);
                tree.insert(new BlockPos(Pos.x(pos), 0, Pos.z(pos)));
            }
        }
        tree.balance();
        return tree;
    }

    public static TwoDTree compute(long seed, int version, int generatorFlags) {
        return compute(new BiomeSeedIdentifier(seed, version, generatorFlags));
    }

    public static TwoDTree compute(BiomeSeedIdentifier biomeSeedIdentifier) {
        return cache.get().computeIfAbsent(biomeSeedIdentifier, _ -> calculateStrongholds(biomeSeedIdentifier.seed(), biomeSeedIdentifier.version(), biomeSeedIdentifier.generatorFlags()));
    }
}
