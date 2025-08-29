package dev.xpple.seedmapper.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class BlockColorConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("seedmapper_block_colors.json");
    private static Map<String, Integer> customColors = new HashMap<>();

    static {
        loadConfig();
    }

    public static int getColor(String blockName, int defaultColor) {
        return customColors.getOrDefault(blockName, defaultColor);
    }

    public static void setColor(String blockName, int color) {
        customColors.put(blockName, color);
        saveConfig();
    }

    public static void resetColor(String blockName) {
        customColors.remove(blockName);
        saveConfig();
    }

    public static void resetAllColors() {
        customColors.clear();
        saveConfig();
    }

    private static void loadConfig() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                customColors = GSON.fromJson(json, new TypeToken<Map<String, Integer>>(){}.getType());
                if (customColors == null) {
                    customColors = new HashMap<>();
                }
            } catch (IOException e) {
                e.printStackTrace();
                customColors = new HashMap<>();
            }
        }
    }

    private static void saveConfig() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            String json = GSON.toJson(customColors);
            Files.writeString(CONFIG_PATH, json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}