package dev.xpple.seedmapper.config;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.xpple.seedmapper.seedmap.MapFeature;

import java.io.IOException;

public class MapFeatureAdapter extends TypeAdapter<MapFeature> {
    @Override
    public void write(JsonWriter writer, MapFeature feature) throws IOException {
        writer.value(feature.getName());
    }

    @Override
    public MapFeature read(JsonReader reader) throws IOException {
        return MapFeature.BY_NAME.get(reader.nextString());
    }
}
