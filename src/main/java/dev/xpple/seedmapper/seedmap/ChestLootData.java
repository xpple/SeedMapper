package dev.xpple.seedmapper.seedmap;

import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;

public record ChestLootData(int structure, String pieceName, BlockPos chestPos, long lootSeed, String lootTable, SimpleContainer container) {
}
