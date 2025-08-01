package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import dev.xpple.seedmapper.thread.LocatorThreadHelper;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class StopTaskCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("sm:stoptask")
            .executes(ctx -> stopTask(ctx.getSource())));
    }

    private static int stopTask(FabricClientCommandSource source) {
        LocatorThreadHelper.stop();
        return Command.SINGLE_SUCCESS;
    }
}
