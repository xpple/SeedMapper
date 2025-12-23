package dev.xpple.seedmapper.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.xpple.seedmapper.command.arguments.BlockArgument;
import dev.xpple.seedmapper.util.BlockColorConfig;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class BlockColorCommands {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("blockcolor")
            .then(literal("set")
                .then(argument("block", StringArgumentType.word())
                .then(argument("color", IntegerArgumentType.integer(0, 0xFFFFFF))
                    .executes(context -> {
                        String block = StringArgumentType.getString(context, "block");
                        int color = IntegerArgumentType.getInteger(context, "color");
                        
                        if (!BlockArgument.BLOCK_IDS.containsKey(block)) {
                            throw CommandExceptions.UNKNOWN_BLOCK_EXCEPTION.create(block);
                        }
                        
                        BlockColorConfig.setColor(block, color);
                        context.getSource().sendFeedback(Text.literal("Set color for " + block + " to #" + 
                            String.format("%06X", color)));
                        return 1;
                    })))
            )
            .then(literal("reset")
                .then(argument("block", StringArgumentType.word())
                    .executes(context -> {
                        String block = StringArgumentType.getString(context, "block");
                        
                        if (!BlockArgument.BLOCK_IDS.containsKey(block)) {
                            throw CommandExceptions.UNKNOWN_BLOCK_EXCEPTION.create(block);
                        }
                        
                        BlockColorConfig.resetColor(block);
                        context.getSource().sendFeedback(Text.literal("Reset color for " + block));
                        return 1;
                    }))
            )
            .then(literal("resetall")
                .executes(context -> {
                    BlockColorConfig.resetAllColors();
                    context.getSource().sendFeedback(Text.literal("Reset all block colors"));
                    return 1;
                })
            )
        );
    }
}
