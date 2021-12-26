package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.Command;
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
import com.seedfinding.mcterrain.TerrainGenerator;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.command.SharedHelpers;
import dev.xpple.seedmapper.util.chat.Chat;
import dev.xpple.seedmapper.util.maps.SimpleOreMap;
import dev.xpple.seedmapper.util.render.RenderQueue;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static dev.xpple.seedmapper.SeedMapper.CLIENT;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;
import static net.minecraft.command.CommandSource.suggestMatching;

public class HighlightCommand extends ClientCommand implements SharedHelpers.Exceptions {

    @Override
    protected void build() {
        final String[] blocks = new String[]{"ancient_debris", "andesite", "blackstone",/* "clay",*/ "coal_ore", "copper_ore", "deepslate", "diamond_ore", "diorite", "dirt", "emerald_ore", "gold_ore", "granite",/* "gravel",*/ "iron_ore", "lapis_ore", "magma_block", "nether_gold_ore", "quartz_ore", "redstone_ore",/* "sand",*/ "soulsand", "tuff"};
        argumentBuilder
                .then(literal("block")
                        .then(argument("block", word())
                                .suggests((context, builder) -> suggestMatching(blocks, builder))
                                .executes(ctx -> highlightBlock(CustomClientCommandSource.of(ctx.getSource()), getString(ctx, "block")))
                                .then(argument("range", integer(0))
                                        .executes(ctx -> highlightBlock(CustomClientCommandSource.of(ctx.getSource()), getString(ctx, "block"), getInteger(ctx, "range"))))));
    }

    @Override
    protected String rootLiteral() {
        return "highlight";
    }

    private static int highlightBlock(CustomClientCommandSource source, String blockString) throws CommandSyntaxException {
        return highlightBlock(source, blockString, 160); // 10 chunks
    }

    private static int highlightBlock(CustomClientCommandSource source, String blockString, int range) throws CommandSyntaxException {
        long seed = SharedHelpers.getSeed();
        String dimensionPath;
        if (source.getMeta("dimension") == null) {
            dimensionPath = source.getWorld().getRegistryKey().getValue().getPath();
        } else {
            dimensionPath = ((Identifier) source.getMeta("dimension")).getPath();
        }
        Dimension dimension = SharedHelpers.getDimension(dimensionPath);
        MCVersion mcVersion;
        if (source.getMeta("version") == null) {
            mcVersion = SharedHelpers.getMCVersion(CLIENT.getGame().getVersion().getName());
        } else {
            mcVersion = (MCVersion) source.getMeta("version");
        }

        final Set<OreDecorator<?, ?>> oreDecorators = SimpleOreMap.getForVersion(mcVersion).values().stream()
                .filter(oreDecorator -> oreDecorator.isValidDimension(dimension))
                .filter(oreDecorator -> oreDecorator.getDefaultOreBlock().getName().equals(blockString))
                .collect(Collectors.toSet());

        if (oreDecorators.isEmpty()) {
            throw BLOCK_NOT_FOUND_EXCEPTION.create(blockString);
        }

        BiomeSource biomeSource = BiomeSource.of(dimension, mcVersion, seed);
        TerrainGenerator terrainGenerator = TerrainGenerator.of(biomeSource);

        final Set<Box> boxes = new HashSet<>();
        BlockPos center = new BlockPos(source.getPosition());
        CPos centerChunk = new CPos(center.getX() >> 4, center.getZ() >> 4);
        SpiralIterator<CPos> spiralIterator = new SpiralIterator<>(centerChunk, new CPos(range, range), (x, y, z) -> new CPos(x, z));
        StreamSupport.stream(spiralIterator.spliterator(), false)
                .map(cPos -> {
                    Biome biome = biomeSource.getBiome((cPos.getX() << 4) + 8, 0, (cPos.getZ() << 4) + 8);

                    final Map<BPos, Block> generatedOres = new HashMap<>();
                    SimpleOreMap.getForVersion(mcVersion).values().stream()
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
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toSet());
                })
                .limit(50)
                .forEach(set -> set.forEach(pos -> boxes.add(new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1))));

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
            Chat.print("", new TranslatableText("command.highlight.block.noneFound", blockString));
        } else {
            Chat.print("", new TranslatableText("command.highlight.block.success", boxes.size(), blockString));
        }
        return Command.SINGLE_SUCCESS;
    }
}
