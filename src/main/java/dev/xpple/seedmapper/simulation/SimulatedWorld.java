package dev.xpple.seedmapper.simulation;

import com.seedfinding.mccore.state.Dimension;
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

    public SimulatedWorld(SimulatedServer server, Dimension dimension) {
        super(server, Util.getMainWorkerExecutor(), ((MinecraftServerAccessor) server).getSession(), new UnmodifiableLevelProperties(server.getSaveProperties(), server.getSaveProperties().getMainWorldProperties()), getWorld(dimension), server.getCombinedDynamicRegistries().getCombinedRegistryManager().get(RegistryKeys.DIMENSION).get(getDimensionOptions(dimension)), FakeWorldGenerationProgressListener.INSTANCE, false, BiomeAccess.hashSeed(server.getSaveProperties().getGeneratorOptions().getSeed()), Collections.emptyList(), false);
    }

    private static RegistryKey<World> getWorld(Dimension dimension) {
        return switch (dimension) {
            case OVERWORLD -> World.OVERWORLD;
            case NETHER -> World.NETHER;
            case END -> World.END;
        };
    }

    private static RegistryKey<DimensionOptions> getDimensionOptions(Dimension dimension) {
        return switch (dimension) {
            case OVERWORLD -> DimensionOptions.OVERWORLD;
            case NETHER -> DimensionOptions.NETHER;
            case END -> DimensionOptions.END;
        };
    }

    @Override
    public void save(@Nullable ProgressListener progressListener, boolean flush, boolean savingDisabled) {
    }
}
