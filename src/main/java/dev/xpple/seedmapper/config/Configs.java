package dev.xpple.seedmapper.config;

import com.google.common.base.Suppliers;
import dev.xpple.betterconfig.api.BetterConfigAPI;
import dev.xpple.betterconfig.api.Config;
import dev.xpple.betterconfig.api.ModConfig;
import dev.xpple.seedmapper.SeedMapper;
import dev.xpple.seedmapper.command.arguments.SeedResolutionArgument;
import dev.xpple.seedmapper.seedmap.MapFeature;
import dev.xpple.seedmapper.seedmap.SeedMapScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

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

    @Config(setter = @Config.Setter("setMinimapOffsetX"))
    public static int SeedMapMinimapOffsetX = 4;

    private static void setMinimapOffsetX(int offsetX) {
        SeedMapMinimapOffsetX = Math.max(0, offsetX);
    }

    @Config(setter = @Config.Setter("setMinimapOffsetY"))
    public static int SeedMapMinimapOffsetY = 4;

    private static void setMinimapOffsetY(int offsetY) {
        SeedMapMinimapOffsetY = Math.max(0, offsetY);
    }

    @Config(setter = @Config.Setter("setMinimapWidth"))
    public static int SeedMapMinimapWidth = 205;

    private static void setMinimapWidth(int width) {
        SeedMapMinimapWidth = Math.clamp(width, 64, 512);
    }

    @Config(setter = @Config.Setter("setMinimapHeight"))
    public static int SeedMapMinimapHeight = 205;

    private static void setMinimapHeight(int height) {
        SeedMapMinimapHeight = Math.clamp(height, 64, 512);
    }

    @Config
    public static boolean SeedMapMinimapRotateWithPlayer = true;

    @Config(setter = @Config.Setter("setMinimapPixelsPerBiome"))
    public static double SeedMapMinimapPixelsPerBiome = 1.5D;

    private static void setMinimapPixelsPerBiome(double pixelsPerBiome) {
        SeedMapMinimapPixelsPerBiome = Math.clamp(pixelsPerBiome, SeedMapScreen.MIN_PIXELS_PER_BIOME, SeedMapScreen.MAX_PIXELS_PER_BIOME);
    }

    @Config(setter = @Config.Setter("setMinimapIconScale"))
    public static double SeedMapMinimapIconScale = 0.5D;

    private static void setMinimapIconScale(double iconScale) {
        SeedMapMinimapIconScale = Math.clamp(iconScale, 0.25D, 4.0D);
    }

    @Config(setter = @Config.Setter("setMinimapOpacity"))
    public static double SeedMapMinimapOpacity = 1.0D;

    private static void setMinimapOpacity(double opacity) {
        SeedMapMinimapOpacity = Math.clamp(opacity, 0.00D, 1.0D);
    }

    @Config(comment = "getPlayerDirectionArrowComment")
    public static boolean ShowPlayerDirectionArrow = true;

    private static Component getPlayerDirectionArrowComment() {
        return Component.translatable("config.showPlayerDirectionArrow.comment");
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
