package dev.xpple.seedmapper.config;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dev.xpple.seedmapper.util.SeedIdentifier;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class SeedIdentifierAdapter extends TypeAdapter<SeedIdentifier> {

    private static final Gson GSON = new Gson();

    @Override
    public void write(JsonWriter writer, SeedIdentifier seed) throws IOException {
        GSON.getAdapter(new TypeToken<SeedIdentifier>() {}).write(writer, seed);
    }

    @Override
    public @Nullable SeedIdentifier read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NUMBER) {
            return new SeedIdentifier(reader.nextLong());
        }
        SeedIdentifier seedIdentifier = GSON.getAdapter(new TypeToken<SeedIdentifier>() {}).read(reader);
        if (seedIdentifier == null) { // is null for default `Seed` config value
            return null;
        }
        if (seedIdentifier.customStructureSalts() == null) {
            return new SeedIdentifier(seedIdentifier.seed(), seedIdentifier.version(), seedIdentifier.generatorFlags());
        }
        return seedIdentifier;
    }
}
