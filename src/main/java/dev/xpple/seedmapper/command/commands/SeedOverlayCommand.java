package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.command.SharedHelpers;
import dev.xpple.seedmapper.util.CacheUtil;
import dev.xpple.seedmapper.util.chat.Chat;
import dev.xpple.seedmapper.util.config.Config;
import dev.xpple.seedmapper.util.maps.SimpleBlockMap;
import dev.xpple.seedmapper.util.render.RenderQueue;
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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.WorldChunk;

import java.util.*;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static dev.xpple.seedmapper.SeedMapper.CLIENT;
import static dev.xpple.seedmapper.util.chat.ChatBuilder.*;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.argument;
import static net.minecraft.command.CommandSource.suggestMatching;

public class SeedOverlayCommand extends ClientCommand implements SharedHelpers.Exceptions {

    @Override
    protected void register() {
        argumentBuilder
                .then(argument("version", word())
                        .suggests((ctx, builder) -> suggestMatching(Arrays.stream(MCVersion.values()).filter(mcVersion -> mcVersion.isNewerThan(MCVersion.v1_10_2)).map(mcVersion -> mcVersion.name), builder))
                        .executes(ctx -> seedOverlay(CustomClientCommandSource.of(ctx.getSource()), getString(ctx, "version"))))
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
        return seedOverlay(source, CLIENT.getGame().getVersion().getName());
    }

    private static int seedOverlay(CustomClientCommandSource source, String version) throws CommandSyntaxException {
        long seed = SharedHelpers.getSeed();
        String dimensionPath;
        if (source.getMeta("dimension") == null) {
            dimensionPath = source.getWorld().getRegistryKey().getValue().getPath();
        } else {
            dimensionPath = ((Identifier) source.getMeta("dimension")).getPath();
        }
        Dimension dimension = SharedHelpers.getDimension(dimensionPath);
        MCVersion mcVersion = SharedHelpers.getMCVersion(version);

        BiomeSource biomeSource = BiomeSource.of(dimension, mcVersion, seed);
        TerrainGenerator terrainGenerator = TerrainGenerator.of(dimension, biomeSource);
        final SimpleBlockMap map = new SimpleBlockMap(mcVersion, dimension, Biomes.PLAINS);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        final BlockPos center = new BlockPos(source.getPosition());
        final WorldChunk chunk = source.getWorld().getChunk(center.getX() >> 4, center.getZ() >> 4);
        final ChunkPos chunkPos = chunk.getPos();

        Set<Box> boxes = new HashSet<>();
        int blocks = 0;

        CPos cPos = new CPos(chunkPos.x, chunkPos.z);
        if (CacheUtil.isNotCached(mcVersion, cPos)) {
            CacheUtil.generateBlocksAt(cPos, terrainGenerator);
        }
        Map<BPos, Block> blocksForWorld = CacheUtil.getBlocksForWorld(mcVersion);
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
                    kaptainwutax.mcutils.block.Block seedBlock = blocksForWorld.get(new BPos(x, y, z));
                    String seedBlockName = seedBlock.getName();
                    if (terrainBlockName.equals(seedBlockName)) {
                        continue;
                    }
                    if (map.sameUngeneration(terrainBlockName, seedBlockName)) {
                        continue;
                    }
                    boxes.add(new Box(mutable));
                    Chat.print("", chain(
                            highlight(new TranslatableText("command.seedoverlay.feedback.0")),
                            copy(
                                    hover(
                                            accent("x: " + x + ", y: " + y + ", z: " + z),
                                            chain(
                                                    base(new TranslatableText("command.seedoverlay.feedback.1")),
                                                    highlight(terrainBlockName),
                                                    base(" ("),
                                                    base(new TranslatableText("command.seedoverlay.expected")),
                                                    highlight(seedBlockName),
                                                    base(")")
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
        for (Box box : boxes) {
            RenderQueue.addCuboid(RenderQueue.Layer.ON_TOP, box, box, 0XFFFB8919, 30 * 20);
        }
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
