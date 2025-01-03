package dev.xpple.seedmapper.util;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.text.NumberFormat;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.xpple.seedmapper.util.ChatBuilder.*;

public final class ComponentUtils {

    private ComponentUtils() {
    }

    private static final NumberFormat numberFormat = NumberFormat.getInstance();

    static {
        numberFormat.setMaximumFractionDigits(1);
        numberFormat.setMinimumFractionDigits(1);
    }

    public static String formatNumber(double number) {
        return numberFormat.format(number);
    }

    public static String center(String text, int len) {
        if (len <= text.length()) {
            return text.substring(0, len);
        }
        int before = (len - text.length()) / 2;
        if (before == 0) {
            return String.format("%-" + len + "s", text);
        }
        int rest = len - before;
        return String.format("%" + before + "s%-" + rest + "s", "", text);
    }

    public static MutableComponent formatList(List<Component> list) {
        MutableComponent output = Component.literal("");

        AtomicInteger count = new AtomicInteger(0);

        list.forEach(text -> {
            int index = count.getAndIncrement();

            output
                .append(text)
                .append(base(index == list.size() - 1 ? "" : index == list.size() - 2 ? " and " : ", "));
        });

        return output;
    }

    public static MutableComponent appendAll(MutableComponent text, List<Component> toAppend) {
        toAppend.forEach(text::append);
        return text;
    }

    public static String random(int length) {
        // 0 to z
        int leftLimit = 48;
        int rightLimit = 122;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
            .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
            .limit(length)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }

    public static String stackTraceToString(Throwable e) {
        StringBuilder stringBuilder = new StringBuilder();

        boolean first = true;

        do {
            if (!first) {
                e = e.getCause();
            }

            for (StackTraceElement element : e.getStackTrace()) {
                stringBuilder.append(element.toString());
                stringBuilder.append("\n");
            }

            first = false;
        } while (e.getCause() != null);

        return stringBuilder.toString();
    }

    public static String formatAxisDirection(Direction.AxisDirection axisDirection) {
        return axisDirection.equals(Direction.AxisDirection.POSITIVE) ? "+" : "-";
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
}
