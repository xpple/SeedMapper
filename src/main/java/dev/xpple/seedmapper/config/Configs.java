package dev.xpple.seedmapper.config;

import com.google.common.base.Suppliers;
import dev.xpple.betterconfig.api.BetterConfigAPI;
import dev.xpple.betterconfig.api.Config;
import dev.xpple.betterconfig.api.ModConfig;
import dev.xpple.seedmapper.SeedMapper;
import dev.xpple.seedmapper.command.arguments.SeedResolutionArgument;
import dev.xpple.seedmapper.seedmap.MapFeature;
import dev.xpple.seedmapper.seedmap.SeedMapScreen;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static dev.xpple.seedmapper.util.ChatBuilder.*;

@SuppressWarnings("unused")
public class Configs {
    public static final Supplier<ModConfig<Component>> CONFIG_REF = Suppliers.memoize(() -> BetterConfigAPI.getInstance().getModConfig(SeedMapper.MOD_ID));

    public static void save() {
        Configs.CONFIG_REF.get().save();
    }

    @Config
    public static Long Seed = null;

    @Config(putter = @Config.Putter("none"), adder = @Config.Adder(value = "addSavedSeed", type = long.class))
    public static Map<String, Long> SavedSeeds = new HashMap<>();
    private static void addSavedSeed(long seed) {
        String key = Minecraft.getInstance().getConnection().getConnection().getRemoteAddress().toString();
        SavedSeeds.put(key, seed);
    }

    @Config
    public static SeedResolutionArgument.SeedResolution SeedResolutionOrder = new SeedResolutionArgument.SeedResolution();

    @Config(comment = "getOreAreCheckComment")
    public static boolean OreAirCheck = true;

    private static Component getOreAreCheckComment() {
        return Component.translatable("config.oreAirCheck.comment");
    }

    private static final int MAX_THREADS = Math.max(1, Runtime.getRuntime().availableProcessors() - 2);
    @Config(setter = @Config.Setter("setSeedMapThreads"))
    public static int SeedMapThreads = MAX_THREADS;

    private static void setSeedMapThreads(int seedMapThreads) {
        SeedMapThreads = Math.clamp(seedMapThreads, 1, MAX_THREADS);
    }

    @Config(setter = @Config.Setter("setPixelsPerBiome"))
    public static int PixelsPerBiome = 4;

    private static void setPixelsPerBiome(int pixelsPerBiome) {
        PixelsPerBiome = Math.clamp(pixelsPerBiome, SeedMapScreen.MIN_PIXELS_PER_BIOME, SeedMapScreen.MAX_PIXELS_PER_BIOME);
    }

    @Config(chatRepresentation = "listToggledFeatures")
    public static EnumSet<MapFeature> ToggledFeatures = Util.make(() -> {
        EnumSet<MapFeature> toggledFeatures = EnumSet.allOf(MapFeature.class);
        toggledFeatures.remove(MapFeature.SLIME_CHUNK);
        return toggledFeatures;
    });

    public static Component listToggledFeatures() {
        return join(Component.literal(", "), ToggledFeatures.stream()
            .map(MapFeature::getName)
            .map(Component::literal));
    }

    @Config(comment = "getDevModeComment")
    public static boolean DevMode = false;

    public static Component getDevModeComment() {
        return Component.translatable("config.devMode.comment");
    }
}
