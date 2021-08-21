package dev.xpple.seedmapper.util.config;

import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static dev.xpple.seedmapper.SeedMapper.MOD_ID;
import static dev.xpple.seedmapper.SeedMapper.MOD_PATH;

public class Config {

    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    private static final Path configPath = Paths.get(MOD_PATH + File.separator + "config.json");

    private static final JsonParser parser = new JsonParser();
    private static JsonObject root = null;

    private static final Set<String> ignoredBlocks = new HashSet<>();
    private static final Map<String, Long> seeds = new HashMap<>();
    private static final Map<String, short[]> colors = new HashMap<>();

    public static void init() {
        try {
            if (configPath.toFile().exists() && configPath.toFile().isFile()) {
                root = parser.parse(Files.newBufferedReader(configPath)).getAsJsonObject();
                if (root.has("seeds")) {
                    for (Map.Entry<String, JsonElement> element : root.getAsJsonObject("seeds").entrySet()) {
                        seeds.put(element.getKey(), element.getValue().getAsLong());
                    }
                } else {
                    root.add("seeds", new JsonObject());
                }
                if (root.has("ignoredBlocks")) {
                    for (JsonElement element : root.getAsJsonArray("ignoredBlocks")) {
                        ignoredBlocks.add(element.getAsString());
                    }
                } else {
                    root.add("ignoredBlocks", new JsonArray());
                }
                if (root.has("automate")) {
                    JsonObject automate = root.getAsJsonObject("automate");
                    automate.addProperty("enabled", false);
                } else {
                    JsonObject automate = new JsonObject();
                    automate.addProperty("enabled", false);
                    root.add("automate", automate);
                }
                if (root.has("colors")) {
                    for (Map.Entry<String, JsonElement> element : root.getAsJsonObject("colors").entrySet()) {
                        JsonArray rgbArray = element.getValue().getAsJsonArray();
                        colors.put(element.getKey(), new short[]{rgbArray.get(0).getAsShort(), rgbArray.get(1).getAsShort(), rgbArray.get(2).getAsShort()});
                    }
                } else {
                    JsonObject colors = new JsonObject();
                    root.add("colors", colors);
                }
            } else {
                String standardJson = """
                        {
                          "seed": null,
                          "seeds": {},
                          "ignoredBlocks": [],
                          "automate": {
                            "enabled": false
                          },
                          "colors": {}
                        }""";
                root = parser.parse(standardJson).getAsJsonObject();
            }
            save();
        } catch (IOException e) {
            LOGGER.error("Could not load config file. Your client may crash due to this.");
        }
    }

    private static void save() {
        try (final BufferedWriter writer = Files.newBufferedWriter(configPath)) {
            JsonObject jsonSeeds = new JsonObject();
            seeds.forEach(jsonSeeds::addProperty);
            root.add("seeds", jsonSeeds);
            JsonArray jsonIgnoredBlocks = new JsonArray();
            ignoredBlocks.forEach(jsonIgnoredBlocks::add);
            root.add("ignoredBlocks", jsonIgnoredBlocks);
            JsonObject jsonColors = new JsonObject();
            colors.forEach((block, shorts) -> {
                JsonArray rgbArray = new JsonArray();
                rgbArray.add(shorts[0]);
                rgbArray.add(shorts[1]);
                rgbArray.add(shorts[2]);
                jsonColors.add(block, rgbArray);
            });
            root.add("colors", jsonColors);
            writer.write(new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(root));
        } catch (IOException e) {
            LOGGER.error("Could not save config file. Your client may crash due to this.");
        }
    }

    public static JsonElement get(String key) {
        JsonElement element = root.get(key);
        if (element == null) {
            throw new JsonIOException("No mapping found for " + key);
        }
        return element;
    }

    public static boolean isEnabled(String key) {
        JsonObject object = get(key).getAsJsonObject();
        if (object.has("enabled")) {
            return object.get("enabled").getAsBoolean();
        }
        throw new JsonParseException("No member \"enabled\" found for " + key);
    }

    public static void toggle(String key, boolean bool) {
        JsonObject object = get(key).getAsJsonObject();
        if (object.has("enabled")) {
            object.addProperty("enabled", bool);
            save();
            return;
        }
        throw new JsonParseException("No member \"enabled\" found for " + key);
    }

    public static boolean addSeed(String key, long seed) {
        if (seeds.containsKey(key)) {
            return false;
        }
        seeds.put(key, seed);
        save();
        return true;
    }

    public static boolean removeSeed(String key) {
        if (seeds.containsKey(key)) {
            seeds.remove(key);
            save();
            return true;
        }
        return false;
    }

    public static Map<String, Long> getSeeds() {
        return seeds;
    }

    public static boolean addBlock(String block) {
        if (ignoredBlocks.contains(block)) {
            return false;
        }
        ignoredBlocks.add(block);
        save();
        return true;
    }

    public static boolean removeBlock(String block) {
        if (ignoredBlocks.contains(block)) {
            ignoredBlocks.remove(block);
            save();
            return true;
        }
        return false;
    }

    public static Set<String> getIgnoredBlocks() {
        return ignoredBlocks;
    }

    public static boolean addColor(String key, short[] color) {
        if (colors.containsKey(key)) {
            return false;
        }
        colors.put(key, color);
        save();
        return true;
    }

    public static boolean removeColor(String key) {
        if (colors.containsKey(key)) {
            colors.remove(key);
            save();
            return true;
        }
        return false;
    }

    public static Map<String, short[]> getColors() {
        return colors;
    }

    public static void set(String key, String value) {
        root.addProperty(key, value);
        save();
    }

    public static void set(String key, boolean value) {
        root.addProperty(key, value);
        save();
    }

    public static void set(String key, Number value) {
        root.addProperty(key, value);
        save();
    }

    public static void set(String key, Character value) {
        root.addProperty(key, value);
        save();
    }
}
