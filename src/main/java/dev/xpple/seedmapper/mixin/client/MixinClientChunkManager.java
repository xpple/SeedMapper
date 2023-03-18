package dev.xpple.seedmapper.mixin.client;

import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcterrain.TerrainGenerator;
import dev.xpple.seedmapper.command.SharedHelpers;
import dev.xpple.seedmapper.command.commands.SeedOverlayCommand;
import dev.xpple.seedmapper.simulation.SimulatedServer;
import dev.xpple.seedmapper.simulation.SimulatedWorld;
import dev.xpple.seedmapper.util.config.Configs;
import dev.xpple.seedmapper.util.render.RenderQueue;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.util.math.Box;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Consumer;

import static dev.xpple.seedmapper.SeedMapper.CLIENT;

@Mixin(ClientChunkManager.class)
public class MixinClientChunkManager {

    @Inject(method = "loadChunkFromPacket", at = @At("RETURN"))
    private void onLoadChunk(int x, int z, PacketByteBuf buf, NbtCompound nbt, Consumer<ChunkData.BlockEntityVisitor> consumer, CallbackInfoReturnable<WorldChunk> cir) {
        if (!Configs.AutoOverlay) {
            return;
        }

        Chunk gameChunk = cir.getReturnValue();

        Map<Box, Block> boxes;

        if (Configs.UseWorldSimulation) {
            if (SimulatedServer.currentInstance == null) {
                return;
            }
            if (SimulatedWorld.currentInstance == null) {
                return;
            }
            boxes = SeedOverlayCommand.overlayUsingWorldSimulation(gameChunk, SimulatedWorld.currentInstance.getChunk(x, z));
        } else {
            long seed;
            try {
                seed = SharedHelpers.getSeed(null);
            } catch (Exception e) {
                return;
            }
            Dimension dimension = Dimension.fromString(CLIENT.world.getRegistryKey().getValue().getPath());
            if (dimension == null) {
                return;
            }
            MCVersion mcVersion = MCVersion.fromString(SharedConstants.getGameVersion().getName());
            if (mcVersion == null) {
                return;
            }

            BiomeSource biomeSource = BiomeSource.of(dimension, mcVersion, seed);
            TerrainGenerator terrainGenerator = TerrainGenerator.of(biomeSource);
            boxes = SeedOverlayCommand.overlayUsingLibraries(gameChunk, terrainGenerator);
        }

        boxes.forEach((key, value) -> RenderQueue.addCuboid(RenderQueue.Layer.ON_TOP, key, key, Configs.BlockColours.get(value),  30 * 20));
    }
}
