package dev.xpple.seedmapper.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.seedfinding.mcbiome.biome.Biome;
import com.seedfinding.mcbiome.biome.Biomes;
import dev.xpple.seedmapper.command.SharedHelpers;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.command.CommandSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class BiomeArgumentType implements ArgumentType<Biome>, SharedHelpers.Exceptions {

    private static final Collection<String> EXAMPLES = Arrays.asList("desert", "crimson_forest", "giant_spruce_taiga_hills");

    public static BiomeArgumentType biome() {
        return new BiomeArgumentType();
    }

    public static Biome getBiome(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, Biome.class);
    }

    @Override
    public Biome parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String biomeString = reader.readUnquotedString();
        return Biomes.REGISTRY.values().stream()
                .filter(b -> b.getName().equals(biomeString))
                .findAny().orElseThrow(() -> {
                    reader.setCursor(cursor);
                    return BIOME_NOT_FOUND_EXCEPTION.create(biomeString);
                });
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(Biomes.REGISTRY.values().stream().map(Biome::getName), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
