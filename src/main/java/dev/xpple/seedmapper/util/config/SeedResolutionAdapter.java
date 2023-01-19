package dev.xpple.seedmapper.util.config;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class SeedResolutionAdapter extends TypeAdapter<SeedResolution> {
    @Override
    public void write(JsonWriter writer, SeedResolution resolution) throws IOException {
        writer.beginArray();
        for (SeedResolution.Method method : resolution) {
            writer.value(method.asString());
        }
        writer.endArray();
    }

    @Override
    public SeedResolution read(JsonReader reader) throws IOException {
        List<SeedResolution.Method> methods = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            methods.add(SeedResolution.Method.CODEC.byId(reader.nextString()));
        }
        reader.endArray();
        return new SeedResolution(methods);
    }
}
