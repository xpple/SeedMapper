package dev.xpple.seedmapper.config;

import com.google.common.base.Suppliers;
import dev.xpple.betterconfig.api.BetterConfigAPI;
import dev.xpple.betterconfig.api.Config;
import dev.xpple.betterconfig.api.ModConfig;
import dev.xpple.seedmapper.SeedMapper;
import dev.xpple.seedmapper.command.arguments.SeedResolutionArgument;
import dev.xpple.seedmapper.render.RenderManager;
import dev.xpple.seedmapper.seedmap.MapFeature;
import dev.xpple.seedmapper.seedmap.SeedMapScreen;
import dev.xpple.seedmapper.util.ComponentUtils;
import dev.xpple.seedmapper.util.SeedIdentifier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

import java.time.Duration;
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

    @Config(chatRepresentation = "displaySeed")
    public static SeedIdentifier Seed = null;
    private static Component displaySeed() {
        return ComponentUtils.formatSeed(Seed);
    }

    @Config(putter = @Config.Putter("none"), adder = @Config.Adder(value = "addSavedSeed", type = SeedIdentifier.class), chatRepresentation = "displaySavedSeeds")
    public static Map<String, SeedIdentifier> SavedSeeds = new HashMap<>();
    private static void addSavedSeed(SeedIdentifier seed) {
        String key = Minecraft.getInstance().getConnection().getConnection().getRemoteAddress().toString();
        SavedSeeds.put(key, seed);
    }
    private static Component displaySavedSeeds() {
        return join(Component.literal(", "), SavedSeeds.entrySet().stream()
            .map(entry -> chain(
                copy(
                    hover(
                        Component.literal(entry.getKey()).withStyle(ChatFormatting.UNDERLINE),
                        base(Component.translatable("chat.copy.click"))),
                    entry.getKey()
                ),
                Component.literal(": "),
                ComponentUtils.formatSeed(entry.getValue()))
            )
        );
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
    public static int MinimapOffsetX = 4;

    private static void setMinimapOffsetX(int offsetX) {
        MinimapOffsetX = Math.max(0, offsetX);
    }

    @Config(setter = @Config.Setter("setMinimapOffsetY"))
    public static int MinimapOffsetY = 4;

    private static void setMinimapOffsetY(int offsetY) {
        MinimapOffsetY = Math.max(0, offsetY);
    }

    @Config(setter = @Config.Setter("setMinimapWidth"))
    public static int MinimapWidth = 205;

    private static void setMinimapWidth(int width) {
        MinimapWidth = Math.clamp(width, 64, 512);
    }

    @Config(setter = @Config.Setter("setMinimapHeight"))
    public static int MinimapHeight = 205;

    private static void setMinimapHeight(int height) {
        MinimapHeight = Math.clamp(height, 64, 512);
    }

    @Config
    public static boolean RotateMinimap = true;

    @Config(chatRepresentation = "listToggledFeatures")
    public static EnumSet<MapFeature> ToggledFeatures = Util.make(() -> {
        EnumSet<MapFeature> toggledFeatures = EnumSet.allOf(MapFeature.class);
        toggledFeatures.remove(MapFeature.SLIME_CHUNK);
        return toggledFeatures;
    });

    private static Component listToggledFeatures() {
        return join(Component.literal(", "), ToggledFeatures.stream()
            .map(MapFeature::getName)
            .map(Component::literal));
    }

    @Config(comment = "getDevModeComment")
    public static boolean DevMode = false;

    private static Component getDevModeComment() {
        return Component.translatable("config.devMode.comment");
    }

    @Config(onChange = "updateHighlightDuration")
    public static Duration HighlightDuration = Duration.ofMinutes(5);
    private static void updateHighlightDuration(Duration oldValue, Duration newValue) {
        RenderManager.rebuildLineSet();
    }
}
