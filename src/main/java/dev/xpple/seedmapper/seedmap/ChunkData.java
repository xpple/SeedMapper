package dev.xpple.seedmapper.seedmap;

import net.minecraft.world.level.ChunkPos;

import java.util.List;

public record ChunkData(ChunkPos pos, List<QuartData> quartDataList, StructureData structureData) {
}
