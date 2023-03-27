package dev.xpple.seedmapper.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

import static dev.xpple.seedmapper.SeedMapper.MOD_PREFIX;

public abstract class ClientCommand {

    private final String rootLiteral = MOD_PREFIX + ':' + this.rootLiteral();
    private final String aliasLiteral = this.alias() == null ? null : MOD_PREFIX + ':' + this.alias();

    public static void instantiate(ClientCommand command, CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> root = command.build(dispatcher, registryAccess);
        if (command.alias() == null) {
            return;
        }
        dispatcher.register(buildAlias(command.getAliasLiteral(), root));
    }

    // See https://github.com/Mojang/brigadier/issues/46
    private static LiteralArgumentBuilder<FabricClientCommandSource> buildAlias(String alias, LiteralCommandNode<FabricClientCommandSource> destination) {
        LiteralArgumentBuilder<FabricClientCommandSource> builder = LiteralArgumentBuilder.<FabricClientCommandSource>literal(alias)
            .requires(destination.getRequirement())
            .forward(destination.getRedirect(), destination.getRedirectModifier(), destination.isFork())
            .executes(destination.getCommand());
        for (CommandNode<FabricClientCommandSource> child : destination.getChildren()) {
            builder.then(child);
        }
        return builder;
    }

    protected String getRootLiteral() {
        return this.rootLiteral;
    }

    private String getAliasLiteral() {
        return this.aliasLiteral;
    }

    protected abstract LiteralCommandNode<FabricClientCommandSource> build(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess);

    protected abstract String rootLiteral();

    protected String alias() {
        return null;
    }
}
