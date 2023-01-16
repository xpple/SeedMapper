package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.util.chat.Chat;
import dev.xpple.seedmapper.util.render.RenderQueue;
import dev.xpple.seedmapper.util.render.Shape;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import java.util.Map;

public class ClearScreenCommand extends ClientCommand {

    @Override
    protected void build(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        argumentBuilder
                .executes(ctx -> clearScreen(CustomClientCommandSource.of(ctx.getSource())));
    }

    @Override
    protected String rootLiteral() {
        return "clearscreen";
    }

    @Override
    protected String alias() {
        return "clear";
    }

    private int clearScreen(CustomClientCommandSource source) {
        Map<Object, Shape> shapeMap = RenderQueue.queue.get(RenderQueue.Layer.ON_TOP);
        final int size = shapeMap.size();
        shapeMap.clear();
        if (size == 0) {
            Chat.print(Text.translatable("command.clearscreen.empty"));
        } else {
            Chat.print(Text.translatable("command.clearscreen.success", size));
        }
        return Command.SINGLE_SUCCESS;
    }
}
