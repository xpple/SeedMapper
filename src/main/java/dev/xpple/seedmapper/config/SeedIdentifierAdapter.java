package dev.xpple.seedmapper.config;

import com.github.cubiomes.Cubiomes;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dev.xpple.seedmapper.util.BiomeSeedIdentifier;
import dev.xpple.seedmapper.util.SeedIdentifier;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.util.Map;
import java.util.stream.Collectors;

public class SeedIdentifierAdapter extends TypeAdapter<SeedIdentifier> {

    private static final Gson GSON = new Gson();

    @Override
    public void write(JsonWriter writer, SeedIdentifier seed) throws IOException {
        GSON.getAdapter(new TypeToken<SeedIdentifier>() {}).write(writer, seed);
    }

    @Override
    public @Nullable SeedIdentifier read(JsonReader reader) throws IOException {
        // for default `Seed` config value
        if (reader.peek() == JsonToken.NULL) {
            return null;
        }
        // old format with just a long
        if (reader.peek() == JsonToken.NUMBER) {
            return new SeedIdentifier(reader.nextLong());
        }
        JsonObject seedObject = JsonParser.parseReader(reader).getAsJsonObject();
        // old format with keys `seed`, `version` and `generatorFlags`
        if (seedObject.has("seed")) {
            return new SeedIdentifier(parseBiomeSeedIdentifier(seedObject));
        }
        // new format
        if (seedObject.has("biomeSeedIdentifier") && seedObject.has("customStructureSalts")) {
            JsonObject biomeSeedIdentifierObject = seedObject.getAsJsonObject("biomeSeedIdentifier");
            BiomeSeedIdentifier biomeSeedIdentifier = parseBiomeSeedIdentifier(biomeSeedIdentifierObject);
            JsonObject customStructureSaltsObject = seedObject.getAsJsonObject("customStructureSalts");
            Map<String, Integer> customStructureSalts = customStructureSaltsObject.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> e.getValue().getAsInt()));
            return new SeedIdentifier(biomeSeedIdentifier, customStructureSalts);
        }
        throw new IOException("Unknown Seed data format, please open a bug report (seed data: " + seedObject + ")");
    }

    private static BiomeSeedIdentifier parseBiomeSeedIdentifier(JsonObject biomeSeedIdentifierObject) throws IOException {
        long seed = biomeSeedIdentifierObject.get("seed").getAsLong();
        BiomeSeedIdentifier biomeSeedIdentifier = new BiomeSeedIdentifier(seed);
        if (biomeSeedIdentifierObject.has("version")) {
            JsonPrimitive versionPrimitive = biomeSeedIdentifierObject.getAsJsonPrimitive("version");
            if (versionPrimitive.isNumber()) {
                biomeSeedIdentifier = biomeSeedIdentifier.withVersion(versionPrimitive.getAsInt());
            } else if (versionPrimitive.isString()) {
                int version;
                try (Arena arena = Arena.ofConfined()) {
                    version = Cubiomes.str2mc(arena.allocateFrom(versionPrimitive.getAsString()));
                }
                biomeSeedIdentifier = biomeSeedIdentifier.withVersion(version);
            } else {
                throw new IOException("Unknown version data format, please open a bug report (seed data: " + versionPrimitive + ")");
            }
        }
        if (biomeSeedIdentifierObject.has("generatorFlags")) {
            biomeSeedIdentifier = biomeSeedIdentifier.withGeneratorFlag(biomeSeedIdentifierObject.get("generatorFlags").getAsInt());
        }
        return biomeSeedIdentifier;
    }
}
