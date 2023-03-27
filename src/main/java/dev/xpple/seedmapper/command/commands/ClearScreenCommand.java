package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.util.chat.Chat;
import dev.xpple.seedmapper.util.render.RenderQueue;
import dev.xpple.seedmapper.util.render.Shape;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

import java.util.Map;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ClearScreenCommand extends ClientCommand {

    @Override
    protected LiteralCommandNode<FabricClientCommandSource> build(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        return dispatcher.register(literal(this.getRootLiteral())
            .executes(ctx -> clearScreen(CustomClientCommandSource.of(ctx.getSource()))));
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
        if (shapeMap == null) {
            Chat.print(Text.translatable("command.clearscreen.empty"));
            return Command.SINGLE_SUCCESS;
        }

        final int size = shapeMap.size();
        if (size == 0) {
            Chat.print(Text.translatable("command.clearscreen.empty"));
        } else {
            shapeMap.clear();
            Chat.print(Text.translatable("command.clearscreen.success", size));
        }
        return Command.SINGLE_SUCCESS;
    }
}
