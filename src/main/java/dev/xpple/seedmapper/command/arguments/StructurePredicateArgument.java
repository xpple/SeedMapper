package dev.xpple.seedmapper.command.arguments;

import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.Piece;
import com.github.cubiomes.StructureVariant;
import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Pair;
import dev.xpple.seedmapper.command.CommandExceptions;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;

import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

public class StructurePredicateArgument implements ArgumentType<StructurePredicateArgument.StructureAndPredicate> {

    private static final Collection<String> EXAMPLES = Arrays.asList("village", "end_city[end_ship]", "ruined_portal{giant=true, underground=true}", "fortress[bridge_spawner, corridor_nether_wart]");

    //<editor-fold defaultstate="collapsed" desc="private static final Map<String, Integer> STRUCTURES;">
    private static final Map<String, Integer> STRUCTURES = ImmutableMap.<String, Integer>builder()
        .put("feature", Cubiomes.Feature())
        .put("desert_pyramid", Cubiomes.Desert_Pyramid())
        .put("jungle_pyramid", Cubiomes.Jungle_Pyramid())
        .put("swamp_hut", Cubiomes.Swamp_Hut())
        .put("igloo", Cubiomes.Igloo())
        .put("village", Cubiomes.Village())
        .put("ocean_ruin", Cubiomes.Ocean_Ruin())
        .put("shipwreck", Cubiomes.Shipwreck())
        .put("monument", Cubiomes.Monument())
        .put("mansion", Cubiomes.Mansion())
        .put("pillager_outpost", Cubiomes.Outpost())
        .put("ruined_portal", Cubiomes.Ruined_Portal())
        .put("ruined_portal_nether", Cubiomes.Ruined_Portal_N())
        .put("ancient_city", Cubiomes.Ancient_City())
        .put("buried_treasure", Cubiomes.Treasure())
        .put("mineshaft", Cubiomes.Mineshaft())
        .put("desert_well", Cubiomes.Desert_Well())
        .put("geode", Cubiomes.Geode())
        .put("fortress", Cubiomes.Fortress())
        .put("bastion_remnant", Cubiomes.Bastion())
        .put("end_city", Cubiomes.End_City())
        .put("end_gateway", Cubiomes.End_Gateway())
        .put("end_island", Cubiomes.End_Island())
        .put("trail_ruins", Cubiomes.Trail_Ruins())
        .put("trial_chambers", Cubiomes.Trial_Chambers())
        .build();
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="public static final Map<Integer, Map<String, Integer>> STRUCTURE_PIECES;">
    public static final Map<Integer, Map<String, Integer>> STRUCTURE_PIECES = ImmutableMap.<Integer, Map<String, Integer>>builder()
        .put(Cubiomes.End_City(), ImmutableMap.<String, Integer>builder()
            .put("base_floor", Cubiomes.BASE_FLOOR())
            .put("base_roof", Cubiomes.BASE_ROOF())
            .put("bridge_end", Cubiomes.BRIDGE_END())
            .put("bridge_gentle_stairs", Cubiomes.BRIDGE_GENTLE_STAIRS())
            .put("bridge_piece", Cubiomes.BRIDGE_PIECE())
            .put("bridge_steep_stairs", Cubiomes.BRIDGE_STEEP_STAIRS())
            .put("fat_tower_base", Cubiomes.FAT_TOWER_BASE())
            .put("fat_tower_middle", Cubiomes.FAT_TOWER_MIDDLE())
            .put("fat_tower_top", Cubiomes.FAT_TOWER_TOP())
            .put("second_floor_1", Cubiomes.SECOND_FLOOR_1())
            .put("second_floor_2", Cubiomes.SECOND_FLOOR_2())
            .put("second_roof", Cubiomes.SECOND_ROOF())
            .put("end_ship", Cubiomes.END_SHIP())
            .put("third_floor_1", Cubiomes.THIRD_FLOOR_1())
            .put("third_floor_2", Cubiomes.THIRD_FLOOR_2())
            .put("third_roof", Cubiomes.THIRD_ROOF())
            .put("tower_base", Cubiomes.TOWER_BASE())
            .put("tower_floor", Cubiomes.TOWER_FLOOR())
            .put("tower_piece", Cubiomes.TOWER_PIECE())
            .put("tower_top", Cubiomes.TOWER_TOP())
            .build())
        .put(Cubiomes.Fortress(), ImmutableMap.<String, Integer>builder()
            .put("fortress_start", Cubiomes.FORTRESS_START())
            .put("bridge_straight", Cubiomes.BRIDGE_STRAIGHT())
            .put("bridge_crossing", Cubiomes.BRIDGE_CROSSING())
            .put("bridge_fortified_crossing", Cubiomes.BRIDGE_FORTIFIED_CROSSING())
            .put("bridge_stairs", Cubiomes.BRIDGE_STAIRS())
            .put("bridge_spawner", Cubiomes.BRIDGE_SPAWNER())
            .put("bridge_corridor_entrance", Cubiomes.BRIDGE_CORRIDOR_ENTRANCE())
            .put("corridor_straight", Cubiomes.CORRIDOR_STRAIGHT())
            .put("corridor_crossing", Cubiomes.CORRIDOR_CROSSING())
            .put("corridor_turn_right", Cubiomes.CORRIDOR_TURN_RIGHT())
            .put("corridor_turn_left", Cubiomes.CORRIDOR_TURN_LEFT())
            .put("corridor_stairs", Cubiomes.CORRIDOR_STAIRS())
            .put("corridor_t_crossing", Cubiomes.CORRIDOR_T_CROSSING())
            .put("corridor_nether_wart", Cubiomes.CORRIDOR_NETHER_WART())
            .put("fortress_end", Cubiomes.FORTRESS_END())
            .build())
        .build();
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="private static final Map<String, Pair<Map<String, Integer>, Function<MemorySegment, Integer>>> GENERAL_VARIANTS;">
    private static final Map<String, Pair<Map<String, Integer>, Function<MemorySegment, Integer>>> GENERAL_VARIANTS = ImmutableMap.<String, Pair<Map<String, Integer>, Function<MemorySegment, Integer>>>builder()
        .put("biome", Pair.of(BiomeArgument.BIOMES, m -> (int) StructureVariant.biome(m)))
        .put("rotation", Pair.of(ImmutableMap.<String, Integer>builder()
            .put("north", 0)
            .put("east", 1)
            .put("south", 2)
            .put("west", 3)
            .build(), m -> (int) StructureVariant.rotation(m)))
        .put("mirrored", Pair.of(Map.of("true", 1, "false", 0), m -> (int) StructureVariant.mirror(m)))
        .build();
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="private static final Map<String, Pair<Map<String, Integer>, Function<MemorySegment, Integer>>> RUINED_PORTAL_SPECIFIC_VARIANTS;">
    private static final Map<String, Pair<Map<String, Integer>, Function<MemorySegment, Integer>>> RUINED_PORTAL_VARIANTS = ImmutableMap.<String, Pair<Map<String, Integer>, Function<MemorySegment, Integer>>>builder()
        .put("start", Pair.of(ImmutableMap.<String, Integer>builder()
            .put("ruined_portal.giant_portal_1", 1)
            .put("ruined_portal.giant_portal_2", 2)
            .put("ruined_portal.giant_portal_3", 3)
            .put("ruined_portal.portal_1", 1)
            .put("ruined_portal.portal_2", 2)
            .put("ruined_portal.portal_3", 3)
            .put("ruined_portal.portal_4", 4)
            .put("ruined_portal.portal_5", 5)
            .put("ruined_portal.portal_6", 6)
            .put("ruined_portal.portal_7", 7)
            .put("ruined_portal.portal_8", 8)
            .put("ruined_portal.portal_9", 9)
            .put("ruined_portal.portal_10", 10)
            .build(), m -> (int) StructureVariant.start(m)))
        .put("giant", Pair.of(Map.of("true", 1, "false", 0), m -> (int) StructureVariant.giant(m)))
        .put("underground", Pair.of(Map.of("true", 1, "false", 0), m -> (int) StructureVariant.underground(m)))
        .put("air_pocket", Pair.of(Map.of("true", 1, "false", 0), m -> (int) StructureVariant.airpocket(m)))
        .build();
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="public static final Map<Integer, Map<String, Pair<Map<String, Integer>, Function<MemorySegment, Integer>>>> STRUCTURE_VARIANTS;">
    public static final Map<Integer, Map<String, Pair<Map<String, Integer>, Function<MemorySegment, Integer>>>> STRUCTURE_VARIANTS = ImmutableMap.<Integer, Map<String, Pair<Map<String, Integer>, Function<MemorySegment, Integer>>>>builder()
        .put(Cubiomes.Village(), ImmutableMap.<String, Pair<Map<String, Integer>, Function<MemorySegment, Integer>>>builder()
            .put("start", Pair.of(ImmutableMap.<String, Integer>builder()
                .put("plains_fountain_01", 0)
                .put("plains_meeting_point_1", 1)
                .put("plains_meeting_point_2", 2)
                .put("plains_meeting_point_3", 3)
                .put("desert_meeting_point_1", 1)
                .put("desert_meeting_point_2", 2)
                .put("desert_meeting_point_3", 3)
                .put("savanna_meeting_point_1", 1)
                .put("savanna_meeting_point_2", 2)
                .put("savanna_meeting_point_3", 3)
                .put("savanna_meeting_point_4", 4)
                .put("taiga_meeting_point_1", 1)
                .put("taiga_meeting_point_2", 2)
                .put("snowy_meeting_point_1", 1)
                .put("snowy_meeting_point_2", 2)
                .put("snowy_meeting_point_3", 3)
                .build(), m -> (int) StructureVariant.start(m)))
            .put("abandoned", Pair.of(Map.of("true", 1, "false", 0), m -> (int) StructureVariant.abandoned(m)))
            .build())
        .put(Cubiomes.Bastion(), ImmutableMap.<String, Pair<Map<String, Integer>, Function<MemorySegment, Integer>>>builder()
            .put("start", Pair.of(ImmutableMap.<String, Integer>builder()
                .put("units.air_base", 0)
                .put("hoglin_stable.air_base", 1)
                .put("treasure.big_air_full", 2)
                .put("bridge.starting_pieces.entrance_base", 3)
                .build(), m -> (int) StructureVariant.start(m)))
            .build())
        .put(Cubiomes.Ancient_City(), ImmutableMap.<String, Pair<Map<String, Integer>, Function<MemorySegment, Integer>>>builder()
            .put("start", Pair.of(ImmutableMap.<String, Integer>builder()
                .put("city_center_1", 1)
                .put("city_center_2", 2)
                .put("city_center_3", 3)
                .build(), m -> (int) StructureVariant.start(m)))
            .build())
        .put(Cubiomes.Ruined_Portal(), RUINED_PORTAL_VARIANTS)
        .put(Cubiomes.Ruined_Portal_N(), RUINED_PORTAL_VARIANTS)
        .put(Cubiomes.Igloo(), ImmutableMap.<String, Pair<Map<String, Integer>, Function<MemorySegment, Integer>>>builder()
            .put("size", Pair.of(ImmutableMap.<String, Integer>builder()
                .put("4", 4)
                .put("5", 5)
                .put("6", 6)
                .put("7", 7)
                .put("8", 8)
                .put("9", 9)
                .put("10", 10)
                .put("11", 11)
                .build(), m -> (int) StructureVariant.size(m)))
            .put("basement", Pair.of(Map.of("true", 1, "false", 0), m -> (int) StructureVariant.basement(m)))
            .build())
        .put(Cubiomes.Outpost(), Collections.emptyMap())
        .put(Cubiomes.Desert_Pyramid(), Collections.emptyMap())
        .put(Cubiomes.Jungle_Temple(), Collections.emptyMap())
        .put(Cubiomes.Swamp_Hut(), Collections.emptyMap())
        .put(Cubiomes.Geode(), ImmutableMap.<String, Pair<Map<String, Integer>, Function<MemorySegment, Integer>>>builder()
            .put("size", Pair.of(ImmutableMap.<String, Integer>builder()
                .put("3", 3)
                .put("4", 4)
                .put("5", 5)
                .build(), m -> (int) StructureVariant.size(m)))
            .put("cracked", Pair.of(Map.of("true", 1, "false", 0), m -> (int) StructureVariant.cracked(m)))
            .build())
        .put(Cubiomes.Trial_Chambers(), ImmutableMap.<String, Pair<Map<String, Integer>, Function<MemorySegment, Integer>>>builder()
            .put("start", Pair.of(ImmutableMap.<String, Integer>builder()
                .put("corridor.end_1", 0)
                .put("corridor.end_2", 1)
                .build(), m -> (int) StructureVariant.size(m)))
            .build())
        .build();
    //</editor-fold>

