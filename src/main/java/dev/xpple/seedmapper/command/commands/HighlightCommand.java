package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.seedfinding.mcbiome.biome.Biome;
import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mccore.block.Block;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.rand.seed.WorldSeed;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.util.data.SpiralIterator;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.decorator.ore.OreDecorator;
import com.seedfinding.mcfeature.misc.SlimeChunk;
import com.seedfinding.mcterrain.TerrainGenerator;
import dev.xpple.seedmapper.command.CommandExceptions;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.util.features.Features;
import dev.xpple.seedmapper.util.render.RenderQueue;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;
import static net.minecraft.commands.SharedSuggestionProvider.*;

public class HighlightCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        final String[] blocks = new String[]{"ancient_debris", "andesite", "blackstone",/* "clay",*/ "coal_ore", "copper_ore", "deepslate", "diamond_ore", "diorite", "dirt", "emerald_ore", "gold_ore", "granite",/* "gravel",*/ "iron_ore", "lapis_ore", "magma_block", "nether_gold_ore", "quartz_ore", "redstone_ore",/* "sand",*/ "soulsand", "tuff"};
        dispatcher.register(literal("sm:highlight")
            .then(literal("block")
                .then(argument("block", word())
                    .suggests((context, builder) -> suggest(blocks, builder))
                    .executes(ctx -> highlightBlock(CustomClientCommandSource.of(ctx.getSource()), getString(ctx, "block")))
                    .then(argument("range", integer(0))
                        .executes(ctx -> highlightBlock(CustomClientCommandSource.of(ctx.getSource()), getString(ctx, "block"), getInteger(ctx, "range"))))))
            .then(literal("feature")
                .then(literal("slimechunk")
                    .executes(ctx -> highlightSlimeChunk(CustomClientCommandSource.of(ctx.getSource())))
                    .then(argument("range", integer(0))
                        .executes(ctx -> highlightSlimeChunk(CustomClientCommandSource.of(ctx.getSource()), getInteger(ctx, "range")))))));
    }

    private static int highlightBlock(CustomClientCommandSource source, String blockString) throws CommandSyntaxException {
        return highlightBlock(source, blockString, 160); // 10 chunks
    }

    private static int highlightBlock(CustomClientCommandSource source, String blockString, int range) throws CommandSyntaxException {
        long seed = source.getSeed().getSecond();
        Dimension dimension = source.getDimension();
        MCVersion version = source.getVersion();
        final Set<OreDecorator<?, ?>> oreDecorators = Features.getOresForVersion(version).stream()
            .filter(oreDecorator -> oreDecorator.isValidDimension(dimension))
            .filter(oreDecorator -> oreDecorator.getDefaultOreBlock().getName().equals(blockString))
            .collect(Collectors.toSet());

        if (oreDecorators.isEmpty()) {
            throw CommandExceptions.UNKNOWN_BLOCK_EXCEPTION.create(blockString);
        }

        BiomeSource biomeSource = BiomeSource.of(dimension, version, seed);
        TerrainGenerator terrainGenerator = TerrainGenerator.of(biomeSource);

        final Set<AABB> boxes = new HashSet<>();
        BlockPos center = BlockPos.containing(source.getPosition());
        CPos centerChunk = new CPos(center.getX() >> 4, center.getZ() >> 4);
        SpiralIterator<CPos> spiralIterator = new SpiralIterator<>(centerChunk, new CPos(range, range), (x, y, z) -> new CPos(x, z));
        StreamSupport.stream(spiralIterator.spliterator(), false)
            .flatMap(cPos -> {
                Biome biome = biomeSource.getBiome((cPos.getX() << 4) + 8, 0, (cPos.getZ() << 4) + 8);

                final Map<BPos, Block> generatedOres = new HashMap<>();
                Features.getOresForVersion(version).stream()
                    .filter(oreDecorator -> oreDecorator.isValidDimension(dimension))
                    .sorted(Comparator.comparingInt(oreDecorator -> oreDecorator.getSalt(biome)))
                    .forEachOrdered(oreDecorator -> {
                        if (!oreDecorator.canSpawn(cPos.getX(), cPos.getZ(), biomeSource)) {
                            return;
                        }
                        oreDecorator.generate(WorldSeed.toStructureSeed(seed), cPos.getX(), cPos.getZ(), biome, new ChunkRand(), terrainGenerator).positions
                            .forEach(bPos -> {
                                if (generatedOres.containsKey(bPos)) {
                                    if (!oreDecorator.getReplaceBlocks(biome).contains(generatedOres.get(bPos))) {
                                        return;
                                    }
                                }
                                generatedOres.put(bPos, oreDecorator.getOreBlock(biome));
                            });
                    });
                return generatedOres.entrySet().stream()
                    .filter(entry -> entry.getValue().getName().equals(blockString))
                    .filter(entry -> entry.getKey().getY() > 0)
                    .map(Map.Entry::getKey);
            })
            .limit(500)
            .forEach(pos -> boxes.add(new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)));

        int colour = switch (blockString) {
            case "diamond_ore" -> 0x00E1FF;
            case "redstone_ore" -> 0xE10000;
            case "iron_ore" -> 0xAFAFAF;
            case "coal_ore" -> 0x191919;
            case "lapis_ore" -> 0x3232E1;
            case "emerald_ore" -> 0x00E100;
            case "gold_ore" -> 0xE1E100;
            case "copper_ore" -> 0xE17D4B;
            case "quartz_ore" -> 0xE1E1E1;
            case "ancient_debris" -> 0x964B19;
            default -> 0x00FF00;
        };
        boxes.forEach(box -> RenderQueue.addCuboid(RenderQueue.Layer.ON_TOP, box, box, colour, -1));

        if (boxes.isEmpty()) {
            source.sendFeedback(Component.translatable("command.highlight.block.noneFound", blockString));
        } else {
            source.sendFeedback(Component.translatable("command.highlight.block.success", boxes.size(), blockString));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int highlightSlimeChunk(CustomClientCommandSource source) throws CommandSyntaxException {
        return highlightSlimeChunk(source, 160); // 10 chunks
    }

    private static int highlightSlimeChunk(CustomClientCommandSource source, int range) throws CommandSyntaxException {
        long seed = source.getSeed().getSecond();
        Dimension dimension = source.getDimension();
        MCVersion version = source.getVersion();
        BlockPos center = BlockPos.containing(source.getPosition());
        CPos centerChunk = new CPos(center.getX() >> 4, center.getZ() >> 4);

        SlimeChunk slimeChunk = new SlimeChunk(version);
        if (!slimeChunk.isValidDimension(dimension)) {
            throw CommandExceptions.INVALID_DIMENSION_EXCEPTION.create();
        }

        ChunkRand rand = new ChunkRand();
        SpiralIterator<CPos> spiralIterator = new SpiralIterator<>(centerChunk, new CPos(range, range), (x, y, z) -> new CPos(x, z));
        StreamSupport.stream(spiralIterator.spliterator(), false)
            .filter(cPos -> {
                SlimeChunk.Data data = slimeChunk.at(cPos.getX(), cPos.getZ(), true);
                return data.testStart(seed, rand);
            })
            .limit(500)
            .forEach(cPos -> {
                BPos bPos = cPos.toBlockPos((int) source.getPosition().y);
                AABB box = new AABB(bPos.getX(), bPos.getY(), bPos.getZ(), bPos.getX() + 16, bPos.getY(), bPos.getZ() + 16);
                RenderQueue.addCuboid(RenderQueue.Layer.ON_TOP, box, box, 0x00FF00, -1);
            });
        return Command.SINGLE_SUCCESS;
    }
}
