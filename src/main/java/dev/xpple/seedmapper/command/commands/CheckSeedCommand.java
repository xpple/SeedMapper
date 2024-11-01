package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.command.arguments.SeedResolutionArgument;
import dev.xpple.seedmapper.util.ComponentUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import static dev.xpple.seedmapper.SeedMapper.*;
import static dev.xpple.seedmapper.util.ChatBuilder.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class CheckSeedCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("sm:checkseed")
            .executes(ctx -> checkSeed(CustomClientCommandSource.of(ctx.getSource()))));
    }

    private static int checkSeed(CustomClientCommandSource source) throws CommandSyntaxException {
        Pair<SeedResolutionArgument.SeedResolution.Method, Long> seedPair = source.getSeed();
        long seed = seedPair.getSecond();
        switch (seedPair.getFirst()) {
            case COMMAND_SOURCE -> source.sendFeedback(chain(
                Component.translatable("command.checkSeed.using", ComponentUtils.formatSeed(seed)),
                highlight(" "),
                format(
                    suggest(
                        Component.translatable("command.checkSeed.fromSource"),
                        String.format("/sm:source seeded %d run ", seed)
                    ),
                    ChatFormatting.UNDERLINE
                ),
                highlight(".")
            ));
            case SEED_CONFIG -> source.sendFeedback(chain(
                Component.translatable("command.checkSeed.using", ComponentUtils.formatSeed(seed)),
                highlight(" "),
                format(
                    file(
                        Component.translatable("command.checkSeed.fromSeed"),
                        modConfigPath.resolve("config.json").toAbsolutePath().toString()
                    ),
                    ChatFormatting.UNDERLINE
                ),
                highlight(".")
            ));
            case SAVED_SEEDS_CONFIG -> source.sendFeedback(chain(
                Component.translatable("command.checkSeed.using", ComponentUtils.formatSeed(seed)),
                highlight(" "),
                format(
                    file(
                        Component.translatable("command.checkSeed.fromSavedSeeds"),
                        modConfigPath.resolve("config.json").toAbsolutePath().toString()
                    ),
                    ChatFormatting.UNDERLINE
                ),
                highlight(".")
            ));
            case ONLINE_DATABASE -> source.sendFeedback(chain(
                Component.translatable("command.checkSeed.using", ComponentUtils.formatSeed(seed)),
                highlight(" "),
                format(
                    url(
                        Component.translatable("command.checkSeed.fromDatabase"),
                        "https://docs.google.com/spreadsheets/d/1tuQiE-0leW88em9OHbZnH-RFNhVqgoHhIt9WQbeqqWw"
                    ),
                    ChatFormatting.UNDERLINE
                ),
                highlight(".")
            ));
        }
        return Command.SINGLE_SUCCESS;
    }
}