    public static StructurePredicateArgument structurePredicate() {
        return new StructurePredicateArgument();
    }

    public static StructureAndPredicate getStructurePredicate(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, StructureAndPredicate.class);
    }

    @Override
    public StructureAndPredicate parse(StringReader reader) throws CommandSyntaxException {
        return new Parser(reader).parse();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        StringReader reader = new StringReader(builder.getInput());
        reader.setCursor(builder.getStart());

        Parser parser = new Parser(reader);

        try {
            parser.parse();
        } catch (CommandSyntaxException ignored) {
        }

        if (parser.suggestor != null) {
            parser.suggestor.accept(builder);
        }

        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    private static final class Parser {

        private final StringReader reader;
        private Consumer<SuggestionsBuilder> suggestor;

        private Parser(StringReader reader) {
            this.reader = reader;
        }

        private StructureAndPredicate parse() throws CommandSyntaxException {
            int structure = parseStructure();

            PiecesPredicate piecesPredicate = parsePieces(structure);

            VariantPredicate variantPredicate = parseVariant(structure);

            return new StructureAndPredicate(structure, piecesPredicate, variantPredicate);
        }

        private int parseStructure() throws CommandSyntaxException {
            int cursor = reader.getCursor();
            suggestor = suggestions -> {
                SuggestionsBuilder builder = suggestions.createOffset(cursor);
                SharedSuggestionProvider.suggest(STRUCTURES.keySet(), builder);
                suggestions.add(builder);
            };
            String structureString = reader.readUnquotedString();
            Integer structure = STRUCTURES.get(structureString);
            if (structure == null) {
                reader.setCursor(cursor);
                throw CommandExceptions.UNKNOWN_STRUCTURE_EXCEPTION.create(structureString);
            }
            return structure;
        }

        private PiecesPredicate parsePieces(int structure) throws CommandSyntaxException {
            Map<String, Integer> piecesMap = STRUCTURE_PIECES.get(structure);
            if (piecesMap == null) {
                return (_, _) -> true;
            }
            if (!reader.canRead()) {
                return (_, _) -> true;
            }

            // pieces that must be present
            Set<Integer> wantedPieces = new HashSet<>();

            reader.expect('[');
            while (true) {
                int cursor = reader.getCursor();
                suggestor = suggestions -> {
                    SuggestionsBuilder builder = suggestions.createOffset(cursor);
                    SharedSuggestionProvider.suggest(piecesMap.keySet(), builder);
                    suggestions.add(builder);
                };
                if (!reader.canRead() || reader.canRead() && reader.peek() == ']') {
                    break;
                }
                String pieceString = reader.readUnquotedString();
                Integer piece = piecesMap.get(pieceString);
                if (piece == null) {
                    reader.setCursor(cursor);
                    throw CommandExceptions.UNKNOWN_STRUCTURE_PIECE_EXCEPTION.create(pieceString);
                }
                wantedPieces.add(piece);
                if (reader.canRead() && reader.peek() == ',') {
                    reader.skip();
                    reader.skipWhitespace();
                    continue;
                }
                break;
            }
            reader.expect(']');

            return (n, p) -> {
                for (int wantedPiece : wantedPieces) {
                    boolean found = IntStream.range(0, n)
                        .mapToObj(i -> Piece.asSlice(p, i))
                        .anyMatch(piece -> Piece.type(piece) == wantedPiece);
                    if (!found) {
                        return false;
                    }
                }
                return true;
            };
        }

        private VariantPredicate parseVariant(int structure) throws CommandSyntaxException {
            VariantPredicate variantPredicate = _ -> true;
            Map<String, Pair<Map<String, Integer>, Function<MemorySegment, Integer>>> specificVariantKeys = STRUCTURE_VARIANTS.get(structure);
            if (specificVariantKeys == null) {
                return variantPredicate;
            }
            if (!reader.canRead()) {
                return variantPredicate;
            }

            reader.expect('{');
            while (true) {
                int cursor = reader.getCursor();
                suggestor = suggestions -> {
                    SuggestionsBuilder builder = suggestions.createOffset(cursor);
                    SharedSuggestionProvider.suggest(GENERAL_VARIANTS.keySet(), builder);
                    SharedSuggestionProvider.suggest(specificVariantKeys.keySet(), builder);
                    suggestions.add(builder);
                };
                if (!reader.canRead() || reader.canRead() && reader.peek() == '}') {
                    break;
                }
                String variantKeyString = reader.readUnquotedString();
                Pair<Map<String, Integer>, Function<MemorySegment, Integer>> variantsMap = GENERAL_VARIANTS.getOrDefault(variantKeyString, specificVariantKeys.get(variantKeyString));
                if (variantsMap == null) {
                    reader.setCursor(cursor);
                    throw CommandExceptions.UNKNOWN_VARIANT_KEY_EXCEPTION.create(variantKeyString);
                }
                reader.expect('=');
                int value = parseVariantValue(variantsMap);
                variantPredicate = variantPredicate.and(v -> variantsMap.getSecond().apply(v) == value);
                if (reader.canRead() && reader.peek() == ',') {
                    reader.skip();
                    reader.skipWhitespace();
                    continue;
                }
                break;
            }
            reader.expect('}');

            return variantPredicate;
        }

        private int parseVariantValue(Pair<Map<String, Integer>, Function<MemorySegment, Integer>> variantsMap) throws CommandSyntaxException {
            int cursor = reader.getCursor();
            suggestor = suggestions -> {
                SuggestionsBuilder builder = suggestions.createOffset(cursor);
                SharedSuggestionProvider.suggest(variantsMap.getFirst().keySet(), builder);
                suggestions.add(builder);
            };
            String variantValueString = reader.readUnquotedString();
            Integer value = variantsMap.getFirst().get(variantValueString);
            if (value == null) {
                reader.setCursor(cursor);
                throw CommandExceptions.UNKNOWN_VARIANT_VALUE_EXCEPTION.create(variantValueString);
            }
            return value;
        }
    }

    public record StructureAndPredicate(int structure, PiecesPredicate piecesPredicate, VariantPredicate variantPredicate) {
    }

    @FunctionalInterface
    public interface PiecesPredicate {
        boolean test(int numPieces, MemorySegment pieces);

        default PiecesPredicate and(PiecesPredicate other) {
            return (numPieces, pieces) -> this.test(numPieces, pieces) && other.test(numPieces, pieces);
        }
    }

    @FunctionalInterface
    public interface VariantPredicate {
        boolean test(MemorySegment variant);

        default VariantPredicate and(VariantPredicate other) {
            return variant -> this.test(variant) && other.test(variant);
        }
    }
}
