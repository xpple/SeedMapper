package dev.xpple.seedmapper.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.seedmapper.util.chat.Chat;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandException;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static dev.xpple.seedmapper.SeedMapper.CLIENT;
import static dev.xpple.seedmapper.util.TextUtil.stackTraceToString;
import static dev.xpple.seedmapper.util.chat.ChatBuilder.*;

public class ClientCommandManager {

    private static final Set<String> clientSideCommands = new HashSet<>();

    public static void clearClientSideCommands() {
        clientSideCommands.clear();
    }

    public static void addClientSideCommand(String name) {
        clientSideCommands.add(name);
    }

    public static boolean isClientSideCommand(String name) {
        return clientSideCommands.contains(name.toLowerCase(Locale.ROOT));
    }

    public static void executeCommand(StringReader reader, String command) {
        ClientPlayerEntity player = CLIENT.player;

        try {
            if (player != null) {
                player.networkHandler.getCommandDispatcher().execute(reader, new FakeCommandSource(player));
            }
        } catch (CommandException e) {
            Chat.error(e.getTextMessage().asString());
        } catch (CommandSyntaxException e) {
            Chat.error(e.getRawMessage().getString());

            if (e.getInput() != null && e.getCursor() >= 0) {
                int cursor = Math.min(e.getCursor(), e.getInput().length());

                LiteralText text = (LiteralText) new LiteralText("").formatted(Formatting.GRAY)
                        .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command)));

                if (cursor > 10) {
                    text.append("...");
                }

                text.append(e.getInput().substring(Math.max(0, cursor - 10), cursor));

                if (cursor < e.getInput().length()) {
                    text.append(
                            new LiteralText(e.getInput().substring(cursor)).formatted(Formatting.RED, Formatting.UNDERLINE));
                }

                text.append(new TranslatableText("command.context.here").formatted(Formatting.RED, Formatting.ITALIC));

                Chat.error(text.asString());
            }
        } catch (Exception e) {
            Chat.error(
                    hover(
                            chain(
                                    text("Command "),
                                    highlight(e.getClass().getName()),
                                    base(": "),
                                    highlight(e.getMessage())
                            ),
                            error(stackTraceToString(e))
                    )
            );
        }
    }
}
