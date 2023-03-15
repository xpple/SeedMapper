package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.command.SharedHelpers;
import dev.xpple.seedmapper.simulation.SimulatedServer;
import dev.xpple.seedmapper.simulation.SimulatedWorld;
import dev.xpple.seedmapper.util.render.RenderQueue;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;

import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static dev.xpple.clientarguments.arguments.CBlockPredicateArgumentType.blockPredicate;
import static dev.xpple.clientarguments.arguments.CBlockPredicateArgumentType.getCBlockPredicate;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class SimulateCommand extends ClientCommand {

    @Override
    protected LiteralCommandNode<FabricClientCommandSource> build(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        return dispatcher.register(literal(this.getRootLiteral())
            .then(literal("highlight")
                .then(literal("block")
                    .then(argument("block", blockPredicate(registryAccess))
                        .executes(ctx -> highlightBlock(CustomClientCommandSource.of(ctx.getSource()), getCBlockPredicate(ctx, "block")))
                        .then(argument("range", integer(0))
                            .executes(ctx -> highlightBlock(CustomClientCommandSource.of(ctx.getSource()), getCBlockPredicate(ctx, "block"), getInteger(ctx, "range"))))))
                .then(literal("feature")
                    .then(literal("slimechunk"))))
            .then(literal("locate"))
            .then(literal("overlay"))
            .then(literal("seedoverlay")));
    }

    @Override
    protected String rootLiteral() {
        return "simulate";
    }

    @Override
    protected String alias() {
        return "sim";
    }

    private static int highlightBlock(CustomClientCommandSource source, Predicate<CachedBlockPosition> predicate) throws CommandSyntaxException {
        return highlightBlock(source, predicate, 160); // 10 chunks
    }

    private static int highlightBlock(CustomClientCommandSource source, Predicate<CachedBlockPosition> predicate, int range) throws CommandSyntaxException {
        SharedHelpers helpers = new SharedHelpers(source);
        try (SimulatedServer server = SimulatedServer.newServer(helpers.seed)) {
            SimulatedWorld world = new SimulatedWorld(server, helpers.dimension);
            BlockPos centre = BlockPos.ofFloored(source.getPosition());
            ChunkPos chunkPos = new ChunkPos(centre);
            Chunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            for (int x = chunkPos.getStartX(); x <= chunkPos.getEndX(); x++) {
                mutable.setX(x);
                for (int y = chunk.getBottomY(); y <= chunk.getTopY(); y++) {
                    mutable.setY(y);
                    for (int z = chunkPos.getStartZ(); z <= chunkPos.getEndZ(); z++) {
                        mutable.setZ(z);
                        if (predicate.test(new CachedBlockPosition(world, mutable, false))) {
                            Box box = new Box(x, y, z, x + 1, y + 1, z + 1);
                            RenderQueue.addCuboid(RenderQueue.Layer.ON_TOP, box, box, 0x00E100, -1);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Command.SINGLE_SUCCESS;
    }
}
