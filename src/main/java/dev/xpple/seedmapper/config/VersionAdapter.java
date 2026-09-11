package dev.xpple.seedmapper.config;

import com.github.cubiomes.Cubiomes;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class VersionAdapter extends TypeAdapter<Integer> {
    @Override
    public void write(JsonWriter writer, Integer version) throws IOException {
        writer.value(Cubiomes.mc2str(version).getString(0));
    }

    @Override
    public Integer read(JsonReader reader) throws IOException {
         throw new AssertionError("Should be unused");
    }
}
