package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.seedmap.SeedMapScreen;
import dev.xpple.seedmapper.util.SeedIdentifier;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.core.BlockPos;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class SeedMapCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("sm:seedmap")
            .executes(ctx -> seedMap(CustomClientCommandSource.of(ctx.getSource()))));
    }

    private static int seedMap(CustomClientCommandSource source) throws CommandSyntaxException {
        SeedIdentifier seed = source.getSeed().getSecond();
        int dimension = source.getDimension();
        int version = source.getVersion();
        int generatorFlags = source.getGeneratorFlags();
        source.getClient().schedule(() -> source.getClient().setScreen(new SeedMapScreen(seed.seed(), dimension, version, generatorFlags, BlockPos.containing(source.getPosition()), source.getRotation())));
        return Command.SINGLE_SUCCESS;
    }
}
