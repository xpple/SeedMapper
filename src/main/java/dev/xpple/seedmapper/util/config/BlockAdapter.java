package dev.xpple.seedmapper.util.config;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.io.IOException;

public class BlockAdapter extends TypeAdapter<Block> {
    @Override
    public void write(JsonWriter writer, Block block) throws IOException {
        writer.value(BuiltInRegistries.BLOCK.getKey(block).getPath());
    }

    @Override
    public Block read(JsonReader reader) throws IOException {
        return BuiltInRegistries.BLOCK.getValue(ResourceLocation.parse(reader.nextString()));
    }
}
