package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.util.render.RenderQueue;
import dev.xpple.seedmapper.util.render.Shape;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

import java.util.Map;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class ClearScreenCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("sm:clearscreen")
            .executes(ctx -> clearScreen(CustomClientCommandSource.of(ctx.getSource()))));
    }

    private static int clearScreen(CustomClientCommandSource source) {
        Map<Object, Shape> shapeMap = RenderQueue.queue.get(RenderQueue.Layer.ON_TOP);
        if (shapeMap == null) {
            source.sendFeedback(Component.translatable("command.clearscreen.empty"));
            return Command.SINGLE_SUCCESS;
        }

        final int size = shapeMap.size();
        if (size == 0) {
            source.sendFeedback(Component.translatable("command.clearscreen.empty"));
        } else {
            shapeMap.clear();
            source.sendFeedback(Component.translatable("command.clearscreen.success", size));
        }
        return Command.SINGLE_SUCCESS;
    }
}
