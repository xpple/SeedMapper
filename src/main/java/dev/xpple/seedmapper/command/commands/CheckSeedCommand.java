package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.util.DatabaseHelper;
import dev.xpple.seedmapper.util.TextUtil;
import dev.xpple.seedmapper.util.chat.Chat;
import dev.xpple.seedmapper.util.config.Configs;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;
import java.nio.file.Paths;

import static dev.xpple.seedmapper.SeedMapper.CLIENT;
import static dev.xpple.seedmapper.SeedMapper.MOD_PATH;
import static dev.xpple.seedmapper.util.chat.ChatBuilder.*;

public class CheckSeedCommand extends ClientCommand {

    @Override
    protected void build(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        argumentBuilder
                .executes(ctx -> checkSeed(CustomClientCommandSource.of(ctx.getSource())));
    }

    @Override
    protected String rootLiteral() {
        return "checkseed";
    }

    @Override
    protected String alias() {
        return "seed";
    }

    private int checkSeed(CustomClientCommandSource source) {
        Long seed = (Long) source.getMeta("seed");
        if (seed != null) {
            Chat.print("", chain(
                    Text.translatable("command.checkSeed.using", TextUtil.formatSeed(seed)),
                    highlight(" "),
                    format(
                            Text.translatable("command.checkSeed.fromSource"),
                            Formatting.UNDERLINE
                    ).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/sm:source seeded %s run", source.getMeta("seed"))))),
                    highlight(".")
            ));
            return Command.SINGLE_SUCCESS;
        }
        String key = CLIENT.getNetworkHandler().getConnection().getAddress().toString();
        seed = Configs.SavedSeeds.get(key);
        if (seed != null) {
            Chat.print("", chain(
                    Text.translatable("command.checkSeed.using", TextUtil.formatSeed(seed)),
                    highlight(" "),
                    format(
                            Text.translatable("command.checkSeed.fromSavedSeeds"),
                            Formatting.UNDERLINE
                    ).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, Paths.get(MOD_PATH + File.separator + "config.json").toAbsolutePath().toString()))),
                    highlight(".")
            ));
            return Command.SINGLE_SUCCESS;
        }
        seed = DatabaseHelper.getSeed(key);
        if (seed != null) {
            Chat.print("", chain(
                    Text.translatable("command.checkSeed.using", TextUtil.formatSeed(seed)),
                    highlight(" "),
                    format(
                            Text.translatable("command.checkSeed.fromDatabase"),
                            Formatting.UNDERLINE
                    ).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://docs.google.com/spreadsheets/d/1tuQiE-0leW88em9OHbZnH-RFNhVqgoHhIt9WQbeqqWw"))),
                    highlight(".")
            ));
            return Command.SINGLE_SUCCESS;
        }
        seed = Configs.Seed;
        if (seed != null) {
            Chat.print("", chain(
                    Text.translatable("command.checkSeed.using", TextUtil.formatSeed(seed)),
                    highlight(" "),
                    format(
                            Text.translatable("command.checkSeed.fromSeed"),
                            Formatting.UNDERLINE
                    ).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, Paths.get(MOD_PATH + File.separator + "config.json").toAbsolutePath().toString()))),
                    highlight(".")
            ));
            return Command.SINGLE_SUCCESS;
        }
        Chat.print("", Text.translatable("command.checkSeed.none"));
        return Command.SINGLE_SUCCESS;
    }
}
