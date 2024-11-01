package dev.xpple.seedmapper.util;

import java.util.HashMap;
import java.util.Map;

public final class RunnableClickEventActionHelper {

    private RunnableClickEventActionHelper() {
    }

    public static final Map<String, Runnable> runnables = new HashMap<>();

    public static String registerCode(Runnable code) {
        String randomString = ComponentUtils.random(10);
        runnables.put(randomString, code);
        return randomString;
    }
}
