package dev.xpple.seedmapper.config;

import dev.xpple.betterconfig.api.Config;
import dev.xpple.seedmapper.command.arguments.SeedResolutionArgument;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class Configs {
    @Config
    public static Long Seed = null;

    @Config(putter = @Config.Putter("none"), adder = @Config.Adder(value = "addSavedSeed", type = long.class))
    public static Map<String, Long> SavedSeeds = new HashMap<>();
    public static void addSavedSeed(long seed) {
        String key = Minecraft.getInstance().getConnection().getConnection().getRemoteAddress().toString();
        SavedSeeds.put(key, seed);
    }

    @Config
    public static SeedResolutionArgument.SeedResolution SeedResolutionOrder = new SeedResolutionArgument.SeedResolution();

    @Config(comment = "Whether or not SeedMapper should use in-game air checks to invalidate ore positions.")
    public static boolean OreAirCheck = true;
}
