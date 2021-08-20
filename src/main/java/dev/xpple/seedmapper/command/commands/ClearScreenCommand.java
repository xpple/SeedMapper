package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.Command;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.util.chat.Chat;
import dev.xpple.seedmapper.util.render.RenderQueue;
import dev.xpple.seedmapper.util.render.Shape;
import net.minecraft.text.TranslatableText;

import java.util.Map;

public class ClearScreenCommand extends ClientCommand {

    @Override
    protected void build() {
        argumentBuilder
                .executes(ctx -> setAutomate(CustomClientCommandSource.of(ctx.getSource())));
    }

    @Override
    protected String rootLiteral() {
        return "clearscreen";
    }

    @Override
    protected String alias() {
        return "clear";
    }

    private int setAutomate(CustomClientCommandSource source) {
        Map<Object, Shape> shapeMap = RenderQueue.queue.get(RenderQueue.Layer.ON_TOP);
        final int size = shapeMap.size();
        shapeMap.clear();
        if (size == 0) {
            Chat.print("", new TranslatableText("command.clearscreen.empty"));
        } else {
            Chat.print("", new TranslatableText("command.clearscreen.success", size));
        }
        return Command.SINGLE_SUCCESS;
    }
}
