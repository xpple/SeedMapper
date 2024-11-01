package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.seedfinding.mcbiome.biome.Biome;
import com.seedfinding.mcbiome.biome.Biomes;
import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mcterrain.TerrainGenerator;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.util.config.Configs;
import dev.xpple.seedmapper.util.maps.SimpleBlockMap;
import dev.xpple.seedmapper.util.render.RenderQueue;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Map;

import static dev.xpple.seedmapper.util.ChatBuilder.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class SeedOverlayCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("sm:seedoverlay")
            .executes(ctx -> seedOverlay(CustomClientCommandSource.of(ctx.getSource()))));
    }

    private static int seedOverlay(CustomClientCommandSource source) throws CommandSyntaxException {
        BiomeSource biomeSource = BiomeSource.of(source.getDimension(), source.getVersion(), source.getSeed().getSecond());
        TerrainGenerator generator = TerrainGenerator.of(source.getDimension(), biomeSource);
        final SimpleBlockMap map = new SimpleBlockMap(source.getVersion(), source.getDimension(), Biomes.PLAINS);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        final BlockPos center = BlockPos.containing(source.getPosition());
        final LevelChunk chunk = source.getWorld().getChunk(center.getX() >> 4, center.getZ() >> 4);
        final ChunkPos chunkPos = chunk.getPos();

        Map<AABB, Block> boxes = new HashMap<>();
        int blocks = 0;
        for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
            mutable.setX(x);
            for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
                mutable.setZ(z);
                final var column = generator.getColumnAt(x, z);
                final Biome biome = biomeSource.getBiome(x, 0, z);
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
                    String terrainBlockName = BuiltInRegistries.BLOCK.getKey(terrainBlock).getPath();
                    source.sendFeedback(chain(
                        highlight(Component.translatable("command.seedoverlay.feedback.0")),
                        copy(
                            hover(
                                accent("x: " + x + ", y: " + y + ", z: " + z),
                                chain(
                                    base(Component.translatable("command.seedoverlay.feedback.1")),
                                    highlight(terrainBlockName)
                                )
                            ),
                            String.format("%d %d %d", x, y ,z)

                        ),
                        highlight(Component.translatable("command.seedoverlay.feedback.2"))
                    ));
                    blocks++;
                }
            }
        }
        boxes.forEach((key, value) -> RenderQueue.addCuboid(RenderQueue.Layer.ON_TOP, key, key, Configs.BlockColours.get(value),  30 * 20));
        if (blocks > 0) {
            source.sendFeedback(chain(
                highlight(Component.translatable("command.seedoverlay.feedback.3")),
                accent(String.valueOf(blocks)),
                highlight(Component.translatable("command.seedoverlay.feedback.4"))
            ));
        } else {
            source.sendFeedback(highlight(Component.translatable("command.seedoverlay.feedback.5")));
        }
        return blocks;
    }
}
