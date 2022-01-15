package dev.xpple.seedmapper.util.features;

import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.decorator.Decorator;
import com.seedfinding.mcfeature.decorator.DesertWell;
import com.seedfinding.mcfeature.decorator.EndGateway;
import com.seedfinding.mcfeature.decorator.ore.OreDecorator;
import com.seedfinding.mcfeature.decorator.ore.nether.*;
import com.seedfinding.mcfeature.decorator.ore.overworld.*;
import com.seedfinding.mcfeature.structure.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Features {
    public static Map<String, FeatureFactory<? extends Structure<?, ?>>> STRUCTURE_REGISTRY = new HashMap<>();
    public static Map<String, FeatureFactory<? extends OreDecorator<?, ?>>> ORE_REGISTRY = new HashMap<>();
    public static Map<String, FeatureFactory<? extends Decorator<?, ?>>> DECORATOR_REGISTRY = new HashMap<>();

    static {
        STRUCTURE_REGISTRY.put("bastion_remnant", BastionRemnant::new);
        STRUCTURE_REGISTRY.put("buried_treasure", BuriedTreasure::new);
        STRUCTURE_REGISTRY.put("desert_pyramid", DesertPyramid::new);
        STRUCTURE_REGISTRY.put("end_city", EndCity::new);
        STRUCTURE_REGISTRY.put("fortress", Fortress::new);
        STRUCTURE_REGISTRY.put("igloo", Igloo::new);
        STRUCTURE_REGISTRY.put("jungle_pyramid", JunglePyramid::new);
        STRUCTURE_REGISTRY.put("mansion", Mansion::new);
        STRUCTURE_REGISTRY.put("mineshaft", Mineshaft::new);
        STRUCTURE_REGISTRY.put("monument", Monument::new);
        STRUCTURE_REGISTRY.put("nether_fossil", NetherFossil::new);
        STRUCTURE_REGISTRY.put("ocean_ruin", OceanRuin::new);
        STRUCTURE_REGISTRY.put("pillager_outpost", PillagerOutpost::new);
        STRUCTURE_REGISTRY.put("shipwreck", Shipwreck::new);
        STRUCTURE_REGISTRY.put("swamp_hut", SwampHut::new);
        STRUCTURE_REGISTRY.put("village", Village::new);
        STRUCTURE_REGISTRY.put("stronghold", Stronghold::new);
        STRUCTURE_REGISTRY.put("overworld_ruined_portal", OverworldRuinedPortal::new);
        STRUCTURE_REGISTRY.put("nether_ruined_portal", NetherRuinedPortal::new);

        ORE_REGISTRY.put("blackstone_ore", BlackstoneOre::new);
        ORE_REGISTRY.put("large_debris_ore", LargeDebrisOre::new);
        ORE_REGISTRY.put("magma_ore", MagmaOre::new);
        ORE_REGISTRY.put("nether_gold_ore", NetherGoldOre::new);
        ORE_REGISTRY.put("nether_gravel_ore", NetherGravelOre::new);
        ORE_REGISTRY.put("quartz_ore", QuartzOre::new);
        ORE_REGISTRY.put("small_debris_ore", SmallDebrisOre::new);
        ORE_REGISTRY.put("soulsand_ore", SoulSandOre::new);
        ORE_REGISTRY.put("andesite_ore", AndesiteOre::new);
        //ORE_REGISTRY.put("clay_disk", ClayDisk::new);
        ORE_REGISTRY.put("coal_ore", CoalOre::new);
        ORE_REGISTRY.put("copper_ore", CopperOre::new);
        ORE_REGISTRY.put("deepslate_ore", DeepslateOre::new);
        ORE_REGISTRY.put("diamond_ore", DiamondOre::new);
        ORE_REGISTRY.put("diorite_ore", DioriteOre::new);
        ORE_REGISTRY.put("dirt_ore", DirtOre::new);
        ORE_REGISTRY.put("emerald_ore", EmeraldOre::new);
        ORE_REGISTRY.put("extra_gold_ore", ExtraGoldOre::new);
        ORE_REGISTRY.put("gold_ore", GoldOre::new);
        ORE_REGISTRY.put("granite_ore", GraniteOre::new);
        //ORE_REGISTRY.put("gravel_disk", GravelDisk::new);
        ORE_REGISTRY.put("iron_ore", IronOre::new);
        ORE_REGISTRY.put("lapis_ore", LapisOre::new);
        ORE_REGISTRY.put("redstone_ore", RedstoneOre::new);
        //ORE_REGISTRY.put("sand_disk", SandDisk::new);
        ORE_REGISTRY.put("tuff_ore", TuffOre::new);

        DECORATOR_REGISTRY.put("desert_well", DesertWell::new);
        DECORATOR_REGISTRY.put("end_gateway", EndGateway::new);
    }

    public static Set<? extends Structure<?, ?>> getStructuresForVersion(MCVersion version) {
        Set<Structure<?, ?>> result = new HashSet<>();
        for (FeatureFactory<? extends Structure<?, ?>> factory : STRUCTURE_REGISTRY.values()) {
            try {
                Structure<?, ?> structure = factory.create(version);
                if (structure.getConfig() != null) {
                    result.add(structure);
                }
            } catch (NullPointerException ignored) {
            }
        }
        return result;
    }

    public static Set<? extends OreDecorator<?, ?>> getOresForVersion(MCVersion version) {
        Set<OreDecorator<?, ?>> result = new HashSet<>();
        for (FeatureFactory<? extends OreDecorator<?, ?>> factory : ORE_REGISTRY.values()) {
            try {
                OreDecorator<?, ?> oreDecorator = factory.create(version);
                if (oreDecorator.getConfig() != null) {
                    result.add(oreDecorator);
                }
            } catch (NullPointerException ignored) {
            }
        }
        return result;
    }

    public static Set<? extends Decorator<?, ?>> getDecoratorsForVersion(MCVersion version) {
        Set<Decorator<?, ?>> result = new HashSet<>();
        for (FeatureFactory<? extends Decorator<?, ?>> factory : DECORATOR_REGISTRY.values()) {
            try {
                Decorator<?, ?> decorator = factory.create(version);
                if (decorator.getConfig() != null) {
                    result.add(decorator);
                }
            } catch (NullPointerException ignored) {
            }
        }
        return result;
    }
}
