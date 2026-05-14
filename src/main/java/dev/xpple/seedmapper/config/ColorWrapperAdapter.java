package dev.xpple.seedmapper.config;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class ColorWrapperAdapter extends TypeAdapter<ColorWrapper> {
    @Override
    public void write(JsonWriter writer, ColorWrapper colorWrapper) throws IOException {
        writer.value(colorWrapper.argb());
    }

    @Override
    public ColorWrapper read(JsonReader reader) throws IOException {
        return new ColorWrapper(reader.nextInt());
    }
}
