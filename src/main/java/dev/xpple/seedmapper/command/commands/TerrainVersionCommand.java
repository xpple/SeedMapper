package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.seedfinding.mcbiome.biome.Biome;
import com.seedfinding.mcbiome.biome.Biomes;
import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mccore.block.Block;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcterrain.TerrainGenerator;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.command.SharedHelpers;
import dev.xpple.seedmapper.util.config.Configs;
import dev.xpple.seedmapper.util.maps.SimpleBlockMap;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static dev.xpple.seedmapper.util.ChatBuilder.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class TerrainVersionCommand extends ClientCommand implements SharedHelpers.Exceptions {

    @Override
    protected LiteralCommandNode<FabricClientCommandSource> build(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        return dispatcher.register(literal(this.getRootLiteral())
            .executes(ctx -> execute(CustomClientCommandSource.of(ctx.getSource()))));
    }

    @Override
    protected String rootLiteral() {
        return "terrainversion";
    }

    private int execute(CustomClientCommandSource source) throws CommandSyntaxException {
        if (Configs.UseWorldSimulation) {
            throw UNSUPPORTED_BY_WORLD_SIMULATION_EXCEPTION.create();
        }

        SharedHelpers helpers = new SharedHelpers(source);

        final AtomicInteger blocks = new AtomicInteger(Integer.MAX_VALUE);
        final AtomicReference<String> version = new AtomicReference<>();

        Arrays.stream(MCVersion.values())
            .filter(mcVersion -> mcVersion.isNewerThan(MCVersion.v1_10_2))
            .forEach(mcVersion -> {
                BiomeSource biomeSource = BiomeSource.of(helpers.dimension(), mcVersion, helpers.seed());
                TerrainGenerator generator = TerrainGenerator.of(helpers.dimension(), biomeSource);
                SimpleBlockMap map = new SimpleBlockMap(mcVersion, helpers.dimension(), Biomes.PLAINS);

                BlockPos.Mutable mutable = new BlockPos.Mutable();
                final BlockPos center = BlockPos.ofFloored(source.getPosition());
                final WorldChunk chunk = source.getWorld().getChunk(center.getX() >> 4, center.getZ() >> 4);
                final ChunkPos chunkPos = chunk.getPos();

                int newBlocks = 0;
                for (int x = chunkPos.getStartX(); x <= chunkPos.getEndX(); x++) {
                    mutable.setX(x);
                    for (int z = chunkPos.getStartZ(); z <= chunkPos.getEndZ(); z++) {
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
                highlight(Text.translatable("command.terrainversion.feedback"))
            ));
        } else {
            source.sendFeedback(highlight(version.get()));
        }
        return Command.SINGLE_SUCCESS;
    }
}
