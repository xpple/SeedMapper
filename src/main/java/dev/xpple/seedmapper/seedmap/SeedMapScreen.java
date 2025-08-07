package dev.xpple.seedmapper.seedmap;

import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.Generator;
import com.github.cubiomes.OreVeinParameters;
import com.github.cubiomes.Pos;
import com.github.cubiomes.Range;
import com.github.cubiomes.StructureConfig;
import com.github.cubiomes.SurfaceNoise;
import com.mojang.blaze3d.platform.InputConstants;
import dev.xpple.seedmapper.SeedMapper;
import dev.xpple.seedmapper.command.commands.LocateCommand;
import dev.xpple.seedmapper.config.Configs;
import dev.xpple.seedmapper.feature.StructureChecks;
import dev.xpple.seedmapper.thread.SeedMapCache;
import dev.xpple.seedmapper.thread.SeedMapExecutor;
import dev.xpple.seedmapper.util.QuartPos2;
import dev.xpple.seedmapper.util.RegionPos;
import dev.xpple.seedmapper.util.TwoDTree;
import dev.xpple.seedmapper.util.WorldIdentifier;
import it.unimi.dsi.fastutil.ints.AbstractIntCollection;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.stream.IntStream;

import static dev.xpple.seedmapper.util.ChatBuilder.*;

public class SeedMapScreen extends Screen {
    /*
     * How the screen works (for my own sanity). The screen
     * is made up of tiles, similar to how Google Maps tiles
     * the world. Each tile is TilePos.TILE_SIZE_CHUNKS by
     * TilePos.TILE_SIZE_CHUNKS chunks in size. These tiles
     * are then filled with seed data when the screen is
     * opened, or when new chunks are loaded by dragging the
     * screen. This ensures that the tile textures are only
     * written to once, and can afterwards be quickly drawn.
     * The smallest unit visible in the seed map is a quart
     * pos (4 by 4 blocks) because biome calculations are
     * initially done at this scale.
     */

    // unsigned char biomeColors[256][3]
    private static final int[] biomeColours = new int[256];

