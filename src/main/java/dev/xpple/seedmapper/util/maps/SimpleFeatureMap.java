package dev.xpple.seedmapper.util.maps;

import kaptainwutax.featureutils.Feature;
import kaptainwutax.featureutils.structure.*;
import kaptainwutax.mcutils.version.MCVersion;

import java.util.HashMap;
import java.util.Map;

public class SimpleFeatureMap {

    public static final Map<Class<? extends Feature<?, ?>>, FeatureFactory<?>> REGISTRY = new HashMap<>();

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

    public static <T extends Feature<?, ?>> void register(Class<T> clazz, FeatureFactory<T> factory) {
        REGISTRY.put(clazz, factory);
    }

    public static Map<Class<? extends Feature<?, ?>>, Feature<?, ?>> getForVersion(MCVersion version) {
        Map<Class<? extends Feature<?, ?>>, Feature<?, ?>> result = new HashMap<>();
        for (Map.Entry<Class<? extends Feature<?, ?>>, FeatureFactory<?>> entry : REGISTRY.entrySet()) {
            try {
                Feature<?, ?> feature = entry.getValue().create(version);
                if (feature.getConfig() != null) {
                    result.put(entry.getKey(), feature);
                }
            } catch (NullPointerException ignored) {
            }
        }
        return result;
    }

    @FunctionalInterface
    interface FeatureFactory<T extends Feature<?, ?>> {
        T create(MCVersion version);
    }
}
