package dev.xpple.seedmapper.simulation;

import com.seedfinding.mccore.state.Dimension;
import dev.xpple.seedmapper.mixin.server.MinecraftServerAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.level.UnmodifiableLevelProperties;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class SimulatedWorld extends ServerWorld {

    public static SimulatedWorld currentInstance = null;

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

    @Override
    public boolean isSavingDisabled() {
        return true;
    }

    @Override
    public void tick(BooleanSupplier shouldKeepTicking) {
    }

    @Override
    protected void tickTime() {
    }

    @Override
    public void tickEntity(Entity entity) {
    }

    @Override
    public <T extends Entity> void tickEntity(Consumer<T> tickConsumer, T entity) {
    }

    @Override
    protected void tickBlockEntities() {
    }

    @Override
    public boolean shouldTickEntity(BlockPos pos) {
        return super.shouldTickEntity(pos);
    }

    @Override
    public void tickChunk(WorldChunk chunk, int randomTickSpeed) {
    }

    @Override
    public boolean shouldTickBlocksInChunk(long chunkPos) {
        return false;
    }

    @Override
    public void tickSpawners(boolean spawnMonsters, boolean spawnAnimals) {
    }

    @Override
    public <T extends ParticleEffect> int spawnParticles(T particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed) {
        return 0;
    }

    @Override
    public <T extends ParticleEffect> boolean spawnParticles(ServerPlayerEntity viewer, T particle, boolean force, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed) {
        return true;
    }
}
