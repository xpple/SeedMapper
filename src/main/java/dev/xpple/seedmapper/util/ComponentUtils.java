package dev.xpple.seedmapper.util;

import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Collection;

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

    public static MutableComponent formatXZCollection(Collection<? extends Vec3i> collection) {
        return join(Component.literal(", "), collection.stream().map(pos -> {
            return ComponentUtils.formatXZ(pos.getX(), pos.getZ());
        }));
    }

    public static MutableComponent formatXZCollection(Collection<? extends Vec3i> collection, MutableComponent copyText) {
        return join(Component.literal(", "), collection.stream().map(pos -> {
            return ComponentUtils.formatXZ(pos.getX(), pos.getZ(), copyText);
        }));
    }

    public static MutableComponent formatXYZCollection(Collection<? extends Vec3i> collection) {
        return join(Component.literal(", "), collection.stream().map(pos -> {
            return ComponentUtils.formatXYZ(pos.getX(), pos.getY(), pos.getZ());
        }));
    }

    public static MutableComponent formatXYZCollection(Collection<? extends Vec3i> collection, MutableComponent copyText) {
        return join(Component.literal(", "), collection.stream().map(pos -> {
            return ComponentUtils.formatXYZ(pos.getX(), pos.getY(), pos.getZ(), copyText);
        }));
    }
}
