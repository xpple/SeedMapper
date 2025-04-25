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
import net.minecraft.world.level.block.Blocks;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BlockArgument implements ArgumentType<Pair<Integer, Integer>> {

    private static final Collection<String> EXAMPLES = Arrays.asList("diamond_ore", "gold_ore", "quartz_ore");

    public static final Map<String, Pair<Integer, Integer>> BLOCKS = ImmutableMap.<String, Pair<Integer, Integer>>builder()
        .put("ancient_debris", Pair.of(Cubiomes.ANCIENT_DEBRIS(), Blocks.ANCIENT_DEBRIS.defaultMapColor().col))
        .put("andesite", Pair.of(Cubiomes.ANDESITE(), Blocks.ANDESITE.defaultMapColor().col))
        .put("basalt", Pair.of(Cubiomes.BASALT(), Blocks.BASALT.defaultMapColor().col))
        .put("blackstone", Pair.of(Cubiomes.BLACKSTONE(), Blocks.BLACKSTONE.defaultMapColor().col))
        .put("clay", Pair.of(Cubiomes.CLAY(), Blocks.CLAY.defaultMapColor().col))
        .put("coal_ore", Pair.of(Cubiomes.COAL_ORE(), Blocks.COAL_ORE.defaultMapColor().col))
        .put("copper_ore", Pair.of(Cubiomes.COPPER_ORE(), Blocks.COPPER_ORE.defaultMapColor().col))
        .put("deepslate", Pair.of(Cubiomes.DEEPSLATE(), Blocks.DEEPSLATE.defaultMapColor().col))
        .put("diamond_ore", Pair.of(Cubiomes.DIAMOND_ORE(), Blocks.DIAMOND_ORE.defaultMapColor().col))
        .put("diorite", Pair.of(Cubiomes.DIORITE(), Blocks.DIORITE.defaultMapColor().col))
        .put("dirt", Pair.of(Cubiomes.DIRT(), Blocks.DIRT.defaultMapColor().col))
        .put("emerald_ore", Pair.of(Cubiomes.EMERALD_ORE(), Blocks.EMERALD_ORE.defaultMapColor().col))
        .put("gold_ore", Pair.of(Cubiomes.GOLD_ORE(), Blocks.GOLD_ORE.defaultMapColor().col))
        .put("granite", Pair.of(Cubiomes.GRANITE(), Blocks.GRANITE.defaultMapColor().col))
        .put("gravel", Pair.of(Cubiomes.GRAVEL(), Blocks.GRAVEL.defaultMapColor().col))
        .put("iron_ore", Pair.of(Cubiomes.IRON_ORE(), Blocks.IRON_ORE.defaultMapColor().col))
        .put("lapis_ore", Pair.of(Cubiomes.LAPIS_ORE(), Blocks.LAPIS_ORE.defaultMapColor().col))
        .put("magma_block", Pair.of(Cubiomes.MAGMA_BLOCK(), Blocks.MAGMA_BLOCK.defaultMapColor().col))
        .put("netherrack", Pair.of(Cubiomes.NETHERRACK(), Blocks.NETHERRACK.defaultMapColor().col))
        .put("nether_gold_ore", Pair.of(Cubiomes.NETHER_GOLD_ORE(), Blocks.NETHER_GOLD_ORE.defaultMapColor().col))
        .put("nether_quartz_ore", Pair.of(Cubiomes.NETHER_QUARTZ_ORE(), Blocks.NETHER_QUARTZ_ORE.defaultMapColor().col))
        .put("redstone_ore", Pair.of(Cubiomes.REDSTONE_ORE(), Blocks.REDSTONE_ORE.defaultMapColor().col))
        .put("soul_sand", Pair.of(Cubiomes.SOUL_SAND(), Blocks.SOUL_SAND.defaultMapColor().col))
        .put("stone", Pair.of(Cubiomes.STONE(), Blocks.STONE.defaultMapColor().col))
        .put("tuff", Pair.of(Cubiomes.TUFF(), Blocks.TUFF.defaultMapColor().col))
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
