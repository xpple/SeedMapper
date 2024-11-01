package dev.xpple.seedmapper.mixin.client;

import com.seedfinding.mcbiome.biome.Biome;
import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcterrain.TerrainGenerator;
import dev.xpple.seedmapper.util.config.Configs;
import dev.xpple.seedmapper.util.maps.SimpleBlockMap;
import dev.xpple.seedmapper.util.render.RenderQueue;
import net.minecraft.SharedConstants;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Mixin(ClientChunkCache.class)
public class ClientChunkCacheMixin {

    @Shadow @Final ClientLevel level;

    @Inject(method = "replaceWithPacketData", at = @At("RETURN"))
    private void onLoadChunk(int x, int z, FriendlyByteBuf buf, CompoundTag nbt, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer, CallbackInfoReturnable<LevelChunk> cir) {
        if (Configs.AutoOverlay) {
            String dimensionPath = this.level.dimension().location().getPath();
            Dimension dimension = Dimension.fromString(dimensionPath);
            if (dimension == null) {
                return;
            }
            MCVersion mcVersion = MCVersion.fromString(SharedConstants.getCurrentVersion().getName());
            if (mcVersion == null) {
                return;
            }
            Long seed = Configs.Seed;
            if (seed == null) {
                return;
            }
            BiomeSource biomeSource = BiomeSource.of(dimension, mcVersion, seed);
            TerrainGenerator generator = TerrainGenerator.of(dimension, biomeSource);
            SimpleBlockMap map = new SimpleBlockMap(dimension);

            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

            final LevelChunk chunk = cir.getReturnValue();
            final ChunkPos chunkPos = chunk.getPos();

            Map<AABB, Block> boxes = new HashMap<>();
            for (int _x = chunkPos.getMinBlockX(); _x <= chunkPos.getMaxBlockX(); _x++) {
                mutable.setX(_x);
                for (int _z = chunkPos.getMinBlockZ(); _z <= chunkPos.getMaxBlockZ(); _z++) {
                    mutable.setZ(_z);
                    final var column = generator.getColumnAt(_x, _z);
                    final Biome biome = biomeSource.getBiome(_x, 0, _z);
                    map.setBiome(biome);
                    for (int y = 0; y < column.length; y++) {
                        mutable.setY(y);
                        final var terrainBlock = chunk.getBlockState(mutable).getBlock();
                        if (Configs.IgnoredBlocks.contains(terrainBlock)) {
                            continue;
                        }
                        if (map.get(terrainBlock) == column[y].getId()) {
                            continue;
                        }
                        boxes.put(new AABB(mutable), terrainBlock);
                    }
                }
            }
            boxes.forEach((key, value) -> RenderQueue.addCuboid(RenderQueue.Layer.ON_TOP, key, key, Configs.BlockColours.get(value),  30 * 20));
        }
    }
}
