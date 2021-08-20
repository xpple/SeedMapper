package dev.xpple.seedmapper.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;

import java.util.Collection;

import static dev.xpple.seedmapper.SeedMapper.CLIENT;
import static dev.xpple.seedmapper.SeedMapper.MOD_ID;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;

public abstract class ClientCommand {

    protected LiteralArgumentBuilder<FabricClientCommandSource> argumentBuilder;

    public static void instantiate(ClientCommand clientCommand, CommandDispatcher<FabricClientCommandSource> dispatcher) {
        register(clientCommand, dispatcher, false);
        final Command<FabricClientCommandSource> command = clientCommand.argumentBuilder.getCommand();
        final Collection<CommandNode<FabricClientCommandSource>> arguments = clientCommand.argumentBuilder.getArguments();
        register(new ClientCommand() {
            @Override
            protected void build() {
                this.argumentBuilder.executes(command);
                arguments.forEach(arg -> this.argumentBuilder.then(arg));
            }

            @Override
            protected String rootLiteral() {
                return MOD_ID + ":" + clientCommand.rootLiteral();
            }
        }, dispatcher, true);
        if (clientCommand.alias() == null) {
            return;
        }
        register(new ClientCommand() {
            @Override
            protected void build() {
                this.argumentBuilder.executes(command);
                arguments.forEach(arg -> this.argumentBuilder.then(arg));
            }

            @Override
            protected String rootLiteral() {
                return clientCommand.alias();
            }
        }, dispatcher, false);
        register(new ClientCommand() {
            @Override
            protected void build() {
                this.argumentBuilder.executes(command);
                arguments.forEach(arg -> this.argumentBuilder.then(arg));
            }

            @Override
            protected String rootLiteral() {
                return MOD_ID + ":" + clientCommand.alias();
            }
        }, dispatcher, true);
    }

    private static void register(ClientCommand clientCommand, CommandDispatcher<FabricClientCommandSource> dispatcher, boolean isNamespaced) {
        final String rootLiteral = clientCommand.rootLiteral();
        clientCommand.argumentBuilder = literal(rootLiteral);
        clientCommand.build();
        boolean available = CLIENT.getNetworkHandler().getCommandDispatcher().getRoot().getChildren().stream().noneMatch(node -> node.getName().equals(rootLiteral));
        if (available || isNamespaced) {
            dispatcher.register(clientCommand.argumentBuilder);
        }
    }

    protected abstract void build();

    protected abstract String rootLiteral();

    protected String alias() {
        return null;
    }
}
