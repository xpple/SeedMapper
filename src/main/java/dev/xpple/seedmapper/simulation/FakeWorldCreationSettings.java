package dev.xpple.seedmapper.simulation;

import net.minecraft.resource.DataConfiguration;
import net.minecraft.world.level.WorldGenSettings;

record FakeWorldCreationSettings(WorldGenSettings worldGenSettings, DataConfiguration dataConfiguration) {
}
