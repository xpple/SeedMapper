package dev.xpple.seedmapper.seedmap;

import com.github.cubiomes.Cubiomes;
import dev.xpple.seedmapper.SeedMapper;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

public record StructureData(ChunkPos pos, Int2ObjectMap<Structure> structures) {
    public record Structure(int structure, BlockPos blockPos, Texture texture) {

        public static final Int2ObjectMap<Texture> STRUCTURE_ICONS = new Int2ObjectOpenHashMap<>() {{
            put(Cubiomes.Desert_Pyramid(), new Texture("desert_pyramid", 19, 20));
            put(Cubiomes.Jungle_Pyramid(), new Texture("jungle_pyramid", 19, 20));
            put(Cubiomes.Swamp_Hut(), new Texture("swamp_hut", 20, 20));
            put(Cubiomes.Igloo(), new Texture("igloo", 20, 20));
            put(Cubiomes.Village(), new Texture("village", 19, 20));
            put(Cubiomes.Ocean_Ruin(), new Texture("ocean_ruin", 19, 19));
            put(Cubiomes.Shipwreck(), new Texture("shipwreck", 19, 19));
            put(Cubiomes.Monument(), new Texture("monument", 20, 20));
            put(Cubiomes.Mansion(), new Texture("mansion", 20, 20));
            put(Cubiomes.Outpost(), new Texture("pillager_outpost", 19, 20));
            put(Cubiomes.Ruined_Portal(), new Texture("ruined_portal", 20, 20));
            put(Cubiomes.Ruined_Portal_N(), new Texture("ruined_portal", 20, 20));
            put(Cubiomes.Ancient_City(), new Texture("ancient_city", 20, 20));
            put(Cubiomes.Treasure(), new Texture("buried_treasure", 19, 19));
            put(Cubiomes.Mineshaft(), new Texture("mineshaft", 20, 19));
            put(Cubiomes.Desert_Well(), new Texture("desert_well", 20, 20));
            put(Cubiomes.Geode(), new Texture("geode", 20, 20));
            put(Cubiomes.Fortress(), new Texture("fortress", 20, 20));
            put(Cubiomes.Bastion(), new Texture("bastion_remnant", 20, 20));
            put(Cubiomes.End_City(), new Texture("end_city", 20, 20));
            put(Cubiomes.End_Gateway(), new Texture("end_gateway", 20, 20));
            put(Cubiomes.Trail_Ruins(), new Texture("trail_ruins", 20, 20));
            put(Cubiomes.Trial_Chambers(), new Texture("trial_chambers", 20, 20));
        }};

        public Structure(int structure, BlockPos blockPos) {
            this(structure, blockPos, STRUCTURE_ICONS.get(structure));
        }

        public record Texture(ResourceLocation resourceLocation, int width, int height) {
            private Texture(String structureName, int width, int height) {
                this(ResourceLocation.fromNamespaceAndPath(SeedMapper.MOD_ID, "textures/structure_icons/" + structureName + ".png"), width, height);
            }
        }
    }
}
