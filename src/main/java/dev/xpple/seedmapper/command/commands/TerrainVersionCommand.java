package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.command.SharedHelpers;
import dev.xpple.seedmapper.util.CacheUtil;
import dev.xpple.seedmapper.util.chat.Chat;
import dev.xpple.seedmapper.util.config.Config;
import dev.xpple.seedmapper.util.maps.SimpleBlockMap;
import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.biomeutils.biome.Biomes;
import kaptainwutax.biomeutils.source.BiomeSource;
import kaptainwutax.mcutils.block.Block;
import kaptainwutax.mcutils.state.Dimension;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.util.pos.CPos;
import kaptainwutax.mcutils.version.MCVersion;
import kaptainwutax.terrainutils.TerrainGenerator;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static dev.xpple.seedmapper.util.chat.ChatBuilder.*;

public class TerrainVersionCommand extends ClientCommand implements SharedHelpers.Exceptions {

    @Override
    protected void register() {
        argumentBuilder
                .executes(ctx -> execute(CustomClientCommandSource.of(ctx.getSource())));
    }

    @Override
    protected String rootLiteral() {
        return "terrainversion";
    }

    private int execute(CustomClientCommandSource source) throws CommandSyntaxException {
        long seed = SharedHelpers.getSeed();
        String dimensionPath;
        if (source.getMeta("dimension") == null) {
            dimensionPath = source.getWorld().getRegistryKey().getValue().getPath();
        } else {
            dimensionPath = ((Identifier) source.getMeta("dimension")).getPath();
        }
        Dimension dimension = SharedHelpers.getDimension(dimensionPath);

        final AtomicInteger blocks = new AtomicInteger(Integer.MAX_VALUE);
        final AtomicReference<String> version = new AtomicReference<>();

        Arrays.stream(MCVersion.values())
                .filter(mcVersion -> mcVersion.isNewerThan(MCVersion.v1_10_2))
                .forEach(mcVersion -> {
                    BiomeSource biomeSource = BiomeSource.of(dimension, mcVersion, seed);
                    TerrainGenerator generator = TerrainGenerator.of(dimension, biomeSource);
                    SimpleBlockMap map = new SimpleBlockMap(mcVersion, dimension, Biomes.PLAINS);

                    BlockPos.Mutable mutable = new BlockPos.Mutable();
                    final BlockPos center = new BlockPos(source.getPosition());
                    final WorldChunk chunk = source.getWorld().getChunk(center.getX() >> 4, center.getZ() >> 4);
                    final ChunkPos chunkPos = chunk.getPos();

                    int newBlocks = 0;
                    Map<BPos, Block> blocksForChunk;
                    try {
                        blocksForChunk = CacheUtil.getBlocksForChunk(new CPos(chunkPos.x, chunkPos.z), generator);
                    } catch (ExecutionException e) {
                        // error
                        return;
                    }
                    for (int x = chunkPos.getStartX(); x <= chunkPos.getEndX(); x++) {
                        mutable.setX(x);
                        for (int z = chunkPos.getStartZ(); z <= chunkPos.getEndZ(); z++) {
                            mutable.setZ(z);
                            final Biome biome = biomeSource.getBiome(x, 0, z);
                            map.setBiome(biome);
                            for (int y = 0; y < 255; y++) {
                                mutable.setY(y);
                                net.minecraft.block.Block terrainBlock = chunk.getBlockState(mutable).getBlock();
                                String terrainBlockName = Registry.BLOCK.getId(terrainBlock).getPath();
                                if (Config.getIgnoredBlocks().contains(terrainBlockName)) {
                                    continue;
                                }
                                kaptainwutax.mcutils.block.Block seedBlock = blocksForChunk.get(new BPos(x, y, z));
                                String seedBlockName = seedBlock.getName();
                                if (terrainBlockName.equals(seedBlockName)) {
                                    continue;
                                }
                                if (map.get(terrainBlockName) == map.get(seedBlockName)) {
                                    continue;
                                }
                                newBlocks++;
                            }
                        }
                    }
                    if (newBlocks < blocks.get()) {
                        blocks.set(newBlocks);
                        version.set(mcVersion.name);
                    }
                });
        if (version.get().startsWith("1")) {
            Chat.print("", chain(
                    accent(version.get()),
                    highlight(new TranslatableText("command.terrainversion.feedback"))
            ));
        } else {
            Chat.print("", highlight(version.get()));
        }
        return Command.SINGLE_SUCCESS;
    }
}
