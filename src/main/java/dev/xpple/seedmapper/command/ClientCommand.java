package dev.xpple.seedmapper.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;

import static dev.xpple.seedmapper.SeedMapper.MOD_ID;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;

public abstract class ClientCommand {

    protected LiteralArgumentBuilder<FabricClientCommandSource> argumentBuilder = literal(this.getRootLiteral());

    public void instantiate() {
        this.build();
        ClientCommandManager.DISPATCHER.register(this.argumentBuilder);
        if (this.alias() == null) {
            return;
        }
        this.argumentBuilder = literal(this.alias());
        this.build();
        ClientCommandManager.DISPATCHER.register(this.argumentBuilder);
    }

    private String getRootLiteral() {
        return MOD_ID + ":" + this.rootLiteral();
    }

    protected abstract void build();

    protected abstract String rootLiteral();

    protected String alias() {
        return null;
    }
}
