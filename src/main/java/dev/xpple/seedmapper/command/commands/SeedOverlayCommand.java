package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.seedfinding.mcbiome.biome.Biome;
import com.seedfinding.mcbiome.biome.Biomes;
import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcterrain.TerrainGenerator;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.command.SharedHelpers;
import dev.xpple.seedmapper.util.chat.Chat;
import dev.xpple.seedmapper.util.config.Config;
import dev.xpple.seedmapper.util.maps.SimpleBlockMap;
import dev.xpple.seedmapper.util.render.RenderQueue;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.WorldChunk;

import java.util.HashMap;
import java.util.Map;

import static dev.xpple.seedmapper.SeedMapper.CLIENT;
import static dev.xpple.seedmapper.util.chat.ChatBuilder.*;

public class SeedOverlayCommand extends ClientCommand implements SharedHelpers.Exceptions {

    @Override
    protected void build() {
        argumentBuilder
                .executes(ctx -> seedOverlay(CustomClientCommandSource.of(ctx.getSource())));
    }

    @Override
    protected String rootLiteral() {
        return "seedoverlay";
    }

    @Override
    protected String alias() {
        return "overlay";
    }

    private static int seedOverlay(CustomClientCommandSource source) throws CommandSyntaxException {
        long seed = SharedHelpers.getSeed();
        String dimensionPath;
        if (source.getMeta("dimension") == null) {
            dimensionPath = source.getWorld().getRegistryKey().getValue().getPath();
        } else {
            dimensionPath = ((Identifier) source.getMeta("dimension")).getPath();
        }
        Dimension dimension = SharedHelpers.getDimension(dimensionPath);
        MCVersion mcVersion;
        if (source.getMeta("version") == null) {
            mcVersion = SharedHelpers.getMCVersion(CLIENT.getGame().getVersion().getName());
        } else {
            mcVersion = (MCVersion) source.getMeta("version");
        }

        BiomeSource biomeSource = BiomeSource.of(dimension, mcVersion, seed);
        TerrainGenerator generator = TerrainGenerator.of(dimension, biomeSource);
        final SimpleBlockMap map = new SimpleBlockMap(mcVersion, dimension, Biomes.PLAINS);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        final BlockPos center = new BlockPos(source.getPosition());
        final WorldChunk chunk = source.getWorld().getChunk(center.getX() >> 4, center.getZ() >> 4);
        final ChunkPos chunkPos = chunk.getPos();

        Map<Box, String> boxes = new HashMap<>();
        int blocks = 0;
        for (int x = chunkPos.getStartX(); x <= chunkPos.getEndX(); x++) {
            mutable.setX(x);
            for (int z = chunkPos.getStartZ(); z <= chunkPos.getEndZ(); z++) {
                mutable.setZ(z);
                final var column = generator.getColumnAt(x, z);
                final Biome biome = biomeSource.getBiome(x, 0, z);
                map.setBiome(biome);
                for (int y = 0; y < column.length; y++) {
                    mutable.setY(y);
                    final var terrainBlock = chunk.getBlockState(mutable).getBlock();
                    String terrainBlockName = Registry.BLOCK.getId(terrainBlock).getPath();
                    if (Config.getIgnoredBlocks().contains(terrainBlockName)) {
                        continue;
                    }
                    if (map.get(terrainBlock) == column[y].getId()) {
                        continue;
                    }
                    boxes.put(new Box(mutable), terrainBlockName);
                    Chat.print("", chain(
                            highlight(new TranslatableText("command.seedoverlay.feedback.0")),
                            copy(
                                    hover(
                                            accent("x: " + x + ", y: " + y + ", z: " + z),
                                            chain(
                                                    base(new TranslatableText("command.seedoverlay.feedback.1")),
                                                    highlight(terrainBlockName)
                                            )
                                    ),
                                    String.format("%d %d %d", x, y ,z)

                            ),
                            highlight(new TranslatableText("command.seedoverlay.feedback.2"))
                    ));
                    blocks++;
                }
            }
        }
        boxes.forEach((key, value) -> RenderQueue.addCuboid(RenderQueue.Layer.ON_TOP, key, key, Config.getColors().get(value),  30 * 20));
        if (blocks > 0) {
            Chat.print("", chain(
                    highlight(new TranslatableText("command.seedoverlay.feedback.3")),
                    accent(String.valueOf(blocks)),
                    highlight(new TranslatableText("command.seedoverlay.feedback.4"))
            ));
        } else {
            Chat.print("", highlight(new TranslatableText("command.seedoverlay.feedback.5")));
        }
        return blocks;
    }
}
