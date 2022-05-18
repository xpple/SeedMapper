package dev.xpple.seedmapper.command.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.brigadier.Command;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.util.TextUtil;
import dev.xpple.seedmapper.util.chat.Chat;
import dev.xpple.seedmapper.util.config.Config;
import dev.xpple.seedmapper.util.database.DatabaseHelper;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.io.File;
import java.nio.file.Paths;

import static dev.xpple.seedmapper.SeedMapper.CLIENT;
import static dev.xpple.seedmapper.SeedMapper.MOD_PATH;
import static dev.xpple.seedmapper.util.chat.ChatBuilder.*;

public class CheckSeedCommand extends ClientCommand {

    @Override
    protected void build() {
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

    // TODO: 18/05/2022 await poll seed priority
    private int checkSeed(CustomClientCommandSource source) {
        String key = CLIENT.getNetworkHandler().getConnection().getAddress().toString();
        Long seed = Config.getSeeds().get(key);
        if (seed != null) {
            Chat.print("", chain(
                    new TranslatableText("command.checkSeed.using", TextUtil.formatSeed(seed)),
                    highlight(" "),
                    format(
                            new TranslatableText("command.checkSeed.fromSeeds"),
                            Formatting.UNDERLINE
                    ).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, Paths.get(MOD_PATH + File.separator + "config.json").toAbsolutePath().toString()))),
                    highlight(".")
            ));
            return Command.SINGLE_SUCCESS;
        }
        seed = DatabaseHelper.getSeed(key);
        if (seed != null) {
            Chat.print("", chain(
                    new TranslatableText("command.checkSeed.using", TextUtil.formatSeed(seed)),
                    highlight(" "),
                    format(
                            new TranslatableText("command.checkSeed.fromDatabase"),
                            Formatting.UNDERLINE
                    ).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://docs.google.com/spreadsheets/d/1tuQiE-0leW88em9OHbZnH-RFNhVqgoHhIt9WQbeqqWw"))),
                    highlight(".")
            ));
            return Command.SINGLE_SUCCESS;
        }
        JsonElement element = Config.get("seed");
        if (!(element instanceof JsonNull)) {
            seed = element.getAsLong();
            Chat.print("", chain(
                    new TranslatableText("command.checkSeed.using", TextUtil.formatSeed(seed)),
                    highlight(" "),
                    format(
                            new TranslatableText("command.checkSeed.fromSeed"),
                            Formatting.UNDERLINE
                    ).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, Paths.get(MOD_PATH + File.separator + "config.json").toAbsolutePath().toString()))),
                    highlight(".")
            ));
            return Command.SINGLE_SUCCESS;
        }
        Chat.print("", new TranslatableText("command.checkSeed.none"));
        return Command.SINGLE_SUCCESS;
    }
}
