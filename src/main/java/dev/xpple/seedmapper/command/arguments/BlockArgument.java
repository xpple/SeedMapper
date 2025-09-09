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
import dev.xpple.seedmapper.util.BlockColorConfig;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.level.material.MapColor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BlockArgument implements ArgumentType<Pair<Integer, Integer>> {

    private static final Collection<String> EXAMPLES = Arrays.asList("diamond_ore", "gold_ore", "nether_quartz_ore");

    // 只存储方块名称和对应的Cubiomes ID，颜色从配置中获取
    public static final Map<String, Integer> BLOCK_IDS = ImmutableMap.<String, Integer>builder()
        .put("ancient_debris", Cubiomes.ANCIENT_DEBRIS())
        .put("andesite", Cubiomes.ANDESITE())
        .put("basalt", Cubiomes.BASALT())
        .put("blackstone", Cubiomes.BLACKSTONE())
        .put("clay", Cubiomes.CLAY())
        .put("coal_ore", Cubiomes.COAL_ORE())
        .put("copper_ore", Cubiomes.COPPER_ORE())
        .put("deepslate", Cubiomes.DEEPSLATE())
        .put("diamond_ore", Cubiomes.DIAMOND_ORE())
        .put("diorite", Cubiomes.DIORITE())
        .put("dirt", Cubiomes.DIRT())
        .put("emerald_ore", Cubiomes.EMERALD_ORE())
        .put("gold_ore", Cubiomes.GOLD_ORE())
        .put("granite", Cubiomes.GRANITE())
        .put("gravel", Cubiomes.GRAVEL())
        .put("iron_ore", Cubiomes.IRON_ORE())
        .put("lapis_ore", Cubiomes.LAPIS_ORE())
        .put("magma_block", Cubiomes.MAGMA_BLOCK())
        .put("netherrack", Cubiomes.NETHERRACK())
        .put("nether_gold_ore", Cubiomes.NETHER_GOLD_ORE())
        .put("nether_quartz_ore", Cubiomes.NETHER_QUARTZ_ORE())
        .put("raw_copper_block", Cubiomes.RAW_COPPER_BLOCK())
        .put("raw_iron_block", Cubiomes.RAW_IRON_BLOCK())
        .put("redstone_ore", Cubiomes.REDSTONE_ORE())
        .put("soul_sand", Cubiomes.SOUL_SAND())
        .put("stone", Cubiomes.STONE())
        .put("tuff", Cubiomes.TUFF())
        .build();

    // 默认颜色映射，用于重置
    public static final Map<String, Integer> DEFAULT_COLORS = ImmutableMap.<String, Integer>builder()
        .put("ancient_debris", MapColor.TERRACOTTA_BROWN.col)
        .put("andesite", MapColor.STONE.col)
        .put("basalt", MapColor.COLOR_BLACK.col)
        .put("blackstone", MapColor.COLOR_BLACK.col)
        .put("clay", MapColor.CLAY.col)
        .put("coal_ore", MapColor.COLOR_BLACK.col)
        .put("copper_ore", MapColor.COLOR_ORANGE.col)
        .put("deepslate", MapColor.DEEPSLATE.col)
        .put("diamond_ore", MapColor.DIAMOND.col)
        .put("diorite", MapColor.QUARTZ.col)
        .put("dirt", MapColor.DIRT.col)
        .put("emerald_ore", MapColor.EMERALD.col)
        .put("gold_ore", MapColor.GOLD.col)
        .put("granite", MapColor.DIRT.col)
        .put("gravel", MapColor.STONE.col)
        .put("iron_ore", MapColor.RAW_IRON.col)
        .put("lapis_ore", MapColor.LAPIS.col)
        .put("magma_block", MapColor.NETHER.col)
        .put("netherrack", MapColor.NETHER.col)
        .put("nether_gold_ore", MapColor.GOLD.col)
        .put("nether_quartz_ore", MapColor.QUARTZ.col)
        .put("raw_copper_block", MapColor.COLOR_YELLOW.col)
        .put("raw_iron_block", MapColor.COLOR_YELLOW.col)
        .put("redstone_ore", MapColor.FIRE.col)
        .put("soul_sand", MapColor.COLOR_BROWN.col)
        .put("stone", MapColor.STONE.col)
        .put("tuff", MapColor.COLOR_GRAY.col)
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
        
        // 检查方块是否存在
        if (!BLOCK_IDS.containsKey(blockString)) {
            reader.setCursor(cursor);
            throw CommandExceptions.UNKNOWN_BLOCK_EXCEPTION.create(blockString);
        }
        
        // 从配置获取颜色，如果没有则使用默认颜色
        int blockId = BLOCK_IDS.get(blockString);
        int color = BlockColorConfig.getColor(blockString, DEFAULT_COLORS.getOrDefault(blockString, MapColor.STONE.col));
        
        return Pair.of(blockId, color);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(BLOCK_IDS.keySet(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
