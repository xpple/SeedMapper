package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.seedfinding.mcbiome.biome.Biome;
import com.seedfinding.mcbiome.biome.Biomes;
import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mccore.block.Block;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcterrain.TerrainGenerator;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.util.maps.SimpleBlockMap;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static dev.xpple.seedmapper.util.ChatBuilder.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class TerrainVersionCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("sm:terrainversion")
            .executes(ctx -> execute(CustomClientCommandSource.of(ctx.getSource()))));
    }

    private static int execute(CustomClientCommandSource source) throws CommandSyntaxException {
        Dimension dimension = source.getDimension();
        long seed = source.getSeed().getSecond();
        final AtomicInteger blocks = new AtomicInteger(Integer.MAX_VALUE);
        final AtomicReference<String> version = new AtomicReference<>();

        Arrays.stream(MCVersion.values())
            .filter(mcVersion -> mcVersion.isNewerThan(MCVersion.v1_10_2))
            .forEach(mcVersion -> {
                BiomeSource biomeSource = BiomeSource.of(dimension, mcVersion, seed);
                TerrainGenerator generator = TerrainGenerator.of(dimension, biomeSource);
                SimpleBlockMap map = new SimpleBlockMap(mcVersion, dimension, Biomes.PLAINS);

                BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
                final BlockPos center = BlockPos.containing(source.getPosition());
                final LevelChunk chunk = source.getWorld().getChunk(center.getX() >> 4, center.getZ() >> 4);
                final ChunkPos chunkPos = chunk.getPos();

                int newBlocks = 0;
                for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
                    mutable.setX(x);
                    for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
                        mutable.setZ(z);
                        final Block[] column = generator.getColumnAt(x, z);
                        final Biome biome = biomeSource.getBiome(x, 0, z);
                        map.setBiome(biome);
                        for (int y = 0; y < column.length; y++) {
                            mutable.setY(y);
                            int seedBlockInt = column[y].getId();
                            int terrainBlockInt = map.get(chunk.getBlockState(mutable).getBlock());
                            if (seedBlockInt == terrainBlockInt) {
                                continue;
                            }
                            newBlocks++;
                        }
                    }
                }
                if (newBlocks <= blocks.get()) {
                    blocks.set(newBlocks);
                    version.set(mcVersion.name);
                }
            });
        if (version.get().startsWith("1")) {
            source.sendFeedback(chain(
                accent(version.get()),
                highlight(Component.translatable("command.terrainversion.feedback"))
            ));
        } else {
            source.sendFeedback(highlight(version.get()));
        }
        return Command.SINGLE_SUCCESS;
    }
}
