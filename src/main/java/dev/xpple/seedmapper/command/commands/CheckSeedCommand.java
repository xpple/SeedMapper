package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.command.arguments.SeedResolutionArgument;
import dev.xpple.seedmapper.util.ComponentUtils;
import dev.xpple.seedmapper.util.SeedIdentifier;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.net.URI;

import static dev.xpple.seedmapper.SeedMapper.*;
import static dev.xpple.seedmapper.util.ChatBuilder.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class CheckSeedCommand {

    private static final URI DATABASE_URL = URI.create("https://docs.google.com/spreadsheets/d/1tuQiE-0leW88em9OHbZnH-RFNhVqgoHhIt9WQbeqqWw");

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("sm:checkseed")
            .executes(ctx -> checkSeed(CustomClientCommandSource.of(ctx.getSource()))));
    }

    private static int checkSeed(CustomClientCommandSource source) throws CommandSyntaxException {
        Pair<SeedResolutionArgument.SeedResolution.Method, SeedIdentifier> seedPair = source.getSeed();
        SeedIdentifier seed = seedPair.getSecond();
        switch (seedPair.getFirst()) {
            case COMMAND_SOURCE -> source.sendFeedback(Component.translatable("command.checkSeed.using", ComponentUtils.formatSeed(seed),
                format(
                    suggest(
                        Component.translatable("command.checkSeed.fromSource"),
                        "/sm:source seeded %d run ".formatted(seed.seed())
                    ),
                    ChatFormatting.UNDERLINE
                ))
            );
            case SEED_CONFIG -> source.sendFeedback(Component.translatable("command.checkSeed.using", ComponentUtils.formatSeed(seed),
                format(
                    file(
                        Component.translatable("command.checkSeed.fromSeed"),
                        modConfigPath.resolve("config.json").toAbsolutePath().toString()
                    ),
                    ChatFormatting.UNDERLINE
                ))
            );
            case SAVED_SEEDS_CONFIG -> source.sendFeedback(Component.translatable("command.checkSeed.using", ComponentUtils.formatSeed(seed),
                format(
                    file(
                        Component.translatable("command.checkSeed.fromSavedSeeds"),
                        modConfigPath.resolve("config.json").toAbsolutePath().toString()
                    ),
                    ChatFormatting.UNDERLINE
                ))
            );
            case ONLINE_DATABASE -> source.sendFeedback(Component.translatable("command.checkSeed.using", ComponentUtils.formatSeed(seed),
                format(
                    url(
                        Component.translatable("command.checkSeed.fromDatabase"),
                        DATABASE_URL
                    ),
                    ChatFormatting.UNDERLINE
                ))
            );
        }
        return Command.SINGLE_SUCCESS;
    }
}
