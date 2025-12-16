package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.seedmap.SeedMapMinimapManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.core.BlockPos;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class MinimapCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("sm:minimap")
            .executes(ctx -> toggle(CustomClientCommandSource.of(ctx.getSource())))
            .then(literal("on").executes(ctx -> enable(CustomClientCommandSource.of(ctx.getSource()))))
            .then(literal("off").executes(ctx -> disable(CustomClientCommandSource.of(ctx.getSource())))));
    }

    private static int toggle(CustomClientCommandSource source) throws CommandSyntaxException {
        long seed = source.getSeed().getSecond();
        int dimension = source.getDimension();
        int version = source.getVersion();
        BlockPos playerPos = BlockPos.containing(source.getPosition());
        source.getClient().schedule(() -> SeedMapMinimapManager.toggle(seed, dimension, version, playerPos));
        return Command.SINGLE_SUCCESS;
    }

    private static int enable(CustomClientCommandSource source) throws CommandSyntaxException {
        long seed = source.getSeed().getSecond();
        int dimension = source.getDimension();
        int version = source.getVersion();
        BlockPos playerPos = BlockPos.containing(source.getPosition());
        source.getClient().schedule(() -> SeedMapMinimapManager.show(seed, dimension, version, playerPos));
        return Command.SINGLE_SUCCESS;
    }

    private static int disable(CustomClientCommandSource source) {
        source.getClient().schedule(SeedMapMinimapManager::hide);
        return Command.SINGLE_SUCCESS;
    }
}
