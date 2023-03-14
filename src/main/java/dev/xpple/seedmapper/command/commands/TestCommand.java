package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.simulation.SimulatedServer;
import dev.xpple.seedmapper.simulation.SimulatedWorld;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureKeys;

import static com.mojang.brigadier.arguments.LongArgumentType.getLong;
import static com.mojang.brigadier.arguments.LongArgumentType.longArg;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class TestCommand extends ClientCommand {
    @Override
    protected void build(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        argumentBuilder
            .then(argument("seed", longArg())
                .executes(ctx -> test(ctx.getSource(), getLong(ctx, "seed"))));
    }

    @Override
    protected String rootLiteral() {
        return "test";
    }

    private int test(FabricClientCommandSource source, long seed) {
        try (SimulatedServer server = SimulatedServer.newServer(seed)) {
            SimulatedWorld world = new SimulatedWorld(server, World.OVERWORLD);
            BlockState blockState = world.getBlockState(new BlockPos(source.getPosition()));
            source.sendFeedback(blockState.getBlock().getName());
            BlockPos structurePos = world.locateStructure(StructureTags.VILLAGE, new BlockPos(source.getPosition()), 1000, false);
            source.sendFeedback(Text.of(structurePos.toShortString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Command.SINGLE_SUCCESS;
    }
}
