package dev.xpple.seedmapper.config;

import com.github.cubiomes.Cubiomes;
import com.google.common.base.Suppliers;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.betterconfig.api.BetterConfigAPI;
import dev.xpple.betterconfig.api.Config;
import dev.xpple.betterconfig.api.ModConfig;
import dev.xpple.seedmapper.SeedMapper;
import dev.xpple.seedmapper.command.CommandExceptions;
import dev.xpple.seedmapper.command.arguments.BlockArgument;
import dev.xpple.seedmapper.command.arguments.SeedResolutionArgument;
import dev.xpple.seedmapper.render.RenderManager;
import dev.xpple.seedmapper.seedmap.MapFeature;
import dev.xpple.seedmapper.seedmap.SeedMapScreen;
import dev.xpple.seedmapper.util.BaritoneIntegration;
import dev.xpple.seedmapper.util.ComponentUtils;
import dev.xpple.seedmapper.util.SeedIdentifier;
import dev.xpple.simplewaypoints.api.SimpleWaypointsAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.level.material.MapColor;

import java.time.Duration;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HexFormat;
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
        String key = SimpleWaypointsAPI.getInstance().getWorldIdentifier(Minecraft.getInstance());
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

    @Config(putter = @Config.Putter(value = "putBlockColor", keyType = String.class, valueType = ColorWrapper.class), remover = @Config.Remover(value = "none"), chatRepresentation = "displayBlockColors")
    public static Map<Integer, Integer> BlockColors = new HashMap<>(Map.ofEntries(
        Map.entry(Cubiomes.ANCIENT_DEBRIS(), MapColor.TERRACOTTA_BROWN.col),
        Map.entry(Cubiomes.ANDESITE(), MapColor.STONE.col),
        Map.entry(Cubiomes.BASALT(), MapColor.COLOR_BLACK.col),
        Map.entry(Cubiomes.BLACKSTONE(), MapColor.COLOR_BLACK.col),
        Map.entry(Cubiomes.CLAY(), MapColor.CLAY.col),
        Map.entry(Cubiomes.COAL_ORE(), MapColor.COLOR_BLACK.col),
        Map.entry(Cubiomes.COPPER_ORE(), MapColor.COLOR_ORANGE.col),
        Map.entry(Cubiomes.DEEPSLATE(), MapColor.DEEPSLATE.col),
        Map.entry(Cubiomes.DIAMOND_ORE(), MapColor.DIAMOND.col),
        Map.entry(Cubiomes.DIORITE(), MapColor.QUARTZ.col),
        Map.entry(Cubiomes.DIRT(), MapColor.DIRT.col),
        Map.entry(Cubiomes.EMERALD_ORE(), MapColor.EMERALD.col),
        Map.entry(Cubiomes.GOLD_ORE(), MapColor.GOLD.col),
        Map.entry(Cubiomes.GRANITE(), MapColor.DIRT.col),
        Map.entry(Cubiomes.GRAVEL(), MapColor.STONE.col),
        Map.entry(Cubiomes.IRON_ORE(), MapColor.RAW_IRON.col),
        Map.entry(Cubiomes.LAPIS_ORE(), MapColor.LAPIS.col),
        Map.entry(Cubiomes.MAGMA_BLOCK(), MapColor.NETHER.col),
        Map.entry(Cubiomes.NETHERRACK(), MapColor.NETHER.col),
        Map.entry(Cubiomes.NETHER_GOLD_ORE(), MapColor.GOLD.col),
        Map.entry(Cubiomes.NETHER_QUARTZ_ORE(), MapColor.QUARTZ.col),
        Map.entry(Cubiomes.RAW_COPPER_BLOCK(), MapColor.COLOR_YELLOW.col),
        Map.entry(Cubiomes.RAW_IRON_BLOCK(), MapColor.COLOR_YELLOW.col),
        Map.entry(Cubiomes.REDSTONE_ORE(), MapColor.FIRE.col),
        Map.entry(Cubiomes.SOUL_SAND(), MapColor.COLOR_BROWN.col),
        Map.entry(Cubiomes.STONE(), MapColor.STONE.col),
        Map.entry(Cubiomes.TUFF(), MapColor.COLOR_GRAY.col)
    ));
    private static void putBlockColor(String key, ColorWrapper value) throws CommandSyntaxException {
        Integer block = BlockArgument.BLOCKS.get(key);
        if (block == null) {
            throw CommandExceptions.UNKNOWN_BLOCK_EXCEPTION.create(key);
        }
        BlockColors.put(block, value.argb());
    }
    private static Component displayBlockColors() {
        return join(Component.literal(", "), BlockColors.entrySet().stream()
            .map(entry -> chain(
                Component.literal(Cubiomes.block2str(entry.getKey()).getString(0)),
                Component.literal(": "),
                accent(HexFormat.of().toHexDigits(entry.getValue(), 6)))
            )
        );
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

    @Config(condition = "hasBaritoneAvailable", onChange = "updateBaritoneGoals")
    public static boolean AutoMine = false;
    private static boolean hasBaritoneAvailable(SharedSuggestionProvider source) {
        return SeedMapper.BARITONE_AVAILABLE;
    }
    private static void updateBaritoneGoals(boolean oldValue, boolean newValue) {
        if (!newValue) {
            BaritoneIntegration.clearGoals();
        }
    }
}
