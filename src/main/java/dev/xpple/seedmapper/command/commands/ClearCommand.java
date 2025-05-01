package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.render.RenderManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class ClearCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("sm:clear")
            .executes(ctx -> clear(CustomClientCommandSource.of(ctx.getSource()))));
    }

    private static int clear(CustomClientCommandSource source) {
        RenderManager.clear();
        source.sendFeedback(Component.translatable("command.clear.success"));
        return Command.SINGLE_SUCCESS;
    }
}
