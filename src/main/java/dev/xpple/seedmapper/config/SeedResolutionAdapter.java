package dev.xpple.seedmapper.config;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xpple.seedmapper.command.arguments.SeedResolutionArgument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SeedResolutionAdapter extends TypeAdapter<SeedResolutionArgument.SeedResolution> {

    private static final Map<String, SeedResolutionArgument.SeedResolution.Method> FROM_OLD_KEYS = Map.of(
        "CommandSource", SeedResolutionArgument.SeedResolution.Method.COMMAND_SOURCE,
        "SavedSeedsConfig", SeedResolutionArgument.SeedResolution.Method.SAVED_SEEDS_CONFIG,
        "OnlineDatabase", SeedResolutionArgument.SeedResolution.Method.ONLINE_DATABASE,
        "SeedConfig", SeedResolutionArgument.SeedResolution.Method.SEED_CONFIG
    );

    @Override
    public void write(JsonWriter writer, SeedResolutionArgument.SeedResolution resolution) throws IOException {
        writer.beginArray();
        for (SeedResolutionArgument.SeedResolution.Method method : resolution) {
            writer.value(method.getSerializedName());
        }
        writer.endArray();
    }

    @Override
    public SeedResolutionArgument.SeedResolution read(JsonReader reader) throws IOException {
        List<SeedResolutionArgument.SeedResolution.Method> methods = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            String key = reader.nextString();
            SeedResolutionArgument.SeedResolution.Method method = FROM_OLD_KEYS.get(key);
            if (method != null) {
                methods.add(method);
                continue;
            }
            method = SeedResolutionArgument.SeedResolution.Method.CODEC.byName(key);
            if (method != null) {
                methods.add(method);
                continue;
            }
            throw new IOException("Unknown seed resolution method " + key);
        }
        reader.endArray();
        return new SeedResolutionArgument.SeedResolution(methods);
    }
}
