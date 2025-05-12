package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import dev.xpple.seedmapper.util.BuildInfo;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

import static dev.xpple.seedmapper.util.ChatBuilder.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class BuildInfoCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("sm:buildinfo")
            .executes(ctx -> buildInfo(ctx.getSource())));
    }

    private static int buildInfo(FabricClientCommandSource source) {
        source.sendFeedback(Component.translatable("command.buildInfo.success", accent(BuildInfo.VERSION), BuildInfo.BRANCH, BuildInfo.SHORT_COMMIT_HASH));
        return Command.SINGLE_SUCCESS;
    }
}
