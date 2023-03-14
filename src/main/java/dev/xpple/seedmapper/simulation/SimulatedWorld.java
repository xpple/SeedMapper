package dev.xpple.seedmapper.simulation;

import dev.xpple.seedmapper.mixin.server.MinecraftServerAccessor;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.level.UnmodifiableLevelProperties;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class SimulatedWorld extends ServerWorld {
    public SimulatedWorld(SimulatedServer server, RegistryKey<World> dimension) {
        super(server, Util.getMainWorkerExecutor(), ((MinecraftServerAccessor) server).getSession(), new UnmodifiableLevelProperties(server.getSaveProperties(), server.getSaveProperties().getMainWorldProperties()), dimension, server.getCombinedDynamicRegistries().getCombinedRegistryManager().get(RegistryKeys.DIMENSION).get(DimensionOptions.OVERWORLD), FakeWorldGenerationProgressListener.INSTANCE, false, BiomeAccess.hashSeed(server.getSaveProperties().getGeneratorOptions().getSeed()), Collections.emptyList(), false);
    }

    @Override
    public void save(@Nullable ProgressListener progressListener, boolean flush, boolean savingDisabled) {
    }
}
