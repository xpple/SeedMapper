package dev.xpple.seedmapper.util.maps;

import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.decorator.ore.OreDecorator;
import com.seedfinding.mcfeature.decorator.ore.nether.*;
import com.seedfinding.mcfeature.decorator.ore.overworld.*;

import java.util.HashMap;
import java.util.Map;

public class SimpleOreMap {

    private static final Map<Class<? extends OreDecorator<?, ?>>, OreDecoratorFactory<?>> REGISTRY = new HashMap<>();

    static {
        register(BlackstoneOre.class, BlackstoneOre::new);
        register(LargeDebrisOre.class, LargeDebrisOre::new);
        register(MagmaOre.class, MagmaOre::new);
        register(NetherGoldOre.class, NetherGoldOre::new);
        register(NetherGravelOre.class, NetherGravelOre::new);
        register(QuartzOre.class, QuartzOre::new);
        register(SmallDebrisOre.class, SmallDebrisOre::new);
        register(SoulSandOre.class, SoulSandOre::new);
        register(AndesiteOre.class, AndesiteOre::new);
        //register(ClayDisk.class, ClayDisk::new);
        register(CoalOre.class, CoalOre::new);
        register(CopperOre.class, CopperOre::new);
        register(DeepslateOre.class, DeepslateOre::new);
        register(DiamondOre.class, DiamondOre::new);
        register(DioriteOre.class, DioriteOre::new);
        register(DirtOre.class, DirtOre::new);
        register(EmeraldOre.class, EmeraldOre::new);
        register(ExtraGoldOre.class, ExtraGoldOre::new);
        register(GoldOre.class, GoldOre::new);
        register(GraniteOre.class, GraniteOre::new);
        //register(GravelDisk.class, GravelDisk::new);
        register(IronOre.class, IronOre::new);
        register(LapisOre.class, LapisOre::new);
        register(RedstoneOre.class, RedstoneOre::new);
        //register(SandDisk.class, SandDisk::new);
        register(TuffOre.class, TuffOre::new);
    }

    public static <T extends OreDecorator<?, ?>> void register(Class<T> clazz, OreDecoratorFactory<T> factory) {
        REGISTRY.put(clazz, factory);
    }

    public static Map<Class<? extends OreDecorator<?, ?>>, OreDecorator<?, ?>> getForVersion(MCVersion version) {
        Map<Class<? extends OreDecorator<?, ?>>, OreDecorator<?, ?>> result = new HashMap<>();
        for (Map.Entry<Class<? extends OreDecorator<?, ?>>, OreDecoratorFactory<?>> entry : REGISTRY.entrySet()) {
            try {
                OreDecorator<?, ?> oreDecorator = entry.getValue().create(version);
                if (oreDecorator.getConfig() != null) {
                    result.put(entry.getKey(), oreDecorator);
                }
            } catch (NullPointerException ignored) {
            }
        }
        return result;
    }

    @FunctionalInterface
    interface OreDecoratorFactory<T extends OreDecorator<?, ?>> {
        T create(MCVersion version);
    }
}
