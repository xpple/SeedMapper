package dev.xpple.seedmapper.seedmap;

import com.github.cubiomes.Cubiomes;
import dev.xpple.seedmapper.SeedMapper;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum MapFeature {
    DESERT_PYRAMID("desert_pyramid", Cubiomes.Desert_Pyramid(), Cubiomes.DIM_OVERWORLD(), Cubiomes.MC_1_3(), 19, 20),
    JUNGLE_PYRAMID("jungle_pyramid", Cubiomes.Jungle_Pyramid(), Cubiomes.DIM_OVERWORLD(), Cubiomes.MC_1_3(), 19, 20),
    SWAMP_HUT("swamp_hut", Cubiomes.Swamp_Hut(), Cubiomes.DIM_OVERWORLD(), Cubiomes.MC_1_4(), 20, 20),
    STRONGHOLD("stronghold", -1, Cubiomes.DIM_OVERWORLD(), Cubiomes.MC_B1_8(), 19, 20),
    IGLOO("igloo", Cubiomes.Igloo(), Cubiomes.DIM_OVERWORLD(), Cubiomes.MC_1_9(), 20, 20),
    VILLAGE("village", Cubiomes.Village(), Cubiomes.DIM_OVERWORLD(), Cubiomes.MC_B1_8(), 19, 20),
    OCEAN_RUIN("ocean_ruin", Cubiomes.Ocean_Ruin(), Cubiomes.DIM_OVERWORLD(), Cubiomes.MC_1_13(), 19, 19),
    SHIPWRECK("shipwreck", Cubiomes.Shipwreck(), Cubiomes.DIM_OVERWORLD(), Cubiomes.MC_1_13(), 19, 19),
    MONUMENT("monument", Cubiomes.Monument(), Cubiomes.DIM_OVERWORLD(), Cubiomes.MC_1_8(), 20, 20),
    MANSION("mansion", Cubiomes.Mansion(), Cubiomes.DIM_OVERWORLD(), Cubiomes.MC_1_11(), 20, 20),
    OUTPOST("pillager_outpost", Cubiomes.Outpost(), Cubiomes.DIM_OVERWORLD(), Cubiomes.MC_1_14(), 19, 20),
    RUINED_PORTAL("ruined_portal", Cubiomes.Ruined_Portal(), Cubiomes.DIM_OVERWORLD(), Cubiomes.MC_1_16_1(), 20, 20),
    RUINED_PORTAL_N("ruined_portal_n", Cubiomes.Ruined_Portal_N(), Cubiomes.DIM_NETHER(), Cubiomes.MC_1_16_1(), 20, 20),
    ANCIENT_CITY("ancient_city", Cubiomes.Ancient_City(), Cubiomes.DIM_OVERWORLD(), Cubiomes.MC_1_19_2(), 20, 20),
    TREASURE("buried_treasure", Cubiomes.Treasure(), Cubiomes.DIM_OVERWORLD(), Cubiomes.MC_1_13(), 19, 19),
    MINESHAFT("mineshaft", Cubiomes.Mineshaft(), Cubiomes.DIM_OVERWORLD(), Cubiomes.MC_B1_8(), 20, 19),
    DESERT_WELL("desert_well", Cubiomes.Desert_Well(), Cubiomes.DIM_OVERWORLD(), Cubiomes.MC_1_13(), 20, 20),
    GEODE("geode", Cubiomes.Geode(), Cubiomes.DIM_OVERWORLD(), Cubiomes.MC_1_17(), 20, 20),
    COPPER_ORE_VEIN("copper_ore_vein", -1, Cubiomes.DIM_OVERWORLD(), Cubiomes.MC_1_18(), 20, 20),
    IRON_ORE_VEIN("iron_ore_vein", -1, Cubiomes.DIM_OVERWORLD(), Cubiomes.MC_1_18(), 20, 20),
    FORTRESS("fortress", Cubiomes.Fortress(), Cubiomes.DIM_NETHER(), Cubiomes.MC_1_0(), 20, 20),
    BASTION("bastion_remnant", Cubiomes.Bastion(), Cubiomes.DIM_NETHER(), Cubiomes.MC_1_16_1(), 20, 20),
    END_CITY("end_city", Cubiomes.End_City(), Cubiomes.DIM_END(), Cubiomes.MC_1_9(), 20, 20),
    END_GATEWAY("end_gateway", Cubiomes.End_Gateway(), Cubiomes.DIM_END(), Cubiomes.MC_1_13(), 20, 20),
    TRAIL_RUINS("trail_ruins", Cubiomes.Trail_Ruins(), Cubiomes.DIM_OVERWORLD(), Cubiomes.MC_1_20(), 20, 20),
    TRIAL_CHAMBERS("trial_chambers", Cubiomes.Trial_Chambers(), Cubiomes.DIM_OVERWORLD(), Cubiomes.MC_1_21_1(), 20, 20),
    WORLD_SPAWN("world_spawn", -1, Cubiomes.DIM_OVERWORLD(), Cubiomes.MC_B1_7(), 20, 20),
    ;

    public static final Map<String, MapFeature> BY_NAME = Arrays.stream(values())
        .collect(Collectors.toUnmodifiableMap(MapFeature::getName, f -> f));

    private final String name;
    private final int structureId;
    private final int dimension;
    private final int availableSince;
    private final Texture texture;

    MapFeature(String name, int structureId, int dimension, int availableSince, int textureWidth, int textureHeight) {
        this.name = name;
        this.structureId = structureId;
        this.dimension = dimension;
        this.availableSince = availableSince;
        // stronghold is the only Cubiomes structure without a numeric id
        if (this.structureId != -1 || name.equals("stronghold")) {
            this.texture = Texture.structureTexture(name, textureWidth, textureHeight);
        } else {
            this.texture = Texture.featureTexture(name, textureWidth, textureHeight);
        }
    }

    public String getName() {
        return this.name;
    }

    public int getStructureId() {
        return this.structureId;
    }

    public int getDimension() {
        return this.dimension;
    }

    public int availableSince() {
        return this.availableSince;
    }

    public Texture getTexture() {
        return this.texture;
    }

    public record Texture(ResourceLocation resourceLocation, int width, int height) {
        private static Texture structureTexture(String structureName, int width, int height) {
            return new Texture(ResourceLocation.fromNamespaceAndPath(SeedMapper.MOD_ID, "textures/structure_icons/" + structureName + ".png"), width, height);
        }

        private static Texture featureTexture(String featureName, int width, int height) {
            return new Texture(ResourceLocation.fromNamespaceAndPath(SeedMapper.MOD_ID, "textures/feature_icons/" + featureName + ".png"), width, height);
        }
    }
}
