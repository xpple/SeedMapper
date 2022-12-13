package dev.xpple.seedmapper.mixin.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.seedfinding.mcbiome.biome.Biome;
import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcterrain.TerrainGenerator;
import dev.xpple.seedmapper.util.config.Config;
import dev.xpple.seedmapper.util.maps.SimpleBlockMap;
import dev.xpple.seedmapper.util.render.RenderQueue;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static dev.xpple.seedmapper.SeedMapper.CLIENT;

@Mixin(ClientChunkManager.class)
public class MixinClientChunkManager {

    @Inject(method = "loadChunkFromPacket", at = @At("RETURN"))
    private void onLoadChunk(int x, int z, PacketByteBuf buf, NbtCompound nbt, Consumer<ChunkData.BlockEntityVisitor> consumer, CallbackInfoReturnable<WorldChunk> cir) {
        if (Config.isEnabled("automate")) {
            String dimensionPath = CLIENT.world.getRegistryKey().getValue().getPath();
            Dimension dimension = Dimension.fromString(dimensionPath);
            if (dimension == null) {
                return;
            }
            MCVersion mcVersion = MCVersion.fromString(CLIENT.getGame().getVersion().getName());
            JsonElement element = Config.get("seed");
            if (element instanceof JsonNull) {
                return;
            }
            BiomeSource biomeSource = BiomeSource.of(dimension, mcVersion, element.getAsLong());
            TerrainGenerator generator = TerrainGenerator.of(dimension, biomeSource);
            SimpleBlockMap map = new SimpleBlockMap(dimension);

            BlockPos.Mutable mutable = new BlockPos.Mutable();

            final WorldChunk chunk = cir.getReturnValue();
            final ChunkPos chunkPos = chunk.getPos();

            Map<Box, String> boxes = new HashMap<>();
            for (int _x = chunkPos.getStartX(); _x <= chunkPos.getEndX(); _x++) {
                mutable.setX(_x);
                for (int _z = chunkPos.getStartZ(); _z <= chunkPos.getEndZ(); _z++) {
                    mutable.setZ(_z);
                    final var column = generator.getColumnAt(_x, _z);
                    final Biome biome = biomeSource.getBiome(_x, 0, _z);
                    map.setBiome(biome);
                    for (int y = 0; y < column.length; y++) {
                        mutable.setY(y);
                        final var terrainBlock = chunk.getBlockState(mutable).getBlock();
                        String terrainBlockName = Registries.BLOCK.getId(terrainBlock).getPath();
                        if (Config.getIgnoredBlocks().contains(terrainBlockName)) {
                            continue;
                        }
                        if (map.get(terrainBlock) == column[y].getId()) {
                            continue;
                        }
                        boxes.put(new Box(mutable), terrainBlockName);
                    }
                }
            }
            boxes.forEach((key, value) -> RenderQueue.addCuboid(RenderQueue.Layer.ON_TOP, key, key, Config.getColors().get(value),  30 * 20));
        }
    }
}
