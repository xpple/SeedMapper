package dev.xpple.seedmapper.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static dev.xpple.seedmapper.util.ChatBuilder.*;

public final class ComponentUtils {

    private ComponentUtils() {
    }

    public static MutableComponent formatSeed(long seed) {
        return copy(
            hover(
                accent(String.valueOf(seed)),
                base(Component.translatable("chat.copy.click"))
            ),
            String.valueOf(seed)
        );
    }

    public static MutableComponent formatXZ(int x, int z) {
        return formatXZ(x, z, Component.translatable("chat.copy.click"));
    }

    public static MutableComponent formatXZ(int x, int z, MutableComponent copyText) {
        return copy(
            hover(
                accent("x: %d, z: %d".formatted(x, z)),
                base(copyText)
            ),
            "%d ~ %d".formatted(x, z)
        );
    }

    public static MutableComponent formatXYZ(int x, int y, int z) {
        return formatXYZ(x, y, z, Component.translatable("chat.copy.click"));
    }

    public static MutableComponent formatXYZ(int x, int y, int z, MutableComponent copyText) {
        return copy(
            hover(
                accent("x: %d, y: %d, z: %d".formatted(x, y, z)),
                base(copyText)
            ),
            "%d %d %d".formatted(x, y, z)
        );
    }
}
