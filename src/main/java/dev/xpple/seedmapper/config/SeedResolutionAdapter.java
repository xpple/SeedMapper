package dev.xpple.seedmapper.config;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xpple.seedmapper.command.arguments.SeedResolutionArgument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SeedResolutionAdapter extends TypeAdapter<SeedResolutionArgument.SeedResolution> {
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
            methods.add(SeedResolutionArgument.SeedResolution.Method.CODEC.byName(reader.nextString()));
        }
        reader.endArray();
        return new SeedResolutionArgument.SeedResolution(methods);
    }
}
