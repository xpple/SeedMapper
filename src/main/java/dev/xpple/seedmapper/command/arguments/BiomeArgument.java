package dev.xpple.seedmapper.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.seedfinding.mcbiome.biome.Biome;
import com.seedfinding.mcbiome.biome.Biomes;
import dev.xpple.seedmapper.command.CommandExceptions;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class BiomeArgument implements ArgumentType<Biome> {

    private static final Collection<String> EXAMPLES = Arrays.asList("desert", "crimson_forest", "giant_spruce_taiga_hills");

    public static BiomeArgument biome() {
        return new BiomeArgument();
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
                return CommandExceptions.UNKNOWN_BIOME_EXCEPTION.create(biomeString);
            });
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(Biomes.REGISTRY.values().stream().map(Biome::getName), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
