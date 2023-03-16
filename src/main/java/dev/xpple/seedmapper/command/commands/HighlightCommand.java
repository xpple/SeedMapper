package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.datafixers.util.Either;
import com.seedfinding.mcbiome.biome.Biome;
import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mccore.block.Block;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.rand.seed.WorldSeed;
import com.seedfinding.mccore.util.data.SpiralIterator;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mcfeature.misc.SlimeChunk;
import com.seedfinding.mcterrain.TerrainGenerator;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.command.SharedHelpers;
import dev.xpple.seedmapper.command.arguments.ClientBlockPredicateArgumentType;
import dev.xpple.seedmapper.simulation.SimulatedServer;
import dev.xpple.seedmapper.simulation.SimulatedWorld;
import dev.xpple.seedmapper.util.chat.Chat;
import dev.xpple.seedmapper.util.config.Configs;
import dev.xpple.seedmapper.util.features.Features;
import dev.xpple.seedmapper.util.render.RenderQueue;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.SharedConstants;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import net.minecraft.world.chunk.Chunk;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static dev.xpple.seedmapper.command.arguments.ClientBlockPredicateArgumentType.blockPredicate;
import static dev.xpple.seedmapper.command.arguments.ClientBlockPredicateArgumentType.getParseResult;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class HighlightCommand extends ClientCommand implements SharedHelpers.Exceptions {

    private static final SimpleCommandExceptionType COULD_NOT_HIGHLIGHT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("command.highlight.couldNotHighlight"));

    @Override
    protected LiteralCommandNode<FabricClientCommandSource> build(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        return dispatcher.register(literal(this.getRootLiteral())
            .then(literal("block")
                .then(argument("block", blockPredicate(registryAccess))
                    .executes(ctx -> highlightBlock(CustomClientCommandSource.of(ctx.getSource()), getParseResult(ctx, "block")))
                    .then(argument("radius", integer(0))
                        .executes(ctx -> highlightBlock(CustomClientCommandSource.of(ctx.getSource()), getParseResult(ctx, "block"), getInteger(ctx, "radius"))))))
            .then(literal("feature")
                .then(literal("slimechunk")
                    .executes(ctx -> highlightSlimeChunk(CustomClientCommandSource.of(ctx.getSource())))
                    .then(argument("radius", integer(0))
                        .executes(ctx -> highlightSlimeChunk(CustomClientCommandSource.of(ctx.getSource()), getInteger(ctx, "radius")))))));
    }

    @Override
    protected String rootLiteral() {
        return "highlight";
    }

    private static int highlightBlock(CustomClientCommandSource source, Either<BlockArgumentParser.BlockResult, BlockArgumentParser.TagResult> result) throws CommandSyntaxException {
        return highlightBlock(source, result, 3); // 3 chunks
    }

    private static int highlightBlock(CustomClientCommandSource source, Either<BlockArgumentParser.BlockResult, BlockArgumentParser.TagResult> result, int radius) throws CommandSyntaxException {
        final Set<Box> boxes;

        if (Configs.UseWorldSimulation) {
            ClientBlockPredicateArgumentType.ClientBlockPredicate predicate = ClientBlockPredicateArgumentType.getBlockPredicate(result);
            boxes = highlightBlockUsingWorldSimulation(predicate, radius, source);
        } else {
            boxes = highlightBlockUsingLibraries(result, radius, source);
        }

        boxes.forEach(box -> RenderQueue.addCuboid(RenderQueue.Layer.ON_TOP, box, box, null, -1));

        if (boxes.isEmpty()) {
            Chat.print(Text.translatable("command.highlight.block.noneFound"));
        } else {
            Chat.print(Text.translatable("command.highlight.block.success", boxes.size()));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static Set<Box> highlightBlockUsingWorldSimulation(ClientBlockPredicateArgumentType.ClientBlockPredicate predicate, int radius, CustomClientCommandSource source) throws CommandSyntaxException {
        SharedHelpers helpers = new SharedHelpers(source);

        if (!helpers.mcVersion().name.equals(SharedConstants.getGameVersion().getName())) {
            throw UNSUPPORTED_VERSION_EXCEPTION.create();
        }

        try (SimulatedServer server = SimulatedServer.newServer(helpers.seed())) {
            SimulatedWorld world = new SimulatedWorld(server, helpers.dimension());
            Vec3d position = source.getPosition();
            CPos center = new BPos(MathHelper.floor(position.getX()), MathHelper.floor(position.getY()), MathHelper.floor(position.getZ())).toChunkPos();

            final Set<Box> boxes = new HashSet<>();

            SpiralIterator<CPos> spiralIterator = new SpiralIterator<>(center, new CPos(radius, radius), CPos.Builder::create);
            StreamSupport.stream(spiralIterator.spliterator(), false).forEach(cPos -> {
                Chunk chunk = world.getChunk(cPos.getX(), cPos.getZ());

                final int startX, endX, startY, endY, startZ, endZ;
                startX = ChunkSectionPos.getBlockCoord(cPos.getX());
                endX = ChunkSectionPos.getOffsetPos(cPos.getX(), 15);
                startY = chunk.getBottomY();
                endY = chunk.getHighestNonEmptySectionYOffset();
                startZ = ChunkSectionPos.getBlockCoord(cPos.getZ());
                endZ = ChunkSectionPos.getOffsetPos(cPos.getZ(), 15);

                BlockPos.Mutable mutable = new BlockPos.Mutable();
                for (int x = startX; x <= endX; x++) {
                    mutable.setX(x);
                    for (int y = startY; y <= endY; y++) {
                        mutable.setY(y);
                        for (int z = startZ; z <= endZ; z++) {
                            mutable.setZ(z);
                            if (predicate.test(chunk, mutable)) {
                                boxes.add(new Box(x, y, z, x + 1, y + 1, z + 1));
                            }
                        }
                    }
                }
            });

            return boxes;
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }

    private static Set<Box> highlightBlockUsingLibraries(Either<BlockArgumentParser.BlockResult, BlockArgumentParser.TagResult> result, int radius, CustomClientCommandSource source) throws CommandSyntaxException {
        SharedHelpers helpers = new SharedHelpers(source);

        Set<String> blocks = result.map(
            blockResult -> Collections.singleton(Registries.BLOCK.getId(blockResult.blockState().getBlock()).getPath()),
            tagResult -> StreamSupport.stream(net.minecraft.block.Block.STATE_IDS.spliterator(), false)
                .filter(state -> state.isIn(tagResult.tag()))
                .map(state -> Registries.BLOCK.getId(state.getBlock()).getPath())
                .collect(Collectors.toSet())
        );

        boolean noneMatch = Features.getOresForVersion(helpers.mcVersion()).stream()
            .filter(oreDecorator -> oreDecorator.isValidDimension(helpers.dimension()))
            .noneMatch(oreDecorator -> blocks.contains(oreDecorator.getDefaultOreBlock().getName()));

        if (noneMatch) {
            throw COULD_NOT_HIGHLIGHT_EXCEPTION.create();
        }

        BiomeSource biomeSource = BiomeSource.of(helpers.dimension(), helpers.mcVersion(), helpers.seed());
        TerrainGenerator terrainGenerator = TerrainGenerator.of(biomeSource);

        final Set<Box> boxes = new HashSet<>();
        BlockPos center = BlockPos.ofFloored(source.getPosition());
        CPos centerChunk = new CPos(center.getX() >> 4, center.getZ() >> 4);
        SpiralIterator<CPos> spiralIterator = new SpiralIterator<>(centerChunk, new CPos(radius, radius), CPos.Builder::create);
        StreamSupport.stream(spiralIterator.spliterator(), false)
            .flatMap(cPos -> {
                Biome biome = biomeSource.getBiome((cPos.getX() << 4) + 8, 0, (cPos.getZ() << 4) + 8);

                final Map<BPos, Block> generatedOres = new HashMap<>();
                Features.getOresForVersion(helpers.mcVersion()).stream()
                    .filter(oreDecorator -> oreDecorator.isValidDimension(helpers.dimension()))
                    .sorted(Comparator.comparingInt(oreDecorator -> oreDecorator.getSalt(biome)))
                    .forEachOrdered(oreDecorator -> {
                        if (!oreDecorator.canSpawn(cPos.getX(), cPos.getZ(), biomeSource)) {
                            return;
                        }
                        oreDecorator.generate(WorldSeed.toStructureSeed(helpers.seed()), cPos.getX(), cPos.getZ(), biome, new ChunkRand(), terrainGenerator).positions
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
                    .filter(entry -> blocks.contains(entry.getValue().getName()))
                    .filter(entry -> entry.getKey().getY() > 0)
                    .map(Map.Entry::getKey);
            })
            .limit(500)
            .forEach(pos -> boxes.add(new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)));
        return boxes;
    }

    private static int highlightSlimeChunk(CustomClientCommandSource source) throws CommandSyntaxException {
        return highlightSlimeChunk(source, 10); // 10 chunks
    }

    private static int highlightSlimeChunk(CustomClientCommandSource source, int range) throws CommandSyntaxException {
        SharedHelpers helpers = new SharedHelpers(source);

        BlockPos center = BlockPos.ofFloored(source.getPosition());
        CPos centerChunk = new CPos(center.getX() >> 4, center.getZ() >> 4);

        SlimeChunk slimeChunk = new SlimeChunk(helpers.mcVersion());
        if (!slimeChunk.isValidDimension(helpers.dimension())) {
            throw INVALID_DIMENSION_EXCEPTION.create();
        }

        ChunkRand rand = new ChunkRand();
        SpiralIterator<CPos> spiralIterator = new SpiralIterator<>(centerChunk, new CPos(range, range), CPos.Builder::create);
        StreamSupport.stream(spiralIterator.spliterator(), false)
            .filter(cPos -> {
                SlimeChunk.Data data = slimeChunk.at(cPos.getX(), cPos.getZ(), true);
                return data.testStart(helpers.seed(), rand);
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
