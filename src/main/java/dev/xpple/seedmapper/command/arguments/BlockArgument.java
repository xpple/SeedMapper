package dev.xpple.seedmapper.command.arguments;

import com.github.cubiomes.Cubiomes;
import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Pair;
import dev.xpple.seedmapper.command.CommandExceptions;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.level.material.MapColor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BlockArgument implements ArgumentType<Pair<Integer, Integer>> {

    private static final Collection<String> EXAMPLES = Arrays.asList("diamond_ore", "gold_ore", "nether_quartz_ore");

    public static final Map<String, Pair<Integer, Integer>> BLOCKS = ImmutableMap.<String, Pair<Integer, Integer>>builder()
        .put("ancient_debris", Pair.of(Cubiomes.ANCIENT_DEBRIS(), MapColor.TERRACOTTA_BROWN.col))
        .put("andesite", Pair.of(Cubiomes.ANDESITE(), MapColor.STONE.col))
        .put("basalt", Pair.of(Cubiomes.BASALT(), MapColor.COLOR_BLACK.col))
        .put("blackstone", Pair.of(Cubiomes.BLACKSTONE(), MapColor.COLOR_BLACK.col))
        .put("clay", Pair.of(Cubiomes.CLAY(), MapColor.CLAY.col))
        .put("coal_ore", Pair.of(Cubiomes.COAL_ORE(), MapColor.COLOR_BLACK.col))
        .put("copper_ore", Pair.of(Cubiomes.COPPER_ORE(), MapColor.COLOR_ORANGE.col))
        .put("deepslate", Pair.of(Cubiomes.DEEPSLATE(), MapColor.DEEPSLATE.col))
        .put("diamond_ore", Pair.of(Cubiomes.DIAMOND_ORE(), MapColor.DIAMOND.col))
        .put("diorite", Pair.of(Cubiomes.DIORITE(), MapColor.QUARTZ.col))
        .put("dirt", Pair.of(Cubiomes.DIRT(), MapColor.DIRT.col))
        .put("emerald_ore", Pair.of(Cubiomes.EMERALD_ORE(), MapColor.EMERALD.col))
        .put("gold_ore", Pair.of(Cubiomes.GOLD_ORE(), MapColor.GOLD.col))
        .put("granite", Pair.of(Cubiomes.GRANITE(), MapColor.DIRT.col))
        .put("gravel", Pair.of(Cubiomes.GRAVEL(), MapColor.STONE.col))
        .put("iron_ore", Pair.of(Cubiomes.IRON_ORE(), MapColor.RAW_IRON.col))
        .put("lapis_ore", Pair.of(Cubiomes.LAPIS_ORE(), MapColor.LAPIS.col))
        .put("magma_block", Pair.of(Cubiomes.MAGMA_BLOCK(), MapColor.NETHER.col))
        .put("netherrack", Pair.of(Cubiomes.NETHERRACK(), MapColor.NETHER.col))
        .put("nether_gold_ore", Pair.of(Cubiomes.NETHER_GOLD_ORE(), MapColor.GOLD.col))
        .put("nether_quartz_ore", Pair.of(Cubiomes.NETHER_QUARTZ_ORE(), MapColor.QUARTZ.col))
        .put("redstone_ore", Pair.of(Cubiomes.REDSTONE_ORE(), MapColor.FIRE.col))
        .put("soul_sand", Pair.of(Cubiomes.SOUL_SAND(), MapColor.COLOR_BROWN.col))
        .put("stone", Pair.of(Cubiomes.STONE(), MapColor.STONE.col))
        .put("tuff", Pair.of(Cubiomes.TUFF(), MapColor.COLOR_GRAY.col))
        .build();

    public static BlockArgument block() {
        return new BlockArgument();
    }

    @SuppressWarnings("unchecked")
    public static Pair<Integer, Integer> getBlock(CommandContext<FabricClientCommandSource> context, String name) {
        return (Pair<Integer, Integer>) context.getArgument(name, Pair.class);
    }

    @Override
    public Pair<Integer, Integer> parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String blockString = reader.readUnquotedString();
        Pair<Integer, Integer> blockPair = BLOCKS.get(blockString);
        if (blockPair == null) {
            reader.setCursor(cursor);
            throw CommandExceptions.UNKNOWN_BLOCK_EXCEPTION.create(blockString);
        }
        return blockPair;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(BLOCKS.keySet(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
