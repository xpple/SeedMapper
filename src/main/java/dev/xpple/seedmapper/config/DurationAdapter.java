package dev.xpple.seedmapper.config;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

public class DurationAdapter extends TypeAdapter<Duration> {
    @Override
    public void write(JsonWriter writer, Duration duration) throws IOException {
        writer.value(duration.toString());
    }

    @Override
    public Duration read(JsonReader reader) throws IOException {
        return Duration.parse(reader.nextString());
    }
}
