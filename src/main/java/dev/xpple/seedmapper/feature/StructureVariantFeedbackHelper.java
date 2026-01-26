package dev.xpple.seedmapper.feature;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;

import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.StructureVariant;

import static dev.xpple.seedmapper.util.ChatBuilder.*;

public final class StructureVariantFeedbackHelper {

    private StructureVariantFeedbackHelper() {
    }

    private static final Int2ObjectMap<Function<MemorySegment, List<Component>>> VARIANT_FEEDBACK;

    static {
        Int2ObjectMap<Function<MemorySegment, List<Component>>> temp = new Int2ObjectOpenHashMap<>();
        temp.defaultReturnValue(_ -> Collections.emptyList());
        temp.put(Cubiomes.Village(), variant -> {
            List<Component> components = new ArrayList<>();
            short biome = StructureVariant.biome(variant);
            byte start = StructureVariant.start(variant);
            if (biome == Cubiomes.meadow() || biome == Cubiomes.plains()) {
                if (start == 0) {
                    components.add(Component.translatable("command.locate.feature.structure.start", "plains_fountain_01"));
                } else {
                    components.add(Component.translatable("command.locate.feature.structure.start", "plains_meeting_point_" + start));
                }
            } else if (biome == Cubiomes.desert()) {
                components.add(Component.translatable("command.locate.feature.structure.start", "desert_meeting_point_" + start));
            } else if (biome == Cubiomes.savanna()) {
                components.add(Component.translatable("command.locate.feature.structure.start", "savanna_meeting_point_" + start));
            } else if (biome == Cubiomes.taiga()) {
                components.add(Component.translatable("command.locate.feature.structure.start", "taiga_meeting_point_" + start));
            } else if (biome == Cubiomes.snowy_tundra()) {
                components.add(Component.translatable("command.locate.feature.structure.start", "snowy_meeting_point_" + start));
            }
            components.add(Component.translatable("command.locate.feature.structure.rotation_" + StructureVariant.rotation(variant)));
            if (StructureVariant.abandoned(variant) == 1) {
                components.add(Component.translatable("command.locate.feature.structure.village.abandoned"));
            } else {
                components.add(Component.translatable("command.locate.feature.structure.village.notAbandoned"));
            }
            return components;
        });
        temp.put(Cubiomes.Bastion(), variant -> {
            List<Component> components = new ArrayList<>();
            switch (StructureVariant.start(variant)) {
                case 0 -> components.add(Component.translatable("command.locate.feature.structure.start", "units/air_base"));
                case 1 -> components.add(Component.translatable("command.locate.feature.structure.start", "hoglin_stable/air_base"));
                case 2 -> components.add(Component.translatable("command.locate.feature.structure.start", "treasure/big_air_full"));
                case 3 -> components.add(Component.translatable("command.locate.feature.structure.start", "bridge/starting_pieces/entrance_base"));
            }
            components.add(Component.translatable("command.locate.feature.structure.rotation_" + StructureVariant.rotation(variant)));
            return components;
        });
        temp.put(Cubiomes.Ancient_City(), variant -> {
            List<Component> components = new ArrayList<>();
            int yLevel = StructureVariant.y(variant);
            components.add(Component.translatable("command.locate.feature.structure.yLevel", copy(accent(String.valueOf(yLevel)), String.valueOf(yLevel))));
            components.add(Component.translatable("command.locate.feature.structure.start", "city_center_" + StructureVariant.start(variant)));
            components.add(Component.translatable("command.locate.feature.structure.rotation_" + StructureVariant.rotation(variant)));
            return components;
        });
        temp.put(Cubiomes.Ruined_Portal(), variant -> {
            List<Component> components = new ArrayList<>();
            byte start = StructureVariant.start(variant);
            if (StructureVariant.giant(variant) == 1) {
                components.add(Component.translatable("command.locate.feature.structure.start", "ruined_portal/giant_portal_" + start));
                components.add(Component.translatable("command.locate.feature.structure.ruinedPortal.giant"));
            } else {
                components.add(Component.translatable("command.locate.feature.structure.start", "ruined_portal/portal_" + start));
                components.add(Component.translatable("command.locate.feature.structure.ruinedPortal.notGiant"));
            }
            components.add(1, Component.translatable("command.locate.feature.structure.rotation_" + StructureVariant.rotation(variant)));
            components.add(2, Component.translatable("command.locate.feature.structure.mirrored", StructureVariant.mirror(variant) == 1));
            if (StructureVariant.underground(variant) == 1) {
                components.add(Component.translatable("command.locate.feature.structure.ruinedPortal.underground"));
            } else {
                components.add(Component.translatable("command.locate.feature.structure.ruinedPortal.notUnderground"));
            }
            if (StructureVariant.airpocket(variant) == 1) {
                components.add(Component.translatable("command.locate.feature.structure.ruinedPortal.airPocket"));
            } else {
                components.add(Component.translatable("command.locate.feature.structure.ruinedPortal.noAirPocket"));
            }
            return components;
        });
        temp.put(Cubiomes.Ruined_Portal_N(), temp.get(Cubiomes.Ruined_Portal()));
        temp.put(Cubiomes.Igloo(), variant -> {
            List<Component> components = new ArrayList<>();
            components.add(Component.translatable("command.locate.feature.structure.igloo.size", StructureVariant.size(variant)));
            components.add(Component.translatable("command.locate.feature.structure.rotation_" + StructureVariant.rotation(variant)));
            components.add(Component.translatable("command.locate.feature.structure.mirrored", StructureVariant.mirror(variant) == 1));
            if (StructureVariant.basement(variant) == 1) {
                components.add(Component.translatable("command.locate.feature.structure.igloo.basement"));
            } else {
                components.add(Component.translatable("command.locate.feature.structure.igloo.noBasement"));
            }
            return components;
        });
        temp.put(Cubiomes.Shipwreck(), variant -> {
            List<Component> components = new ArrayList<>();
            String variantName;
            int biome = StructureVariant.biome(variant);
            if (Cubiomes.isOceanic(biome) == 0) {
                // isBeached
                variantName = switch (StructureVariant.start(variant)) {
                    case 0 -> "shipwreck/with_mast";
                    case 1 -> "shipwreck/sideways_full";
                    case 2 -> "shipwreck/sideways_fronthalf";
                    case 3 -> "shipwreck/sideways_backhalf";
                    case 4 -> "shipwreck/rightsideup_full";
                    case 5 -> "shipwreck/rightsideup_fronthalf";
                    case 6 -> "shipwreck/rightsideup_backhalf";
                    case 7 -> "shipwreck/with_mast_degraded";
                    case 8 -> "shipwreck/rightsideup_full_degraded";
                    case 9 -> "shipwreck/rightsideup_fronthalf_degraded";
                    case 10 -> "shipwreck/rightsideup_backhalf_degraded";
                    default -> "shipwreck/isBeached/error";
                };
            } else {
                variantName = switch (StructureVariant.start(variant)) {
                    case 0 -> "shipwreck/with_mast";
                    case 1 -> "shipwreck/upsidedown_full";
                    case 2 -> "shipwreck/upsidedown_fronthalf";
                    case 3 -> "shipwreck/upsidedown_backhalf";
                    case 4 -> "shipwreck/sideways_full";
                    case 5 -> "shipwreck/sideways_fronthalf";
                    case 6 -> "shipwreck/sideways_backhalf";
                    case 7 -> "shipwreck/rightsideup_full";
                    case 8 -> "shipwreck/rightsideup_fronthalf";
                    case 9 -> "shipwreck/rightsideup_backhalf";
                    case 10 -> "shipwreck/with_mast_degraded";
                    case 11 -> "shipwreck/upsidedown_full_degraded";
                    case 12 -> "shipwreck/upsidedown_fronthalf_degraded";
                    case 13 -> "shipwreck/upsidedown_backhalf_degraded";
                    case 14 -> "shipwreck/sideways_full_degraded";
                    case 15 -> "shipwreck/sideways_fronthalf_degraded";
                    case 16 -> "shipwreck/sideways_backhalf_degraded";
                    case 17 -> "shipwreck/rightsideup_full_degraded";
                    case 18 -> "shipwreck/rightsideup_fronthalf_degraded";
                    case 19 -> "shipwreck/rightsideup_backhalf_degraded";
                    default -> "shipwreck/isOcean/error";
                };
            }
            components.add(Component.translatable("command.locate.feature.structure.start", variantName));
            components.add(Component.translatable("command.locate.feature.structure.rotation_" + StructureVariant.rotation(variant)));
            return components;
        });
        temp.put(Cubiomes.Outpost(), variant -> {
            List<Component> components = new ArrayList<>();
            components.add(Component.translatable("command.locate.feature.structure.rotation_" + StructureVariant.rotation(variant)));
            return components;
        });
        temp.put(Cubiomes.Desert_Pyramid(), variant -> {
            List<Component> components = new ArrayList<>();
            components.add(Component.translatable("command.locate.feature.structure.rotation_" + StructureVariant.rotation(variant)));
            components.add(Component.translatable("command.locate.feature.structure.mirrored", StructureVariant.mirror(variant) == 1));
            return components;
        });
        temp.put(Cubiomes.Jungle_Temple(), temp.get(Cubiomes.Desert_Pyramid()));
        temp.put(Cubiomes.Swamp_Hut(), temp.get(Cubiomes.Desert_Pyramid()));
        temp.put(Cubiomes.Geode(), variant -> {
            List<Component> components = new ArrayList<>();
            int yLevel = StructureVariant.y(variant);
            components.add(Component.translatable("command.locate.feature.structure.yLevel", copy(accent(String.valueOf(yLevel)), String.valueOf(yLevel))));
            components.add(Component.translatable("command.locate.feature.structure.geode.size", StructureVariant.size(variant)));
            if (StructureVariant.cracked(variant) == 1) {
                components.add(Component.translatable("command.locate.feature.structure.geode.cracked"));
            } else {
                components.add(Component.translatable("command.locate.feature.structure.geode.notCracked"));
            }
            return components;
        });
        temp.put(Cubiomes.Trial_Chambers(), variant -> {
            List<Component> components = new ArrayList<>();
            int yLevel = StructureVariant.y(variant);
            components.add(Component.translatable("command.locate.feature.structure.yLevel", copy(accent(String.valueOf(yLevel)), String.valueOf(yLevel))));
            components.add(Component.translatable("command.locate.feature.structure.start", "corridor/end_" + StructureVariant.start(variant)));
            components.add(Component.translatable("command.locate.feature.structure.rotation_" + StructureVariant.rotation(variant)));
            return components;
        });
        VARIANT_FEEDBACK = Int2ObjectMaps.unmodifiable(temp);
    }

    public static List<Component> get(int structure, MemorySegment structureVariant) {
        return VARIANT_FEEDBACK
            .get(structure)
            .apply(structureVariant);
    }
}
