package dev.xpple.seedmapper.command;

import com.mojang.brigadier.StringReader;

public class CommandReader extends StringReader {
    public CommandReader(String command) {
        super(command);
    }

    public String readUnquotedString() {
        final int start = getCursor();
        while (canRead() && isAllowedInCommand(peek())) {
            skip();
        }
        return getString().substring(start, getCursor());
    }

    public static boolean isAllowedInCommand(final char c) {
        return c >= '0' && c <= '9'
                || c >= 'A' && c <= 'Z'
                || c >= 'a' && c <= 'z'
                || c == '_' || c == '-'
                || c == '.' || c == '+'
                || c == ':';
    }
}
