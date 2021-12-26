package dev.xpple.seedmapper.util.maps;

import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.structure.*;
import dev.xpple.seedmapper.util.features.NetherRuinedPortal;
import dev.xpple.seedmapper.util.features.OverworldRuinedPortal;

import java.util.HashMap;
import java.util.Map;

public class SimpleStructureMap {

    public static final Map<String, StructureFactory<?>> REGISTRY = new HashMap<>();

    static {
        register("bastion_remnant", BastionRemnant::new);
        register("buried_treasure", BuriedTreasure::new);
        register("desert_pyramid", DesertPyramid::new);
        register("end_city", EndCity::new);
        register("fortress", Fortress::new);
        register("igloo", Igloo::new);
        register("jungle_pyramid", JunglePyramid::new);
        register("mansion", Mansion::new);
        register("mineshaft", Mineshaft::new);
        register("monument", Monument::new);
        register("nether_fossil", NetherFossil::new);
        register("ocean_ruin", OceanRuin::new);
        register("pillager_outpost", PillagerOutpost::new);
        register("shipwreck", Shipwreck::new);
        register("swamp_hut", SwampHut::new);
        register("village", Village::new);
        register("stronghold", Stronghold::new);
        register("overworld_ruined_portal", OverworldRuinedPortal::new);
        register("nether_ruined_portal", NetherRuinedPortal::new);
    }

    public static <T extends Structure<?, ?>> void register(String name, StructureFactory<T> factory) {
        REGISTRY.put(name, factory);
    }

    public static Map<String, Structure<?, ?>> getForVersion(MCVersion version) {
        Map<String, Structure<?, ?>> result = new HashMap<>();
        for (Map.Entry<String, StructureFactory<?>> entry : REGISTRY.entrySet()) {
            try {
                Structure<?, ?> structure = entry.getValue().create(version);
                if (structure.getConfig() != null) {
                    result.put(entry.getKey(), structure);
                }
            } catch (NullPointerException ignored) {
            }
        }
        return result;
    }

    @FunctionalInterface
    interface StructureFactory<T extends Structure<?, ?>> {
        T create(MCVersion version);
    }
}
