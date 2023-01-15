package dev.xpple.seedmapper.util.config;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.block.Block;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.xpple.seedmapper.SeedMapper.MOD_ID;
import static dev.xpple.seedmapper.SeedMapper.MOD_PATH;

public class ConfigHelper {

    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    private static final Path configPath = Paths.get(MOD_PATH + File.separator + "config.json");

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().enableComplexMapKeySerialization().registerTypeHierarchyAdapter(Block.class, new BlockAdapter()).create();

    private static JsonObject root = null;

    public static void init() {
        try {
            if (configPath.toFile().exists() && configPath.toFile().isFile()) {
                root = JsonParser.parseReader(Files.newBufferedReader(configPath)).getAsJsonObject();
            } else {
                String standardJson = """
                        {
                          "Seed": null,
                          "SavedSeeds": {},
                          "IgnoredBlocks": [],
                          "AutoOverlay": false,
                          "BlockColours": {}
                        }""";
                root = JsonParser.parseString(standardJson).getAsJsonObject();
            }
        } catch (Exception e) {
            LOGGER.error("Could not load config file. Your client may crash due to this.");
        }
    }

    static void save() {
        try (final BufferedWriter writer = Files.newBufferedWriter(configPath)) {
            Configs.getConfigs().forEach(config -> {
                Object value = Configs.get(config);
                root.add(config, gson.toJsonTree(value));
            });
            writer.write(gson.toJson(root));
        } catch (IOException e) {
            LOGGER.error("Could not save config file. Your client may crash due to this.");
        }
    }

    static Long initSeed() {
        JsonElement seed = root.get("Seed");
        if (seed == null) {
            root.add("Seed", JsonNull.INSTANCE);
            return null;
        }
        if (seed instanceof JsonNull) {
            return null;
        }
        return seed.getAsLong();
    }

    static Map<String, Long> initSavedSeeds() {
        if (root.has("SavedSeeds")) {
            return gson.fromJson(root.getAsJsonObject("SavedSeeds"), new TypeToken<Map<String, Long>>() {}.getType());
        }
        root.add("SavedSeeds", new JsonObject());
        return new HashMap<>();
    }

    static List<Block> initIgnoredBlocks() {
        if (root.has("IgnoredBlocks")) {
            return gson.fromJson(root.getAsJsonArray("IgnoredBlocks"), new TypeToken<List<Block>>() {}.getType());
        }
        root.add("IgnoredBlocks", new JsonArray());
        return new ArrayList<>();
    }

    static boolean initAutoOverlay() {
        if (root.has("AutoOverlay")) {
            return root.get("AutoOverlay").getAsBoolean();
        }
        root.addProperty("AutoOverlay", false);
        return false;
    }

    static Map<Block, Integer> initBlockColours() {
        if (root.has("BlockColours")) {
            return gson.fromJson(root.getAsJsonObject("BlockColours"), new TypeToken<Map<Block, Integer>>() {}.getType());
        }
        root.add("BlockColours", new JsonObject());
        return new HashMap<>();
    }
}
