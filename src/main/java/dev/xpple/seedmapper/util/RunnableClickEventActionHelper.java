package dev.xpple.seedmapper.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RunnableClickEventActionHelper {

    public static final Map<String, Runnable> runnables = new HashMap<>();

    public static String registerCode(Runnable code) {
        String randomString = TextUtil.random(10);
        runnables.put(randomString, code);
        return randomString;
    }
}
