package dev.xpple.seedmapper.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.ServerCommandSource;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static dev.xpple.seedmapper.SeedMapper.MOD_ID;

public abstract class ClientCommand {

    protected LiteralArgumentBuilder<ServerCommandSource> argumentBuilder;
    protected CommandDispatcher<ServerCommandSource> dispatcher;

    public static void instantiate(ClientCommand clientCommand, CommandDispatcher<ServerCommandSource> dispatcher) {
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

    private static void helper(ClientCommand clientCommand, CommandDispatcher<ServerCommandSource> dispatcher, String rootLiteral) {
        boolean sameRootLiteral = false;
        for (CommandNode<ServerCommandSource> serverCommandSourceCommandNode : dispatcher.getRoot().getChildren()) {
            if (serverCommandSourceCommandNode.getName().equals(rootLiteral)) {
                sameRootLiteral = true;
                break;
            }
        }

        if (sameRootLiteral) return;

        ClientCommandManager.addClientSideCommand(rootLiteral);

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
