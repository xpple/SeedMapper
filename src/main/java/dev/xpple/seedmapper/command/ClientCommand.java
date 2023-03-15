package dev.xpple.seedmapper.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

import static dev.xpple.seedmapper.SeedMapper.MOD_PREFIX;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public abstract class ClientCommand {

    public static void instantiate(ClientCommand command, CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> root = command.build(dispatcher, registryAccess);
        if (command.alias() == null) {
            return;
        }
        dispatcher.register(literal(command.getAliasLiteral()).redirect(root));

        LiteralCommandNode<FabricClientCommandSource> rt = dispatcher.register(literal("aliascmd").executes(ctx -> {
            System.out.println("works");
            return 1;
        }));

        dispatcher.register(literal("testcmd").redirect(rt));
    }

    protected String getRootLiteral() {
        return MOD_PREFIX + ":" + this.rootLiteral();
    }

    private String getAliasLiteral() {
        return MOD_PREFIX + ":" + this.alias();
    }

    protected abstract LiteralCommandNode<FabricClientCommandSource> build(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess);

    protected abstract String rootLiteral();

    protected String alias() {
        return null;
    }
}
