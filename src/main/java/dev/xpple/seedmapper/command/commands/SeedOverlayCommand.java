package dev.xpple.seedmapper.command.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.util.blocks.SimpleBlockMap;
import dev.xpple.seedmapper.util.chat.Chat;
import dev.xpple.seedmapper.util.config.Config;
import dev.xpple.seedmapper.util.render.RenderQueue;
import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.biomeutils.source.BiomeSource;
import kaptainwutax.mcutils.state.Dimension;
import kaptainwutax.mcutils.version.MCVersion;
import kaptainwutax.terrainutils.ChunkGenerator;
import kaptainwutax.terrainutils.utils.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static dev.xpple.seedmapper.SeedMapper.CLIENT;
import static dev.xpple.seedmapper.util.chat.ChatBuilder.*;
import static net.minecraft.command.CommandSource.suggestMatching;
import static net.minecraft.server.command.CommandManager.argument;

public class SeedOverlayCommand extends ClientCommand {

    private static final DynamicCommandExceptionType NULL_POINTER_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.nullPointerException", arg));
    private static final DynamicCommandExceptionType DIMENSION_NOT_SUPPORTED_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.dimensionNotSupported", arg));
    private static final DynamicCommandExceptionType VERSION_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(arg -> new TranslatableText("commands.exceptions.versionNotFound", arg));

    @Override
    protected void register() {
        argumentBuilder
                .then(argument("version", word())
                        .suggests((ctx, builder) -> suggestMatching(Arrays.stream(MCVersion.values()).filter(mcVersion -> mcVersion.isNewerThan(MCVersion.v1_12_2)).map(mcVersion -> mcVersion.name), builder))
                        .executes(this::seedOverlayVersion))
                .executes(this::seedOverlay);
    }

    @Override
    protected String rootLiteral() {
        return "seedoverlay";
    }

    @Override
    protected String alias() {
        return "overlay";
    }

    private int seedOverlay(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        JsonElement element = Config.get("seed");
        if (element instanceof JsonNull) {
            throw NULL_POINTER_EXCEPTION.create("seed");
        }
        return execute(element.getAsLong(), CLIENT.getGame().getVersion().getName());
    }

    private int seedOverlayVersion(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        JsonElement element = Config.get("seed");
        if (element instanceof JsonNull) {
            throw NULL_POINTER_EXCEPTION.create("seed");
        }
        return execute(element.getAsLong(), getString(ctx, "version"));
    }

    private static int execute(long seed, String version) throws CommandSyntaxException {
        String dimensionPath = CLIENT.world.getRegistryKey().getValue().getPath();
        Dimension dimension = Dimension.fromString(dimensionPath);
        if (dimension == null) {
            throw DIMENSION_NOT_SUPPORTED_EXCEPTION.create(dimensionPath);
        }
        MCVersion mcVersion = MCVersion.fromString(version);
        if (mcVersion == null) {
            throw VERSION_NOT_FOUND_EXCEPTION.create(version);
        }
        BiomeSource biomeSource = BiomeSource.of(dimension, mcVersion, seed);
        ChunkGenerator generator = ChunkGenerator.of(dimension, biomeSource);
        final SimpleBlockMap map = new SimpleBlockMap(dimension);

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        final BlockPos playerBlockPos = CLIENT.player.getBlockPos();
        final WorldChunk chunk = CLIENT.player.world.getChunk(playerBlockPos.getX() >> 4, playerBlockPos.getZ() >> 4);
        final ChunkPos chunkPos = chunk.getPos();

        Set<Box> boxes = new HashSet<>();
        int blocks = 0;
        for (int x = chunkPos.getStartX(); x <= chunkPos.getEndX(); x++) {
            mutable.setX(x);
            for (int z = chunkPos.getStartZ(); z <= chunkPos.getEndZ(); z++) {
                mutable.setZ(z);
                final Block[] column = generator.getColumnAt(x, z);
                final Biome biome = biomeSource.getBiome(x, 0, z);
                map.setBiome(biome);
                for (int y = 0; y < column.length; y++) {
                    mutable.setY(y);
                    final BlockState blockState = chunk.getBlockState(mutable);
                    if (Config.getIgnoredBlocks().contains(blockState.getBlock())) {
                        continue;
                    }
                    int terrainBlockInt = map.get(blockState.getBlock());
                    int seedBlockInt = column[y].getValue();
                    if (seedBlockInt == terrainBlockInt) {
                        continue;
                    }
                    boxes.add(new Box(mutable));
                    Chat.print("", chain(
                            highlight("Block at "),
                            copy(
                                    hover(
                                            accent("x: " + x + ", y: " + y + ", z: " + z),
                                            chain(
                                                    base("Click to copy coordinates of "),
                                                    highlight(chunk.getBlockState(mutable).getBlock().getName())
                                            )
                                    ),
                                    String.format("%d %d %d", x, y ,z)

                            ),
                            highlight(" doesn't match!")
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
                    highlight("A total of "),
                    accent(String.valueOf(blocks)),
                    highlight(" blocks didn't match.")
            ));
        } else {
            Chat.print("", highlight("All blocks match!"));
        }
        return blocks;
    }
}
