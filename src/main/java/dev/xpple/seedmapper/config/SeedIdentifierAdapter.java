package dev.xpple.seedmapper.config;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xpple.seedmapper.util.SeedIdentifier;

import java.io.IOException;

public class SeedIdentifierAdapter extends TypeAdapter<SeedIdentifier> {

    private static final Gson GSON = new Gson();

    @Override
    public void write(JsonWriter writer, SeedIdentifier seed) throws IOException {
        GSON.getAdapter(new TypeToken<SeedIdentifier>() {}).write(writer, seed);
    }

    @Override
    public SeedIdentifier read(JsonReader reader) throws IOException {
        return switch (reader.peek()) {
            case NUMBER -> new SeedIdentifier(reader.nextLong());
            default -> GSON.getAdapter(new TypeToken<SeedIdentifier>() {}).read(reader);
        };
    }
}
