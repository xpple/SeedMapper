package dev.xpple.seedmapper.seedmap;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.level.ChunkPos;

public record ChunkStructureData(ChunkPos pos, Int2ObjectMap<StructureData> structures) {
}
