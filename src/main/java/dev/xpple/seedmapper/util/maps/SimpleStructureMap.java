package dev.xpple.seedmapper.util.maps;

import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.structure.*;

import java.util.HashMap;
import java.util.Map;

public class SimpleStructureMap {

    public static final Map<Class<? extends Structure<?, ?>>, StructureFactory<?>> REGISTRY = new HashMap<>();

    static {
        register(BastionRemnant.class, BastionRemnant::new);
        register(BuriedTreasure.class, BuriedTreasure::new);
        register(DesertPyramid.class, DesertPyramid::new);
        register(EndCity.class, mcVersion -> new EndCity(mcVersion) {
            @Override
            public String getName() {
                return "endcity";
            }
        });
        register(Fortress.class, Fortress::new);
        register(Igloo.class, Igloo::new);
        register(JunglePyramid.class, JunglePyramid::new);
        register(Mansion.class, Mansion::new);
        register(Mineshaft.class, Mineshaft::new);
        register(Monument.class, Monument::new);
        register(NetherFossil.class, NetherFossil::new);
        register(OceanRuin.class, OceanRuin::new);
        register(PillagerOutpost.class, PillagerOutpost::new);
        register(Shipwreck.class, Shipwreck::new);
        register(SwampHut.class, SwampHut::new);
        register(Village.class, Village::new);
        register(Stronghold.class, Stronghold::new);
    }

    public static <T extends Structure<?, ?>> void register(Class<T> clazz, StructureFactory<T> factory) {
        REGISTRY.put(clazz, factory);
    }

    public static Map<Class<? extends Structure<?, ?>>, Structure<?, ?>> getForVersion(MCVersion version) {
        Map<Class<? extends Structure<?, ?>>, Structure<?, ?>> result = new HashMap<>();
        for (Map.Entry<Class<? extends Structure<?, ?>>, StructureFactory<?>> entry : REGISTRY.entrySet()) {
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
