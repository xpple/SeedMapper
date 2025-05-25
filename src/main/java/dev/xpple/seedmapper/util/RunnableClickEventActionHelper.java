package dev.xpple.seedmapper.util;

import net.minecraft.world.item.component.WritableBookContent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class RunnableClickEventActionHelper {

    private RunnableClickEventActionHelper() {
    }

    private static final Random random = new Random();

    public static final Map<Integer, Runnable> runnables = new HashMap<>();

    public static int registerCode(Runnable code) {
        int randomInt;
        do {
            randomInt = random.nextInt();
        } while ((randomInt >= 0 && randomInt < WritableBookContent.MAX_PAGES) || runnables.putIfAbsent(randomInt, code) != null);
        return randomInt;
    }
}
