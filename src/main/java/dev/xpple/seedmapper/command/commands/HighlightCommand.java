package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.seedfinding.mcbiome.biome.Biome;
import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mccore.block.Block;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.rand.seed.WorldSeed;
import com.seedfinding.mccore.util.data.SpiralIterator;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mcfeature.decorator.ore.OreDecorator;
import com.seedfinding.mcfeature.misc.SlimeChunk;
import com.seedfinding.mcterrain.TerrainGenerator;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.command.SharedHelpers;
import dev.xpple.seedmapper.util.chat.Chat;
import dev.xpple.seedmapper.util.features.Features;
import dev.xpple.seedmapper.util.render.RenderQueue;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.minecraft.command.CommandSource.suggestMatching;

public class HighlightCommand extends ClientCommand implements SharedHelpers.Exceptions {

    @Override
    protected void build(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        final String[] blocks = new String[]{"ancient_debris", "andesite", "blackstone",/* "clay",*/ "coal_ore", "copper_ore", "deepslate", "diamond_ore", "diorite", "dirt", "emerald_ore", "gold_ore", "granite",/* "gravel",*/ "iron_ore", "lapis_ore", "magma_block", "nether_gold_ore", "quartz_ore", "redstone_ore",/* "sand",*/ "soulsand", "tuff"};
        argumentBuilder
            .then(literal("block")
                .then(argument("block", word())
                    .suggests((context, builder) -> suggestMatching(blocks, builder))
                    .executes(ctx -> highlightBlock(CustomClientCommandSource.of(ctx.getSource()), getString(ctx, "block")))
                    .then(argument("range", integer(0))
                        .executes(ctx -> highlightBlock(CustomClientCommandSource.of(ctx.getSource()), getString(ctx, "block"), getInteger(ctx, "range"))))))
            .then(literal("feature")
                .then(literal("slimechunk")
                    .executes(ctx -> highlightSlimeChunk(CustomClientCommandSource.of(ctx.getSource())))
                    .then(argument("range", integer(0))
                        .executes(ctx -> highlightSlimeChunk(CustomClientCommandSource.of(ctx.getSource()), getInteger(ctx, "range"))))));
    }

    @Override
    protected String rootLiteral() {
        return "highlight";
    }

    private static int highlightBlock(CustomClientCommandSource source, String blockString) throws CommandSyntaxException {
        return highlightBlock(source, blockString, 160); // 10 chunks
    }

    private static int highlightBlock(CustomClientCommandSource source, String blockString, int range) throws CommandSyntaxException {
        SharedHelpers helpers = new SharedHelpers(source);

        final Set<OreDecorator<?, ?>> oreDecorators = Features.getOresForVersion(helpers.mcVersion).stream()
            .filter(oreDecorator -> oreDecorator.isValidDimension(helpers.dimension))
            .filter(oreDecorator -> oreDecorator.getDefaultOreBlock().getName().equals(blockString))
            .collect(Collectors.toSet());

        if (oreDecorators.isEmpty()) {
            throw BLOCK_NOT_FOUND_EXCEPTION.create(blockString);
        }

        BiomeSource biomeSource = BiomeSource.of(helpers.dimension, helpers.mcVersion, helpers.seed);
        TerrainGenerator terrainGenerator = TerrainGenerator.of(biomeSource);

        final Set<Box> boxes = new HashSet<>();
        BlockPos center = new BlockPos(source.getPosition());
        CPos centerChunk = new CPos(center.getX() >> 4, center.getZ() >> 4);
        SpiralIterator<CPos> spiralIterator = new SpiralIterator<>(centerChunk, new CPos(range, range), (x, y, z) -> new CPos(x, z));
        StreamSupport.stream(spiralIterator.spliterator(), false)
            .flatMap(cPos -> {
                Biome biome = biomeSource.getBiome((cPos.getX() << 4) + 8, 0, (cPos.getZ() << 4) + 8);

                final Map<BPos, Block> generatedOres = new HashMap<>();
                Features.getOresForVersion(helpers.mcVersion).stream()
                    .filter(oreDecorator -> oreDecorator.isValidDimension(helpers.dimension))
                    .sorted(Comparator.comparingInt(oreDecorator -> oreDecorator.getSalt(biome)))
                    .forEachOrdered(oreDecorator -> {
                        if (!oreDecorator.canSpawn(cPos.getX(), cPos.getZ(), biomeSource)) {
                            return;
                        }
                        oreDecorator.generate(WorldSeed.toStructureSeed(helpers.seed), cPos.getX(), cPos.getZ(), biome, new ChunkRand(), terrainGenerator).positions
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
            .forEach(pos -> boxes.add(new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)));

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
            Chat.print(Text.translatable("command.highlight.block.noneFound", blockString));
        } else {
            Chat.print(Text.translatable("command.highlight.block.success", boxes.size(), blockString));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int highlightSlimeChunk(CustomClientCommandSource source) throws CommandSyntaxException {
        return highlightSlimeChunk(source, 160); // 10 chunks
    }

    private static int highlightSlimeChunk(CustomClientCommandSource source, int range) throws CommandSyntaxException {
        SharedHelpers helpers = new SharedHelpers(source);

        BlockPos center = new BlockPos(source.getPosition());
        CPos centerChunk = new CPos(center.getX() >> 4, center.getZ() >> 4);

        SlimeChunk slimeChunk = new SlimeChunk(helpers.mcVersion);
        if (!slimeChunk.isValidDimension(helpers.dimension)) {
            throw INVALID_DIMENSION_EXCEPTION.create();
        }

        ChunkRand rand = new ChunkRand();
        SpiralIterator<CPos> spiralIterator = new SpiralIterator<>(centerChunk, new CPos(range, range), (x, y, z) -> new CPos(x, z));
        StreamSupport.stream(spiralIterator.spliterator(), false)
            .filter(cPos -> {
                SlimeChunk.Data data = slimeChunk.at(cPos.getX(), cPos.getZ(), true);
                return data.testStart(helpers.seed, rand);
            })
            .limit(500)
            .forEach(cPos -> {
                BPos bPos = cPos.toBlockPos((int) source.getPosition().y);
                Box box = new Box(bPos.getX(), bPos.getY(), bPos.getZ(), bPos.getX() + 16, bPos.getY(), bPos.getZ() + 16);
                RenderQueue.addCuboid(RenderQueue.Layer.ON_TOP, box, box, 0x00FF00, -1);
            });
        return Command.SINGLE_SUCCESS;
    }
}
