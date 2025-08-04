package dev.xpple.seedmapper.seedmap;

import com.github.cubiomes.Cubiomes;
import dev.xpple.seedmapper.SeedMapper;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum MapFeature {
    DESERT_PYRAMID("desert_pyramid", Cubiomes.Desert_Pyramid(), 19, 20),
    JUNGLE_PYRAMID("jungle_pyramid", Cubiomes.Jungle_Pyramid(), 19, 20),
    SWAMP_HUT("swamp_hut", Cubiomes.Swamp_Hut(), 20, 20),
    STRONGHOLD("stronghold", -1, 19, 20),
    IGLOO("igloo", Cubiomes.Igloo(), 20, 20),
    VILLAGE("village", Cubiomes.Village(), 19, 20),
    OCEAN_RUIN("ocean_ruin", Cubiomes.Ocean_Ruin(), 19, 19),
    SHIPWRECK("shipwreck", Cubiomes.Shipwreck(), 19, 19),
    MONUMENT("monument", Cubiomes.Monument(), 20, 20),
    MANSION("mansion", Cubiomes.Mansion(), 20, 20),
    OUTPOST("pillager_outpost", Cubiomes.Outpost(), 19, 20),
    RUINED_PORTAL("ruined_portal", Cubiomes.Ruined_Portal(), 20, 20),
    RUINED_PORTAL_N("ruined_portal_n", Cubiomes.Ruined_Portal_N(), 20, 20),
    ANCIENT_CITY("ancient_city", Cubiomes.Ancient_City(), 20, 20),
    TREASURE("buried_treasure", Cubiomes.Treasure(), 19, 19),
    MINESHAFT("mineshaft", Cubiomes.Mineshaft(), 20, 19),
    DESERT_WELL("desert_well", Cubiomes.Desert_Well(), 20, 20),
    GEODE("geode", Cubiomes.Geode(), 20, 20),
    IRON_ORE_VEIN("iron_ore_vein", -1, 20, 20),
    COPPER_ORE_VEIN("copper_ore_vein", -1, 20, 20),
    FORTRESS("fortress", Cubiomes.Fortress(), 20, 20),
    BASTION("bastion_remnant", Cubiomes.Bastion(), 20, 20),
    END_CITY("end_city", Cubiomes.End_City(), 20, 20),
    END_GATEWAY("end_gateway", Cubiomes.End_Gateway(), 20, 20),
    TRAIL_RUINS("trail_ruins", Cubiomes.Trail_Ruins(), 20, 20),
    TRIAL_CHAMBERS("trial_chambers", Cubiomes.Trial_Chambers(), 20, 20);

    public static final Map<String, MapFeature> BY_NAME = Arrays.stream(values())
        .collect(Collectors.toUnmodifiableMap(MapFeature::getName, f -> f));

    private final String name;
    private final int structureId;
    private final Texture texture;

    MapFeature(String name, int structureId, int textureWidth, int textureHeight) {
        this.name = name;
        this.structureId = structureId;
        this.texture = new Texture(name, textureWidth, textureHeight);
    }

    public String getName() {
        return this.name;
    }

    public int getStructureId() {
        return this.structureId;
    }

    public Texture getTexture() {
        return this.texture;
    }

    public record Texture(ResourceLocation resourceLocation, int width, int height) {
        private Texture(String structureName, int width, int height) {
            this(ResourceLocation.fromNamespaceAndPath(SeedMapper.MOD_ID, "textures/structure_icons/" + structureName + ".png"), width, height);
        }
    }
}
