package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.net.URI;

import static dev.xpple.seedmapper.util.ChatBuilder.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class DiscordCommand {

    private static final URI DISCORD_INVITE_LINK = URI.create("https://discord.xpple.dev/");

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("sm:discord")
            .executes(ctx -> sendDiscord(CustomClientCommandSource.of(ctx.getSource()))));
    }

    private static int sendDiscord(FabricClientCommandSource source) {
        source.sendFeedback(url(accent(DISCORD_INVITE_LINK.toString()), DISCORD_INVITE_LINK));
        return Command.SINGLE_SUCCESS;
    }
}
