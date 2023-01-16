package dev.xpple.seedmapper.util.chat;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import static dev.xpple.seedmapper.SeedMapper.CLIENT;
import static dev.xpple.seedmapper.SeedMapper.MOD_NAME;
import static dev.xpple.seedmapper.util.chat.ChatBuilder.*;

public class Chat {

    public static void toChat(MutableText... texts) {
        CLIENT.inGameHud.getChatHud().addMessage(ChatBuilder.chain(texts));
    }

    public static void toActionBar(MutableText... texts) {
        CLIENT.inGameHud.setOverlayMessage(ChatBuilder.chain(texts), false);
    }

    public static void print(MutableText... texts) {
        toChat(dark("["), accent(MOD_NAME), dark("] "), ChatBuilder.chain(texts));
    }

    public static void announce(String prefix, MutableText... texts) {
        toActionBar(dark("["), accent(MOD_NAME), dark("] "), base(prefix), ChatBuilder.chain(texts));
    }

    public static void announce(MutableText... texts) {
        announce("Log", texts);
    }

    public static void warn(String prefix, MutableText... texts) {
        toChat(dark("["), accent(MOD_NAME), dark("] "), ChatBuilder.warn(prefix),
                ChatBuilder.chain(texts));
    }

    public static void warn(MutableText... texts) {
        warn("Warn", texts);
    }

    public static void error(String prefix, MutableText... texts) {
        toChat(dark("["), accent(MOD_NAME), dark("] "), ChatBuilder.error(prefix), accent(" > "),
                ChatBuilder.chain(texts));
    }

    public static void error(MutableText... texts) {
        error("Error", texts);
    }

    public static void send(String text) {
        if (CLIENT.player != null) {
            CLIENT.player.sendMessage(Text.of(text));
        }
    }
}
