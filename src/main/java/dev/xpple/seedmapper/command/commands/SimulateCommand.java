package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.seedfinding.mccore.state.Dimension;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.command.SharedHelpers;
import dev.xpple.seedmapper.simulation.SimulatedServer;
import dev.xpple.seedmapper.simulation.SimulatedWorld;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

import static dev.xpple.seedmapper.SeedMapper.CLIENT;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class SimulateCommand extends ClientCommand implements SharedHelpers.Exceptions {

    public static SimulatedServer currentServer = null;
    public static SimulatedWorld currentWorld = null;

    public static boolean isServerStarting = false;

    private static final SimpleCommandExceptionType SERVER_ALREADY_RUNNING_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("command.simulate.serverAlreadyRunning"));
    private static final SimpleCommandExceptionType NO_SERVER_RUNNING_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("command.simulate.noServerRunning"));

    @Override
    protected LiteralCommandNode<FabricClientCommandSource> build(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        return dispatcher.register(literal(this.getRootLiteral())
            .then(literal("start")
                .executes(ctx -> start(CustomClientCommandSource.of(ctx.getSource()))))
            .then(literal("stop")
                .executes(ctx -> stop(CustomClientCommandSource.of(ctx.getSource()))))
            .then(literal("refresh")
                .executes(ctx -> refresh(CustomClientCommandSource.of(ctx.getSource())))));
    }

    @Override
    protected String rootLiteral() {
        return "simulate";
    }

    @Override
    protected String alias() {
        return "sim";
    }

    public static int start(CustomClientCommandSource source) throws CommandSyntaxException {
        if (currentServer != null) {
            throw SERVER_ALREADY_RUNNING_EXCEPTION.create();
        }

        long seed = SharedHelpers.getSeed(source);
        try {
            currentServer = SimulatedServer.newServer(seed);
            Dimension dimension = SharedHelpers.getDimension(CLIENT.world.getRegistryKey().getValue().getPath());
            currentWorld = new SimulatedWorld(currentServer, dimension);
        } catch (Exception e) {
            throw WORLD_SIMULATION_ERROR_EXCEPTION.create(e.getMessage());
        }

        if (source != null) {
            source.sendFeedback(Text.translatable("command.simulate.start", seed));
        }
        return Command.SINGLE_SUCCESS;
    }

    public static int stop(CustomClientCommandSource source) throws CommandSyntaxException {
        if (currentServer == null) {
            throw NO_SERVER_RUNNING_EXCEPTION.create();
        }

        currentServer = null;
        currentWorld = null;

        if (source != null) {
            source.sendFeedback(Text.translatable("command.simulate.stop"));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int refresh(CustomClientCommandSource source) throws CommandSyntaxException {
        if (currentServer == null) {
            throw NO_SERVER_RUNNING_EXCEPTION.create();
        }

        Dimension dimension = SharedHelpers.getDimension(CLIENT.world.getRegistryKey().getValue().getPath());
        currentWorld = new SimulatedWorld(currentServer, dimension);

        source.sendFeedback(Text.translatable("command.simulate.refresh"));
        return Command.SINGLE_SUCCESS;
    }
}
