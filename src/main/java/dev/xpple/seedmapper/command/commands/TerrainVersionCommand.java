package dev.xpple.seedmapper.command.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.util.blocks.SimpleBlockMap;
import dev.xpple.seedmapper.util.chat.Chat;
import dev.xpple.seedmapper.util.config.Config;
import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.biomeutils.source.BiomeSource;
import kaptainwutax.mcutils.state.Dimension;
import kaptainwutax.mcutils.version.MCVersion;
import kaptainwutax.terrainutils.ChunkGenerator;
import kaptainwutax.terrainutils.utils.Block;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static dev.xpple.seedmapper.SeedMapper.CLIENT;
import static dev.xpple.seedmapper.util.chat.ChatBuilder.*;

public class TerrainVersionCommand extends ClientCommand {

    private static final DynamicCommandExceptionType DIMENSION_NOT_SUPPORTED_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.dimensionNotSupported", arg));
    private static final DynamicCommandExceptionType NULL_POINTER_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.nullPointerException", arg));

    @Override
    protected void register() {
        argumentBuilder
                .executes(this::execute);
    }

    @Override
    protected String rootLiteral() {
        return "terrainversion";
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String dimensionPath = CLIENT.world.getRegistryKey().getValue().getPath();
        Dimension dimension = Dimension.fromString(dimensionPath);
        if (dimension == null) {
            throw DIMENSION_NOT_SUPPORTED_EXCEPTION.create(dimensionPath);
        }

        long seed;
        JsonElement element = Config.get("seed");
        if (element instanceof JsonNull) {
            throw NULL_POINTER_EXCEPTION.create("seed");
        } else {
            seed = element.getAsLong();
        }

        final AtomicInteger blocks = new AtomicInteger(65536);
        final AtomicReference<String> version = new AtomicReference<>("This chunk was modified too drastically.");

        Arrays.stream(MCVersion.values())
                .filter(mcVersion -> mcVersion.isNewerThan(MCVersion.v1_12_2))
                .forEach(mcVersion -> {
                    BiomeSource biomeSource = BiomeSource.of(dimension, mcVersion, seed);
                    ChunkGenerator generator = ChunkGenerator.of(dimension, biomeSource);
                    SimpleBlockMap map = new SimpleBlockMap(dimension);

                    BlockPos.Mutable mutable = new BlockPos.Mutable();
                    final BlockPos playerBlockPos = CLIENT.player.getBlockPos();
                    final WorldChunk chunk = CLIENT.player.world.getChunk(playerBlockPos.getX() >> 4, playerBlockPos.getZ() >> 4);
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
                                int seedBlockInt = column[y].getValue();
                                int terrainBlockInt = map.get(chunk.getBlockState(mutable).getBlock());
                                if (seedBlockInt == terrainBlockInt) {
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
                    highlight("Version "),
                    accent(version.get()),
                    highlight(" is the best match.")
            ));
        } else {
            Chat.print("", highlight(version.get()));
        }
        return Command.SINGLE_SUCCESS;
    }
}
