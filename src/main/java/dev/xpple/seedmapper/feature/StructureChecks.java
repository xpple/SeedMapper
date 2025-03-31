package dev.xpple.seedmapper.feature;

import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.Generator;
import com.github.cubiomes.Pos;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.lang.foreign.MemorySegment;
import java.util.function.IntFunction;

public final class StructureChecks {

    private StructureChecks() {
    }

    private static final Int2ObjectMap<IntFunction<StructureCheck>> STRUCTURE_CHECKS;

    static {
        Int2ObjectMap<IntFunction<StructureCheck>> temp = new Int2ObjectOpenHashMap<>();
        temp.defaultReturnValue(baseCheck());
        temp.put(Cubiomes.End_City(), structure -> baseCheck().apply(structure).and((generator, surfaceNoise, _, _, structurePos) -> {
            return Cubiomes.isViableEndCityTerrain(generator, surfaceNoise, Pos.x(structurePos), Pos.z(structurePos)) != 0;
        }));
        STRUCTURE_CHECKS = Int2ObjectMaps.unmodifiable(temp);
    }

    public static StructureCheck get(int structure) {
        return STRUCTURE_CHECKS.get(structure).apply(structure);
    }

    private static IntFunction<StructureCheck> baseCheck() {
        return structure -> (generator, _, regionX, regionZ, structurePos) -> {
            if (Cubiomes.getStructurePos(structure, Generator.mc(generator), Generator.seed(generator), regionX, regionZ, structurePos) == 0) {
                return false;
            }
            if (Cubiomes.isViableStructurePos(structure, generator, Pos.x(structurePos), Pos.z(structurePos), 0) == 0) {
                return false;
            }
            if (Cubiomes.isViableStructureTerrain(structure, generator, Pos.x(structurePos), Pos.z(structurePos)) == 0) {
                return false;
            }
            return true;
        };
    }

    @FunctionalInterface
    public interface StructureCheck {
        boolean check(MemorySegment generator, MemorySegment surfaceNoise, int regionX, int regionZ, MemorySegment structurePos);

        default StructureCheck and(StructureCheck other) {
            return (generator, surfaceNoise, regionX, regionZ, structurePos) -> this.check(generator, surfaceNoise, regionX, regionZ, structurePos) && other.check(generator, surfaceNoise, regionX, regionZ, structurePos);
        }
    }
}
