package dev.xpple.seedmapper.feature;

import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.Generator;
import com.github.cubiomes.Pos;
import dev.xpple.seedmapper.command.arguments.StructurePredicateArgument;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.lang.foreign.MemorySegment;
import java.util.function.IntFunction;

public final class StructureChecks {

    private StructureChecks() {
    }

    // 400 == max fortress pieces as specified in Cubiomes Viewer
    public static final int MAX_END_CITY_AND_FORTRESS_PIECES = Math.max(Cubiomes.END_CITY_PIECES_MAX(), 400);

    private static final Int2ObjectMap<IntFunction<GenerationCheck>> GENERATION_CHECKS;

    private static final Int2ObjectMap<PiecesPredicateCheck> PIECES_PREDICATE_CHECKS;

    private static final Int2ObjectMap<VariantPredicateCheck> VARIANT_PREDICATE_CHECKS;

    static {
        Int2ObjectMap<IntFunction<GenerationCheck>> tempGenerationChecks = new Int2ObjectOpenHashMap<>();
        tempGenerationChecks.defaultReturnValue(baseGenerationCheck());
        tempGenerationChecks.put(Cubiomes.End_City(), structure -> baseGenerationCheck().apply(structure).and((generator, surfaceNoise, _, _, structurePos) -> {
            return Cubiomes.isViableEndCityTerrain(generator, surfaceNoise, Pos.x(structurePos), Pos.z(structurePos)) != 0;
        }));
        GENERATION_CHECKS = Int2ObjectMaps.unmodifiable(tempGenerationChecks);

        Int2ObjectMap<PiecesPredicateCheck> tempPiecesPredicateChecks = new Int2ObjectOpenHashMap<>();
        tempPiecesPredicateChecks.defaultReturnValue((_, _, _, _) -> true);
        tempPiecesPredicateChecks.put(Cubiomes.End_City(), (piecesPredicate, pieces, generator, structurePos) -> {
            int numPieces = Cubiomes.getEndCityPieces(pieces, Generator.seed(generator), Pos.x(structurePos) >> 4, Pos.z(structurePos) >> 4);
            return piecesPredicate.test(numPieces, pieces);
        });
        tempPiecesPredicateChecks.put(Cubiomes.Fortress(), (piecesPredicate, pieces, generator, structurePos) -> {
            int numPieces = Cubiomes.getFortressPieces(pieces, MAX_END_CITY_AND_FORTRESS_PIECES, Generator.mc(generator), Generator.seed(generator), Pos.x(structurePos) >> 4, Pos.z(structurePos) >> 4);
            return piecesPredicate.test(numPieces, pieces);
        });
        PIECES_PREDICATE_CHECKS = Int2ObjectMaps.unmodifiable(tempPiecesPredicateChecks);

        Int2ObjectMap<VariantPredicateCheck> tempVariantPredicateChecks = new Int2ObjectOpenHashMap<>();
        tempVariantPredicateChecks.defaultReturnValue((_, _, _, _) -> true);
        for (int structure : StructurePredicateArgument.VARIANT_SUPPORTED_STRUCTURES) {
            tempVariantPredicateChecks.put(structure, (variantPredicate, structureVariant, generator, structurePos) -> {
                int biome = Cubiomes.getBiomeAt(generator, 4, Pos.x(structurePos) >> 2, 320 >> 2, Pos.z(structurePos) >> 2);
                Cubiomes.getVariant(structureVariant, structure, Generator.mc(generator), Generator.seed(generator), Pos.x(structurePos), Pos.z(structurePos), biome);
                return variantPredicate.test(structureVariant);
            });
        }
        VARIANT_PREDICATE_CHECKS = Int2ObjectMaps.unmodifiable(tempVariantPredicateChecks);
    }

    public static GenerationCheck getGenerationCheck(int structure) {
        return GENERATION_CHECKS.get(structure).apply(structure);
    }

    public static PiecesPredicateCheck getPiecesPredicateCheck(int structure) {
        return PIECES_PREDICATE_CHECKS.get(structure);
    }

    public static VariantPredicateCheck getVariantPredicateCheck(int structure) {
        return VARIANT_PREDICATE_CHECKS.get(structure);
    }

    private static IntFunction<GenerationCheck> baseGenerationCheck() {
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
    public interface GenerationCheck {
        boolean check(MemorySegment generator, MemorySegment surfaceNoise, int regionX, int regionZ, MemorySegment structurePos);

        default GenerationCheck and(GenerationCheck other) {
            return (generator, surfaceNoise, regionX, regionZ, structurePos) -> this.check(generator, surfaceNoise, regionX, regionZ, structurePos) && other.check(generator, surfaceNoise, regionX, regionZ, structurePos);
        }
    }

    @FunctionalInterface
    public interface PiecesPredicateCheck {
        boolean check(StructurePredicateArgument.PiecesPredicate piecesPredicate, MemorySegment pieces, MemorySegment generator, MemorySegment structurePos);

        default PiecesPredicateCheck and(PiecesPredicateCheck other) {
            return (piecesPredicate, pieces, generator, structurePos) -> this.check(piecesPredicate, pieces, generator, structurePos) && other.check(piecesPredicate, pieces, generator, structurePos);
        }
    }

    @FunctionalInterface
    public interface VariantPredicateCheck {
        boolean check(StructurePredicateArgument.VariantPredicate variantPredicate, MemorySegment pieces, MemorySegment generator, MemorySegment structurePos);

        default VariantPredicateCheck and(VariantPredicateCheck other) {
            return (variantPredicate, structureVariant, generator, structurePos) -> this.check(variantPredicate, structureVariant, generator, structurePos) && other.check(variantPredicate, structureVariant, generator, structurePos);
        }
    }
}
