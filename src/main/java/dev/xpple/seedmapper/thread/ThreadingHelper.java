package dev.xpple.seedmapper.thread;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.seedmapper.command.CommandExceptions;
import dev.xpple.seedmapper.util.CheckedSupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static dev.xpple.seedmapper.util.ChatBuilder.*;

public final class ThreadingHelper {
    private ThreadingHelper() {
    }

    private static final ExecutorService locatingExecutor = Executors.newCachedThreadPool();
    private static Future<Integer> currentTask = null;

    public static final Component STOP_TASK_COMPONENT = run(hover(format(Component.translatable("commands.exceptions.alreadyBusyLocating.stopTask"), ChatFormatting.UNDERLINE), base(Component.translatable("commands.exceptions.alreadyBusyLocating.clickToStop"))), () -> {
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
            Minecraft.getInstance().player.displayClientMessage(Component.translatable("command.locate.taskStopped"), false);
        } else {
            Minecraft.getInstance().player.displayClientMessage(format(Component.translatable("command.locate.noTaskRunning"), ChatFormatting.RED), false);
        }
    });

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(locatingExecutor::shutdownNow));
    }

    public static int submit(CheckedSupplier<Integer, CommandSyntaxException> task) throws CommandSyntaxException {
        if (currentTask != null && !currentTask.isDone()) {
            throw CommandExceptions.ALREADY_BUSY_LOCATING_EXCEPTION.create();
        }
        currentTask = locatingExecutor.submit(() -> {
            try {
                return task.get();
            } catch (CommandSyntaxException e) {
                Player player = Minecraft.getInstance().player;
                if (player != null) {
                    Minecraft.getInstance().schedule(() -> player.displayClientMessage(format((MutableComponent) e.getRawMessage(), ChatFormatting.RED), false));
                }
                return 0;
            }
        });
        Minecraft.getInstance().player.displayClientMessage(Component.translatable("command.locate.taskStarted"), false);
        return Command.SINGLE_SUCCESS;
    }
}
