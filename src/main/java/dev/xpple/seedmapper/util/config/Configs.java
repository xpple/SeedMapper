package dev.xpple.seedmapper.util.config;

import dev.xpple.betterconfig.api.Config;
import net.minecraft.block.Block;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static dev.xpple.seedmapper.SeedMapper.CLIENT;

@SuppressWarnings("unused")
public class Configs {
    @Config
    public static Long Seed = null;

    @Config(putter = @Config.Putter("none"), adder = @Config.Adder(value = "addSavedSeed", type = long.class))
    public static Map<String, Long> SavedSeeds = new HashMap<>();
    public static void addSavedSeed(long seed) {
        String key = CLIENT.getNetworkHandler().getConnection().getAddress().toString();
        SavedSeeds.put(key, seed);
    }

    @Config
    public static Set<Block> IgnoredBlocks = new HashSet<>();

    @Config
    public static boolean AutoOverlay = false;

    @Config
    public static Map<Block, Integer> BlockColours = new HashMap<>();

    @Config
    public static SeedResolution SeedResolutionOrder = new SeedResolution();

    @Config
    public static boolean UseWorldSimulation = false;
}
