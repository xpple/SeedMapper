package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.seedmap.MinimapManager;
import dev.xpple.seedmapper.util.SeedIdentifier;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class MinimapCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("sm:minimap")
            .executes(ctx -> toggle(CustomClientCommandSource.of(ctx.getSource())))
            .then(literal("show")
                .executes(ctx -> toggle(CustomClientCommandSource.of(ctx.getSource()), true)))
            .then(literal("hide")
                .executes(ctx -> toggle(CustomClientCommandSource.of(ctx.getSource()), false))));
    }

    private static int toggle(CustomClientCommandSource source) throws CommandSyntaxException {
        return toggle(source, !MinimapManager.isVisible());
    }

    private static int toggle(CustomClientCommandSource source, boolean show) throws CommandSyntaxException {
        if (show) {
            SeedIdentifier seed = source.getSeed().getSecond();
            int dimension = source.getDimension();
            int version = source.getVersion();
            int generatorFlags = source.getGeneratorFlags();
            MinimapManager.show(seed.seed(), dimension, version, generatorFlags);
            return 1;
        } else {
            MinimapManager.hide();
            return 0;
        }
    }
}