    static {
        // unsigned char color[3]
        MemoryLayout rgbLayout = MemoryLayout.sequenceLayout(3, Cubiomes.C_CHAR);

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment biomeColoursInternal = arena.allocate(rgbLayout, biomeColours.length);
            Cubiomes.initBiomeColors(biomeColoursInternal);
            for (int biome = 0; biome < biomeColours.length; biome++) {
                MemorySegment colourArray = biomeColoursInternal.asSlice(biome * rgbLayout.byteSize());
                int red = colourArray.getAtIndex(Cubiomes.C_CHAR, 0) & 0xFF;
                int green = colourArray.getAtIndex(Cubiomes.C_CHAR, 1) & 0xFF;
                int blue = colourArray.getAtIndex(Cubiomes.C_CHAR, 2) & 0xFF;
                int colour = ARGB.color(red, green, blue);
                biomeColours[biome] = colour;
            }
        }
    }

    public static final int BIOME_SCALE = 4;
    public static final int SCALED_CHUNK_SIZE = LevelChunkSection.SECTION_WIDTH / BIOME_SCALE;

    private static final int HORIZONTAL_PADDING = 50;
    private static final int VERTICAL_PADDING = 50;

    public static final int MIN_PIXELS_PER_BIOME = 1;
    public static final int MAX_PIXELS_PER_BIOME = 100;

    private static final int HORIZONTAL_FEATURE_TOGGLE_SPACING = 5;
    private static final int VERTICAL_FEATURE_TOGGLE_SPACING = 1;
    private static final int FEATURE_TOGGLE_HEIGHT = 20;

    private static final int TELEPORT_FIELD_WIDTH = 70;

    private static final IntSupplier TILE_SIZE_PIXELS = () -> TilePos.TILE_SIZE_CHUNKS * SCALED_CHUNK_SIZE * Configs.PixelsPerBiome;

    private static final Object2ObjectMap<WorldIdentifier, Object2ObjectMap<TilePos, int[]>> biomeDataCache = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectMap<WorldIdentifier, Object2ObjectMap<ChunkPos, StructureData>> structureDataCache = new Object2ObjectOpenHashMap<>();
    public static final Object2ObjectMap<WorldIdentifier, TwoDTree> strongholdDataCache = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectMap<WorldIdentifier, Object2ObjectMap<TilePos, OreVeinData>> oreVeinDataCache = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectMap<WorldIdentifier, Object2ObjectMap<TilePos, BitSet>> slimeChunkDataCache = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectMap<WorldIdentifier, BlockPos> spawnDataCache = new Object2ObjectOpenHashMap<>();

    private final SeedMapExecutor seedMapExecutor = new SeedMapExecutor();

    private final Arena arena = Arena.ofShared();

    private final long seed;
    private final int dimension;
    private final int version;
    private final WorldIdentifier worldIdentifier;

    private final MemorySegment biomeGenerator; // thread safe
    private final MemorySegment structureGenerator; // NOT thread safe
    private final MemorySegment surfaceNoise;
    private final PositionalRandomFactory oreVeinRandom;
    private final MemorySegment oreVeinParameters;

    private final Object2ObjectMap<TilePos, Tile> biomeTileCache = new Object2ObjectOpenHashMap<>();
    private final SeedMapCache<TilePos, int[]> biomeCache;
    private final Object2ObjectMap<ChunkPos, StructureData> structureCache;
    private final SeedMapCache<TilePos, OreVeinData> oreVeinCache;
    private final Object2ObjectMap<TilePos, Tile> slimeChunkTileCache = new Object2ObjectOpenHashMap<>();
    private final SeedMapCache<TilePos, BitSet> slimeChunkCache;

    private final BlockPos playerPos;

    private QuartPos2 centerQuart;

    private int centerX;
    private int centerY;
    private int seedMapWidth;
    private int seedMapHeight;

    private final List<MapFeature> toggleableFeatures;
    private final int featureIconsCombinedWidth;

    private QuartPos2 mouseQuart;

    private int displayCoordinatesCopiedTicks = 0;

    private EditBox editBoxX;
    private EditBox editBoxZ;

    public SeedMapScreen(long seed, int dimension, int version, BlockPos playerPos) {
        super(Component.empty());
        this.seed = seed;
        this.dimension = dimension;
        this.version = version;
        this.worldIdentifier = new WorldIdentifier(this.seed, this.dimension, this.version);

        this.biomeGenerator = Generator.allocate(this.arena);
        Cubiomes.setupGenerator(this.biomeGenerator, this.version, 0);
        Cubiomes.applySeed(this.biomeGenerator, this.dimension, this.seed);

        this.structureGenerator = Generator.allocate(this.arena);
        this.structureGenerator.copyFrom(this.biomeGenerator);

        this.surfaceNoise = SurfaceNoise.allocate(this.arena);
        Cubiomes.initSurfaceNoise(this.surfaceNoise, this.dimension, this.seed);

        this.oreVeinRandom = new XoroshiroRandomSource(this.seed).forkPositional().fromHashOf(ResourceLocation.fromNamespaceAndPath(SeedMapper.MOD_ID, "ore_vein_feature")).forkPositional();
        this.oreVeinParameters = OreVeinParameters.allocate(this.arena);
        Cubiomes.initOreVeinNoise(this.oreVeinParameters, this.seed, this.version);

        this.toggleableFeatures = Arrays.stream(MapFeature.values())
            .filter(feature -> feature.getDimension() == this.dimension)
            .filter(feature -> this.version >= feature.availableSince())
            .sorted(Comparator.comparing(MapFeature::getName))
            .toList();

        this.biomeCache = new SeedMapCache<>(Object2ObjectMaps.synchronize(biomeDataCache.computeIfAbsent(this.worldIdentifier, _ -> new Object2ObjectOpenHashMap<>())), this.seedMapExecutor);
        this.structureCache = structureDataCache.computeIfAbsent(this.worldIdentifier, _ -> new Object2ObjectOpenHashMap<>());
        this.slimeChunkCache = new SeedMapCache<>(Object2ObjectMaps.synchronize(slimeChunkDataCache.computeIfAbsent(this.worldIdentifier, _ -> new Object2ObjectOpenHashMap<>())), this.seedMapExecutor);
        this.oreVeinCache = new SeedMapCache<>(oreVeinDataCache.computeIfAbsent(this.worldIdentifier, _ -> new Object2ObjectOpenHashMap<>()), this.seedMapExecutor);

        if (this.toggleableFeatures.contains(MapFeature.STRONGHOLD) && !strongholdDataCache.containsKey(this.worldIdentifier)) {
            this.seedMapExecutor.submitCalculation(() -> LocateCommand.calculateStrongholds(this.seed, this.dimension, this.version))
                .thenAccept(tree -> {
                    if (tree != null) {
                        strongholdDataCache.put(this.worldIdentifier, tree);
                    }
                });
        }

        this.featureIconsCombinedWidth = this.toggleableFeatures.stream()
            .map(feature -> feature.getTexture().width())
            .reduce((l, r) -> l + HORIZONTAL_FEATURE_TOGGLE_SPACING + r)
            .orElseThrow();

        this.playerPos = playerPos;

        this.centerQuart = QuartPos2.fromBlockPos(playerPos);
        this.mouseQuart = new QuartPos2(this.centerQuart.x(), this.centerQuart.z());
    }

    @Override
    protected void init() {
        super.init();

        this.centerX = this.width / 2;
        this.centerY = this.height / 2;

        this.seedMapWidth = 2 * (this.centerX - HORIZONTAL_PADDING);
        this.seedMapHeight = 2 * (this.centerY - VERTICAL_PADDING);

        this.createFeatureToggles();
        this.createTeleportField();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // draw seed + version
        Component seedComponent = Component.translatable("seedMap.seedAndVersion", accent(Long.toString(this.seed)), Cubiomes.mc2str(this.version).getString(0));
        guiGraphics.drawString(this.font, seedComponent, HORIZONTAL_PADDING, VERTICAL_PADDING - this.font.lineHeight - 1, -1);

        int tileSizePixels = TILE_SIZE_PIXELS.getAsInt();
        int horTileRadius = Math.ceilDiv(this.seedMapWidth, tileSizePixels) + 1;
        int verTileRadius = Math.ceilDiv(this.seedMapHeight, tileSizePixels) + 1;

        TilePos centerTile = TilePos.fromQuartPos(this.centerQuart);
        for (int relTileX = -horTileRadius; relTileX <= horTileRadius; relTileX++) {
            for (int relTileZ = -verTileRadius; relTileZ <= verTileRadius; relTileZ++) {
                TilePos tilePos = centerTile.add(relTileX, relTileZ);

                // compute biomes and store in texture
                int[] biomeData = this.biomeCache.computeIfAbsent(tilePos, this::calculateBiomeData);
                if (biomeData != null) {
                    Tile tile = this.biomeTileCache.computeIfAbsent(tilePos, _ -> this.createBiomeTile(tilePos, biomeData));
                    this.drawTile(guiGraphics, tile);
                }

                // compute slime chunks and store in texture
                if (this.toggleableFeatures.contains(MapFeature.SLIME_CHUNK) && Configs.ToggledFeatures.contains(MapFeature.SLIME_CHUNK)) {
                    BitSet slimeChunkData = this.slimeChunkCache.computeIfAbsent(tilePos, this::calculateSlimeChunkData);
                    if (slimeChunkData != null) {
                        Tile tile = this.slimeChunkTileCache.computeIfAbsent(tilePos, _ -> this.createSlimeChunkTile(tilePos, slimeChunkData));
                        this.drawTile(guiGraphics, tile);
                    }
                }
            }
        }

        int horChunkRadius = Math.ceilDiv(this.seedMapWidth / 2, SCALED_CHUNK_SIZE * Configs.PixelsPerBiome);
        int verChunkRadius = Math.ceilDiv(this.seedMapHeight / 2, SCALED_CHUNK_SIZE * Configs.PixelsPerBiome);

        // compute structures
        Configs.ToggledFeatures.stream()
            .filter(this.toggleableFeatures::contains)
            .filter(f -> f.getStructureId() != -1)
            .forEach(feature -> {
                int structure = feature.getStructureId();
                MemorySegment structureConfig = StructureConfig.allocate(this.arena);
                if (Cubiomes.getStructureConfig(structure, this.version, structureConfig) == 0) {
                    return;
                }
                if (StructureConfig.dim(structureConfig) != this.dimension) {
                    return;
                }
                int regionSize = StructureConfig.regionSize(structureConfig);
                RegionPos centerRegion = RegionPos.fromQuartPos(this.centerQuart, regionSize);
                int horRegionRadius = Math.ceilDiv(horChunkRadius, regionSize);
                int verRegionRadius = Math.ceilDiv(verChunkRadius, regionSize);
                StructureChecks.GenerationCheck generationCheck = StructureChecks.getGenerationCheck(structure);
                MemorySegment structurePos = Pos.allocate(this.arena);
                for (int relRegionX = -horRegionRadius; relRegionX <= horRegionRadius; relRegionX++) {
                    for (int relRegionZ = -verRegionRadius; relRegionZ <= verRegionRadius; relRegionZ++) {
                        RegionPos regionPos = centerRegion.add(relRegionX, relRegionZ);
                        if (Cubiomes.getStructurePos(structure, this.version, this.seed, regionPos.x(), regionPos.z(), structurePos) == 0) {
                            continue;
                        }
                        ChunkPos chunkPos = new ChunkPos(SectionPos.blockToSectionCoord(Pos.x(structurePos)), SectionPos.blockToSectionCoord(Pos.z(structurePos)));

                        StructureData structureData = this.structureCache.computeIfAbsent(chunkPos, _ -> new StructureData(chunkPos, new Int2ObjectArrayMap<>()));
                        BlockPos pos = structureData.structures().computeIfAbsent(structure, _ -> this.calculateStructurePos(regionPos, structurePos, generationCheck));
                        if (pos == null) {
                            continue;
                        }
                        this.drawMapFeature(guiGraphics, feature, pos);
                    }
                }
            });

        // draw strongholds
        if (this.toggleableFeatures.contains(MapFeature.STRONGHOLD) && Configs.ToggledFeatures.contains(MapFeature.STRONGHOLD)) {
            TwoDTree tree = strongholdDataCache.get(this.worldIdentifier);
            if (tree != null) {
                for (BlockPos strongholdPos : tree) {
                    this.drawMapFeature(guiGraphics, MapFeature.STRONGHOLD, strongholdPos);
                }
            }
        }

        // compute ore veins
        if ((this.toggleableFeatures.contains(MapFeature.COPPER_ORE_VEIN) || this.toggleableFeatures.contains(MapFeature.IRON_ORE_VEIN))
            && (Configs.ToggledFeatures.contains(MapFeature.COPPER_ORE_VEIN) || Configs.ToggledFeatures.contains(MapFeature.IRON_ORE_VEIN))) {
            for (int relTileX = -horTileRadius; relTileX <= horTileRadius; relTileX++) {
                for (int relTileZ = -verTileRadius; relTileZ <= verTileRadius; relTileZ++) {
                    TilePos tilePos = new TilePos(centerTile.x() + relTileX, centerTile.z() + relTileZ);
                    OreVeinData oreVeinData = this.oreVeinCache.computeIfAbsent(tilePos, _ -> this.calculateOreVein(tilePos));
                    if (oreVeinData == null) {
                        continue;
                    }
                    if (Configs.ToggledFeatures.contains(oreVeinData.oreVeinType())) {
                        this.drawMapFeature(guiGraphics, oreVeinData.oreVeinType(), oreVeinData.blockPos());
                    }
                }
            }
        }

        // draw player position
        int playerMinX = this.centerX + Configs.PixelsPerBiome * (QuartPos.fromBlock(this.playerPos.getX()) - this.centerQuart.x()) - 10;
        int playerMinY = this.centerY + Configs.PixelsPerBiome * (QuartPos.fromBlock(this.playerPos.getZ()) - this.centerQuart.z()) - 10;
        int playerMaxX = playerMinX + 20;
        int playerMaxY = playerMinY + 20;
        if (playerMinX >= HORIZONTAL_PADDING && playerMaxX <= HORIZONTAL_PADDING + this.seedMapWidth && playerMinY >= VERTICAL_PADDING && playerMaxY <= VERTICAL_PADDING + this.seedMapHeight) {
            PlayerFaceRenderer.draw(guiGraphics, this.minecraft.player.getSkin(), playerMinX, playerMinY, 20);
        }

        // calculate spawn point
        if (this.toggleableFeatures.contains(MapFeature.WORLD_SPAWN) && Configs.ToggledFeatures.contains(MapFeature.WORLD_SPAWN)) {
            BlockPos spawnPoint = spawnDataCache.computeIfAbsent(this.worldIdentifier, _ -> this.calculateSpawnData());
            this.drawMapFeature(guiGraphics, MapFeature.WORLD_SPAWN, spawnPoint);
        }

        // draw hovered coordinates
        Component coordinates = accent("x: %d, z: %d".formatted(QuartPos.toBlock(this.mouseQuart.x()), QuartPos.toBlock(this.mouseQuart.z())));
        if (this.displayCoordinatesCopiedTicks > 0) {
            coordinates = Component.translatable("seedMap.coordinatesCopied", coordinates);
        }
        guiGraphics.drawString(this.font, coordinates , HORIZONTAL_PADDING, VERTICAL_PADDING + this.seedMapHeight + 1, -1);
    }

    private void drawTile(GuiGraphics guiGraphics, Tile tile) {
        TilePos tilePos = tile.pos();
        QuartPos2 relQuartPos = QuartPos2.fromTilePos(tilePos).subtract(this.centerQuart);
        int tileSizePixels = TILE_SIZE_PIXELS.getAsInt();
        int minX = this.centerX + Configs.PixelsPerBiome * relQuartPos.x();
        int minY = this.centerY + Configs.PixelsPerBiome * relQuartPos.z();
        int maxX = minX + tileSizePixels;
        int maxY = minY + tileSizePixels;

        if (maxX < HORIZONTAL_PADDING || minX > HORIZONTAL_PADDING + this.seedMapWidth) {
            return;
        }
        if (maxY < VERTICAL_PADDING || minY > VERTICAL_PADDING + this.seedMapHeight) {
            return;
        }

        float u0, u1, v0, v1;
        if (minX < HORIZONTAL_PADDING) {
            u0 = (float) (HORIZONTAL_PADDING - minX) / tileSizePixels;
            minX = HORIZONTAL_PADDING;
        } else u0 = 0;
        if (maxX > HORIZONTAL_PADDING + this.seedMapWidth) {
            u1 = 1 - ((float) (maxX - HORIZONTAL_PADDING - this.seedMapWidth) / tileSizePixels);
            maxX = HORIZONTAL_PADDING + this.seedMapWidth;
        } else u1 = 1;
        if (minY < VERTICAL_PADDING) {
            v0 = (float) (VERTICAL_PADDING - minY) / tileSizePixels;
            minY = VERTICAL_PADDING;
        } else v0 = 0;
        if (maxY > VERTICAL_PADDING + this.seedMapHeight) {
            v1 = 1 - ((float) (maxY - VERTICAL_PADDING - this.seedMapHeight) / tileSizePixels);
            maxY = VERTICAL_PADDING + this.seedMapHeight;
        } else v1 = 1;

        guiGraphics.submitBlit(RenderPipelines.GUI_TEXTURED, tile.texture().getTextureView(), minX, minY, maxX, maxY, u0, u1, v0, v1, -1);
    }

    private Tile createBiomeTile(TilePos tilePos, int[] biomeData) {
        Tile tile = new Tile(tilePos, this.seed, this.dimension);
        DynamicTexture texture = tile.texture();
        int width = texture.getPixels().getWidth();
        int height = texture.getPixels().getHeight();
        for (int relX = 0; relX < width; relX++) {
            for (int relZ = 0; relZ < height; relZ++) {
                int biome = biomeData[relX + relZ * width];
                texture.getPixels().setPixel(relX, relZ, biomeColours[biome]);
            }
        }
        texture.upload();
        return tile;
    }

    private Tile createSlimeChunkTile(TilePos tilePos, BitSet slimeChunkData) {
        Tile tile = new Tile(tilePos, this.seed, this.dimension);
        DynamicTexture texture = tile.texture();
        for (int relChunkX = 0; relChunkX < TilePos.TILE_SIZE_CHUNKS; relChunkX++) {
            for (int relChunkZ = 0; relChunkZ < TilePos.TILE_SIZE_CHUNKS; relChunkZ++) {
                boolean isSlimeChunk = slimeChunkData.get(relChunkX + relChunkZ * TilePos.TILE_SIZE_CHUNKS);
                if (isSlimeChunk) {
                    texture.getPixels().fillRect(SCALED_CHUNK_SIZE * relChunkX, SCALED_CHUNK_SIZE * relChunkZ, SCALED_CHUNK_SIZE, SCALED_CHUNK_SIZE, 0xFF_00FF00);
                }
            }
        }
        texture.upload();
        return tile;
    }

    private void drawMapFeature(GuiGraphics guiGraphics, MapFeature feature, BlockPos pos) {
        MapFeature.Texture featureIcon = feature.getTexture();
        QuartPos2 relQuartPos = QuartPos2.fromBlockPos(pos).subtract(this.centerQuart);
        int minX = this.centerX + Configs.PixelsPerBiome * relQuartPos.x() - featureIcon.width() / 2;
        int minY = this.centerY + Configs.PixelsPerBiome * relQuartPos.z() - featureIcon.height() / 2;
        int maxX = minX + featureIcon.width();
        int maxY = minY + featureIcon.height();

        if (maxX >= HORIZONTAL_PADDING + this.seedMapWidth || maxY >= VERTICAL_PADDING + this.seedMapHeight) {
            return;
        }
        if (minX < HORIZONTAL_PADDING || minY < VERTICAL_PADDING) {
            return;
        }

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, featureIcon.resourceLocation(), minX, minY, 0, 0, featureIcon.width(), featureIcon.height(), featureIcon.width(), featureIcon.height());
    }

    private void createFeatureToggles() {
        // TODO: replace with Gatherers API?
        // TODO: only calculate on resize?
        int rows = Math.ceilDiv(this.featureIconsCombinedWidth, this.seedMapWidth);
        int togglesPerRow = Math.ceilDiv(this.toggleableFeatures.size(), rows);
        int toggleMinY = 1;
        for (int row = 0; row < rows - 1; row++) {
            this.createFeatureTogglesInner(row, togglesPerRow, togglesPerRow, HORIZONTAL_PADDING, toggleMinY);
            toggleMinY += FEATURE_TOGGLE_HEIGHT + VERTICAL_FEATURE_TOGGLE_SPACING;
        }
        int togglesInLastRow = this.toggleableFeatures.size() - togglesPerRow * (rows - 1);
        this.createFeatureTogglesInner(rows - 1, togglesPerRow, togglesInLastRow, HORIZONTAL_PADDING, toggleMinY);
    }

    private void createFeatureTogglesInner(int row, int togglesPerRow, int maxToggles, int toggleMinX, int toggleMinY) {
        for (int toggle = 0; toggle < maxToggles; toggle++) {
            MapFeature feature = this.toggleableFeatures.get(row * togglesPerRow + toggle);
            MapFeature.Texture featureIcon = feature.getTexture();
            this.addRenderableWidget(new FeatureToggleWidget(feature, toggleMinX, toggleMinY));
            toggleMinX += featureIcon.width() + HORIZONTAL_FEATURE_TOGGLE_SPACING;
        }
    }

    private int[] calculateBiomeData(TilePos tilePos) {
        QuartPos2 quartPos = QuartPos2.fromTilePos(tilePos);
        int rangeSize = TilePos.TILE_SIZE_CHUNKS * SCALED_CHUNK_SIZE;

        MemorySegment range = Range.allocate(this.arena);
        Range.scale(range, BIOME_SCALE);
        Range.x(range, quartPos.x());
        Range.z(range, quartPos.z());
        Range.sx(range, rangeSize);
        Range.sz(range, rangeSize);
        Range.y(range, 63 / Range.scale(range)); // sea level
        Range.sy(range, 1);

        long cacheSize = Cubiomes.getMinCacheSize(this.biomeGenerator, Range.scale(range), Range.sx(range), Range.sy(range), Range.sz(range));
        MemorySegment biomeIds = this.arena.allocate(Cubiomes.C_INT, cacheSize);
        if (Cubiomes.genBiomes(this.biomeGenerator, biomeIds, range) == 0) {
            return biomeIds.toArray(Cubiomes.C_INT);
        }

        throw new RuntimeException("Cubiomes.genBiomes() failed!");
    }

    private BitSet calculateSlimeChunkData(TilePos tilePos) {
        BitSet slimeChunks = new BitSet(TilePos.TILE_SIZE_CHUNKS * TilePos.TILE_SIZE_CHUNKS);
        ChunkPos chunkPos = tilePos.toChunkPos();
        for (int relChunkX = 0; relChunkX < TilePos.TILE_SIZE_CHUNKS; relChunkX++) {
            for (int relChunkZ = 0; relChunkZ < TilePos.TILE_SIZE_CHUNKS; relChunkZ++) {
                RandomSource random = WorldgenRandom.seedSlimeChunk(chunkPos.x + relChunkX, chunkPos.z + relChunkZ, this.seed, 987234911L);
                slimeChunks.set(relChunkX + relChunkZ * TilePos.TILE_SIZE_CHUNKS, random.nextInt(10) == 0);
            }
        }
        return slimeChunks;
    }

    private @Nullable BlockPos calculateStructurePos(RegionPos regionPos, MemorySegment structurePos, StructureChecks.GenerationCheck generationCheck) {
        if (!generationCheck.check(this.structureGenerator, this.surfaceNoise, regionPos.x(), regionPos.z(), structurePos)) {
            return null;
        }

        return new BlockPos(Pos.x(structurePos), 0, Pos.z(structurePos));
    }

    private @Nullable OreVeinData calculateOreVein(TilePos tilePos) {
        ChunkPos chunkPos = tilePos.toChunkPos();
        for (int relChunkX = 0; relChunkX < TilePos.TILE_SIZE_CHUNKS; relChunkX++) {
            for (int relChunkZ = 0; relChunkZ < TilePos.TILE_SIZE_CHUNKS; relChunkZ++) {
                int minBlockX = SectionPos.sectionToBlockCoord(chunkPos.x + relChunkZ);
                int minBlockZ = SectionPos.sectionToBlockCoord(chunkPos.z + relChunkZ);
                RandomSource rnd = this.oreVeinRandom.at(minBlockX, 0, minBlockZ);
                BlockPos pos = new BlockPos(minBlockX + rnd.nextInt(LevelChunkSection.SECTION_WIDTH), 0, minBlockZ + rnd.nextInt(LevelChunkSection.SECTION_WIDTH));
                IntSet blocks = IntStream.rangeClosed(0, (50 - -60) / 4)
                    .map(y -> 4 * y + -60)
                    .map(y -> Cubiomes.getOreVeinBlockAt(pos.getX(), y, pos.getZ(), this.oreVeinParameters))
                    .collect(IntArraySet::new, IntArraySet::add, AbstractIntCollection::addAll);
                if (blocks.contains(Cubiomes.RAW_COPPER_BLOCK())) {
                    return new OreVeinData(tilePos, MapFeature.COPPER_ORE_VEIN, pos);
                } else if (blocks.contains(Cubiomes.RAW_IRON_BLOCK())) {
                    return new OreVeinData(tilePos, MapFeature.IRON_ORE_VEIN, pos);
                } else if (blocks.contains(Cubiomes.COPPER_ORE())) {
                    return new OreVeinData(tilePos, MapFeature.COPPER_ORE_VEIN, pos);
                } else if (blocks.contains(Cubiomes.IRON_ORE())) {
                    return new OreVeinData(tilePos, MapFeature.IRON_ORE_VEIN, pos);
                } else if (blocks.contains(Cubiomes.GRANITE())) {
                    return new OreVeinData(tilePos, MapFeature.COPPER_ORE_VEIN, pos);
                } else if (blocks.contains(Cubiomes.TUFF())) {
                    return new OreVeinData(tilePos, MapFeature.IRON_ORE_VEIN, pos);
                }
            }
        }
        return null;
    }

    private BlockPos calculateSpawnData() {
        MemorySegment pos = Cubiomes.getSpawn(this.arena, this.biomeGenerator);
        return new BlockPos(Pos.x(pos), 0, Pos.z(pos));
    }

    private void createTeleportField() {
        this.editBoxX = new EditBox(this.font, this.width / 2 - TELEPORT_FIELD_WIDTH, VERTICAL_PADDING + this.seedMapHeight + 1, TELEPORT_FIELD_WIDTH, 20, Component.translatable("seedMap.editBoxX"));
        this.editBoxX.setMaxLength(9);
        this.addRenderableWidget(this.editBoxX);
        this.editBoxZ = new EditBox(this.font, this.width / 2, VERTICAL_PADDING + this.seedMapHeight + 1, TELEPORT_FIELD_WIDTH, 20, Component.translatable("seedMap.editBoxZ"));
        this.editBoxZ.setMaxLength(9);
        this.addRenderableWidget(this.editBoxZ);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.displayCoordinatesCopiedTicks > 0) {
            this.displayCoordinatesCopiedTicks--;
        }
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (mouseX < HORIZONTAL_PADDING || mouseX > HORIZONTAL_PADDING + this.seedMapWidth || mouseY < VERTICAL_PADDING || mouseY > VERTICAL_PADDING + this.seedMapHeight) {
            return;
        }

        int relXQuart = (int) ((mouseX - this.centerX) / Configs.PixelsPerBiome);
        int relZQuart = (int) ((mouseY - this.centerY) / Configs.PixelsPerBiome);

        this.mouseQuart = this.centerQuart.add(relXQuart, relZQuart);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }

        float currentScroll = Mth.clamp((float) Configs.PixelsPerBiome / MAX_PIXELS_PER_BIOME, 0.0F, 1.0F);
        currentScroll = Mth.clamp(currentScroll - (float) (-scrollY / MAX_PIXELS_PER_BIOME), 0.0F, 1.0F);

        Configs.PixelsPerBiome = Math.max((int) (currentScroll * MAX_PIXELS_PER_BIOME + 0.5), MIN_PIXELS_PER_BIOME);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button != InputConstants.MOUSE_BUTTON_LEFT) {
            return false;
        }
        if (mouseX < HORIZONTAL_PADDING || mouseX > HORIZONTAL_PADDING + this.seedMapWidth || mouseY < VERTICAL_PADDING || mouseY > VERTICAL_PADDING + this.seedMapHeight) {
            return false;
        }

        // TODO: fix small drags not taking effect
        int relXQuart = (int) (-dragX / Configs.PixelsPerBiome);
        int relZQuart = (int) (-dragY / Configs.PixelsPerBiome);

        this.centerQuart = this.centerQuart.add(relXQuart, relZQuart);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (this.handleMapClicked(mouseX, mouseY, button)) {
            return true;
        }
        return false;
    }

    private boolean handleMapClicked(double mouseX, double mouseY, int button) {
        if (button != InputConstants.MOUSE_BUTTON_RIGHT) {
            return false;
        }
        if (mouseX < HORIZONTAL_PADDING || mouseX > HORIZONTAL_PADDING + this.seedMapWidth || mouseY < VERTICAL_PADDING || mouseY > VERTICAL_PADDING + this.seedMapHeight) {
            return false;
        }
        this.minecraft.keyboardHandler.setClipboard("%d ~ %d".formatted(QuartPos.toBlock(this.mouseQuart.x()), QuartPos.toBlock(this.mouseQuart.z())));
        this.displayCoordinatesCopiedTicks = SharedConstants.TICKS_PER_SECOND;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return this.handleTeleportFieldEnter(keyCode, scanCode, modifiers);
    }

    private boolean handleTeleportFieldEnter(int keyCode, int scanCode, int modifiers) {
        if (keyCode != InputConstants.KEY_RETURN) {
            return false;
        }
        if (!this.editBoxX.isActive() && !this.editBoxZ.isActive()) {
            return false;
        }
        int x, z;
        try {
            x = Integer.parseInt(this.editBoxX.getValue());
            z = Integer.parseInt(this.editBoxZ.getValue());
        } catch (NumberFormatException _) {
            return false;
        }
        if (x < -MinecraftServer.ABSOLUTE_MAX_WORLD_SIZE || x > MinecraftServer.ABSOLUTE_MAX_WORLD_SIZE) {
            return false;
        }
        if (z < -MinecraftServer.ABSOLUTE_MAX_WORLD_SIZE || z > MinecraftServer.ABSOLUTE_MAX_WORLD_SIZE) {
            return false;
        }
        this.centerQuart = new QuartPos2(QuartPos.fromBlock(x), QuartPos.fromBlock(z));
        return true;
    }

    @Override
    public void onClose() {
        super.onClose();
        this.biomeTileCache.values().forEach(Tile::close);
        this.slimeChunkTileCache.values().forEach(Tile::close);
        this.seedMapExecutor.close(this.arena::close);
        Configs.save();
    }
}
