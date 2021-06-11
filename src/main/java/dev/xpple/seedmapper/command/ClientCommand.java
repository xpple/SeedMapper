package dev.xpple.seedmapper.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;

import static dev.xpple.seedmapper.SeedMapper.MOD_ID;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;

public abstract class ClientCommand {

    protected LiteralArgumentBuilder<FabricClientCommandSource> argumentBuilder;
    protected CommandDispatcher<FabricClientCommandSource> dispatcher;

    public static void instantiate(ClientCommand clientCommand, CommandDispatcher<FabricClientCommandSource> dispatcher) {
        if (clientCommand.rootLiteral() == null) {
            return;
        }

        String rootLiteral = clientCommand.rootLiteral();
        helper(clientCommand, dispatcher, rootLiteral);
        helper(clientCommand, dispatcher, MOD_ID + ":" + rootLiteral);

        if (clientCommand.alias() == null) {
            return;
        }

        String alias = clientCommand.alias();
        helper(clientCommand, dispatcher, alias);
        helper(clientCommand, dispatcher, MOD_ID + ":" + alias);
    }

    private static void helper(ClientCommand clientCommand, CommandDispatcher<FabricClientCommandSource> dispatcher, String rootLiteral) {
        boolean sameRootLiteral = false;
        for (CommandNode<FabricClientCommandSource> serverCommandSourceCommandNode : dispatcher.getRoot().getChildren()) {
            if (serverCommandSourceCommandNode.getName().equals(rootLiteral)) {
                sameRootLiteral = true;
                break;
            }
        }

        if (sameRootLiteral) {
            return;
        }

        clientCommand.argumentBuilder = literal(rootLiteral);

        clientCommand.register();

        dispatcher.register(clientCommand.argumentBuilder);

        clientCommand.dispatcher = dispatcher;
    }

    protected abstract void register();

    protected abstract String rootLiteral();

    protected String alias() {
        return null;
    }
}
