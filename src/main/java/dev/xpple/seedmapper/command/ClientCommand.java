package dev.xpple.seedmapper.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static dev.xpple.seedmapper.SeedMapper.MOD_PREFIX;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public abstract class ClientCommand {

    protected LiteralArgumentBuilder<FabricClientCommandSource> argumentBuilder = literal(this.getRootLiteral());

    public static void instantiate(ClientCommand command, CommandDispatcher<FabricClientCommandSource> dispatcher) {
        command.build(dispatcher);
        dispatcher.register(command.argumentBuilder);
        if (command.alias() == null) {
            return;
        }
        command.argumentBuilder = literal(command.getAliasLiteral());
        command.build(dispatcher);
        dispatcher.register(command.argumentBuilder);
    }

    private String getRootLiteral() {
        return MOD_PREFIX + ":" + this.rootLiteral();
    }

    private String getAliasLiteral() {
        return MOD_PREFIX + ":" + this.alias();
    }

    protected abstract void build(CommandDispatcher<FabricClientCommandSource> dispatcher);

    protected abstract String rootLiteral();

    protected String alias() {
        return null;
    }
}
