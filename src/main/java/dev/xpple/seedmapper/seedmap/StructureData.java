package dev.xpple.seedmapper.seedmap;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public record StructureData(ChunkPos pos, Int2ObjectMap<BlockPos> structures) {
}
