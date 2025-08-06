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
import dev.xpple.seedmapper.thread.SeedMapThreadingHelper;
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
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.Arrays;
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

    private static final IntSupplier TILE_SIZE_PIXELS = () -> TilePos.TILE_SIZE_CHUNKS * SCALED_CHUNK_SIZE * Configs.PixelsPerBiome;

    private static final List<MapFeature> TOGGLEABLE_FEATURES = Arrays.stream(MapFeature.values()).sorted(Comparator.comparing(MapFeature::getName)).toList();

    private static final int FEATURE_ICONS_COMBINED_WIDTH = Arrays.stream(MapFeature.values())
        .map(feature -> feature.getTexture().width())
        .reduce((l, r) -> l + HORIZONTAL_FEATURE_TOGGLE_SPACING + r)
        .orElseThrow();

    private static final Object2ObjectMap<WorldIdentifier, Object2ObjectMap<TilePos, int[]>> biomeDataCache = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectMap<WorldIdentifier, Object2ObjectMap<ChunkPos, StructureData>> structureDataCache = new Object2ObjectOpenHashMap<>();
    public static final Object2ObjectMap<WorldIdentifier, TwoDTree> strongholdDataCache = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectMap<WorldIdentifier, Object2ObjectMap<TilePos, OreVeinData>> oreVeinDataCache = new Object2ObjectOpenHashMap<>();

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

    private final Object2ObjectMap<TilePos, Tile> tileCache = new Object2ObjectOpenHashMap<>();
    private final ObjectSet<TilePos> pendingBiomeCalculations = ObjectSets.synchronize(new ObjectOpenHashSet<>());
    private final Object2ObjectMap<TilePos, int[]> biomeCache;
    private final Object2ObjectMap<ChunkPos, StructureData> structureCache;
    private final Object2ObjectMap<TilePos, OreVeinData> oreVeinCache;

    private final BlockPos playerPos;

    private QuartPos2 centerQuart;

    private int centerX;
    private int centerY;
    private int seedMapWidth;
    private int seedMapHeight;

    private List<List<MapFeature.Texture>> featureToggleLocations;

    private QuartPos2 mouseQuart;

    private int displayCoordinatesCopiedTicks = 0;

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

        this.biomeCache = Object2ObjectMaps.synchronize(biomeDataCache.computeIfAbsent(this.worldIdentifier, _ -> new Object2ObjectOpenHashMap<>()));
        this.structureCache = structureDataCache.computeIfAbsent(this.worldIdentifier, _ -> new Object2ObjectOpenHashMap<>());
        this.oreVeinCache = oreVeinDataCache.computeIfAbsent(this.worldIdentifier, _ -> new Object2ObjectOpenHashMap<>());

        if (!strongholdDataCache.containsKey(this.worldIdentifier)) {
            SeedMapThreadingHelper.submitStrongholdCalculation(() -> LocateCommand.calculateStrongholds(this.seed, this.dimension, this.version))
                .thenAccept(tree -> {
                    if (tree != null) {
                        strongholdDataCache.put(this.worldIdentifier, tree);
                    }
                });
        }

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
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // draw seed
        Component seedComponent = Component.translatable("seedMap.seed", accent(Long.toString(this.seed)));
        guiGraphics.drawString(this.font, seedComponent, HORIZONTAL_PADDING, VERTICAL_PADDING - this.font.lineHeight - 1, -1);

        this.featureToggleLocations = this.drawFeatureToggles(guiGraphics);

        int tileSizePixels = TILE_SIZE_PIXELS.getAsInt();
        int horTileRadius = Math.ceilDiv(this.seedMapWidth, tileSizePixels) + 1;
        int verTileRadius = Math.ceilDiv(this.seedMapHeight, tileSizePixels) + 1;

        TilePos centerTile = TilePos.fromQuartPos(this.centerQuart);
        for (int relTileX = -horTileRadius; relTileX <= horTileRadius; relTileX++) {
            for (int relTileZ = -verTileRadius; relTileZ <= verTileRadius; relTileZ++) {
                TilePos tilePos = centerTile.add(relTileX, relTileZ);

                // compute biomes and store in texture
                int[] biomeData = this.biomeCache.get(tilePos);
                if (biomeData == null) {
                    if (!this.pendingBiomeCalculations.add(tilePos)) {
                        continue;
                    }
                    SeedMapThreadingHelper.submitBiomeCalculation(() -> this.calculateBiomeData(tilePos)).thenAccept(data -> {
                        if (data != null) {
                            this.biomeCache.put(tilePos, data);
                            this.pendingBiomeCalculations.remove(tilePos);
                        }
                    });
                    continue;
                }

                Tile tile = this.tileCache.computeIfAbsent(tilePos, _ -> this.createTile(tilePos, biomeData));

                QuartPos2 relQuartPos = QuartPos2.fromTilePos(tilePos).subtract(this.centerQuart);
                int minX = this.centerX + Configs.PixelsPerBiome * relQuartPos.x();
                int minY = this.centerY + Configs.PixelsPerBiome * relQuartPos.z();
                int maxX = minX + tileSizePixels;
                int maxY = minY + tileSizePixels;

                if (maxX < HORIZONTAL_PADDING || minX > HORIZONTAL_PADDING + this.seedMapWidth) {
                    continue;
                }
                if (maxY < VERTICAL_PADDING || minY > VERTICAL_PADDING + this.seedMapHeight) {
                    continue;
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
        }

        int horChunkRadius = Math.ceilDiv(this.seedMapWidth / 2, SCALED_CHUNK_SIZE * Configs.PixelsPerBiome);
        int verChunkRadius = Math.ceilDiv(this.seedMapHeight / 2, SCALED_CHUNK_SIZE * Configs.PixelsPerBiome);

        // compute structures
        Configs.ToggledFeatures.stream()
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

        // compute strongholds
        if (Configs.ToggledFeatures.contains(MapFeature.STRONGHOLD)) {
            TwoDTree tree = strongholdDataCache.get(this.worldIdentifier);
            if (tree != null) {
                for (BlockPos strongholdPos : tree) {
                    this.drawMapFeature(guiGraphics, MapFeature.STRONGHOLD, strongholdPos);
                }
            }
        }

        // compute ore veins
        if (Configs.ToggledFeatures.contains(MapFeature.IRON_ORE_VEIN) || Configs.ToggledFeatures.contains(MapFeature.COPPER_ORE_VEIN)) {
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
        // draw hovered coordinates
        Component coordinates = accent("x: %d, z: %d".formatted(QuartPos.toBlock(this.mouseQuart.x()), QuartPos.toBlock(this.mouseQuart.z())));
        if (this.displayCoordinatesCopiedTicks > 0) {
            coordinates = Component.translatable("seedMap.coordinatesCopied", coordinates);
        }
        guiGraphics.drawString(this.font, coordinates , HORIZONTAL_PADDING, VERTICAL_PADDING + this.seedMapHeight + 1, -1);
    }

    private Tile createTile(TilePos tilePos, int[] biomeData) {
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

    private List<List<MapFeature.Texture>> drawFeatureToggles(GuiGraphics guiGraphics) {
        // TODO: replace with Gatherers API?
        // TODO: only calculate on resize?
        int rows = Math.ceilDiv(FEATURE_ICONS_COMBINED_WIDTH, this.seedMapWidth);
        List<List<MapFeature.Texture>> featureToggleLocations = new ArrayList<>(rows);
        int togglesPerRow = Math.ceilDiv(MapFeature.values().length, rows);
        int toggleMinY = 1;
        for (int row = 0; row < rows - 1; row++) {
            List<MapFeature.Texture> featureToggleRow = drawFeatureTogglesInner(guiGraphics, row, togglesPerRow, togglesPerRow, HORIZONTAL_PADDING, toggleMinY);
            featureToggleLocations.add(featureToggleRow);
            toggleMinY += FEATURE_TOGGLE_HEIGHT + VERTICAL_FEATURE_TOGGLE_SPACING;
        }
        int togglesInLastRow = MapFeature.values().length - togglesPerRow * (rows - 1);
        List<MapFeature.Texture> lastFeatureToggleRow = drawFeatureTogglesInner(guiGraphics, rows - 1, togglesPerRow, togglesInLastRow, HORIZONTAL_PADDING, toggleMinY);
        featureToggleLocations.add(lastFeatureToggleRow);
        return featureToggleLocations;
    }

    private static List<MapFeature.Texture> drawFeatureTogglesInner(GuiGraphics guiGraphics, int row, int togglesPerRow, int maxToggles, int toggleMinX, int toggleMinY) {
        List<MapFeature.Texture> featureToggleRow = new ArrayList<>(maxToggles);
        for (int toggle = 0; toggle < maxToggles; toggle++) {
            MapFeature feature = TOGGLEABLE_FEATURES.get(row * togglesPerRow + toggle);
            MapFeature.Texture featureIcon = feature.getTexture();
            featureToggleRow.add(featureIcon);
            int colour = -1;
            if (!Configs.ToggledFeatures.contains(feature)) {
                colour = ARGB.color(255 >> 1, 255, 255, 255);
            }
            drawFeatureIcon(guiGraphics, featureIcon, toggleMinX, toggleMinY, colour);
            toggleMinX += featureIcon.width() + HORIZONTAL_FEATURE_TOGGLE_SPACING;
        }
        return featureToggleRow;
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

    private @Nullable BlockPos calculateStructurePos(RegionPos regionPos, MemorySegment structurePos, StructureChecks.GenerationCheck generationCheck) {
        if (!generationCheck.check(this.structureGenerator, this.surfaceNoise, regionPos.x(), regionPos.z(), structurePos)) {
            return null;
        }

        return new BlockPos(Pos.x(structurePos), 0, Pos.z(structurePos));
    }

    private @Nullable OreVeinData calculateOreVein(TilePos tilePos) {
        ChunkPos chunkPos = tilePos.toChunkPos();
        for (int relChunkX = 0; relChunkX < TilePos.TILE_SIZE_CHUNKS; relChunkX++) {
            for (int relChunkZ = 0; relChunkZ < SCALED_CHUNK_SIZE; relChunkZ++) {
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

    private static void drawFeatureIcon(GuiGraphics guiGraphics, MapFeature.Texture featureIcon, int minX, int minY, int colour) {
        int iconWidth = featureIcon.width();
        int iconHeight = featureIcon.height();
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, featureIcon.resourceLocation(), minX, minY, 0, 0, iconWidth, iconHeight, iconWidth, iconHeight, iconWidth, iconHeight, colour);
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
        if (this.handleMapClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (this.handleToggleClicked(mouseX, mouseY, button)) {
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

    private boolean handleToggleClicked(double mouseX, double mouseY, int button) {
        if (mouseX < HORIZONTAL_PADDING || mouseX > HORIZONTAL_PADDING + this.seedMapWidth || mouseY < 1 || mouseY > VERTICAL_PADDING) {
            return false;
        }
        int minY = 1;
        int featureToggleRowCount = this.featureToggleLocations.size();
        int clickedRowIndex = -1;
        for (int rowIndex = 0; rowIndex < featureToggleRowCount; rowIndex++) {
            minY += FEATURE_TOGGLE_HEIGHT;
            if (minY > mouseY) {
                clickedRowIndex = rowIndex;
                break;
            }
            minY += VERTICAL_FEATURE_TOGGLE_SPACING;
        }
        if (clickedRowIndex == -1) {
            return false;
        }
        int minX = HORIZONTAL_PADDING;
        List<MapFeature.Texture> featureToggleRow = this.featureToggleLocations.get(clickedRowIndex);
        int featureToggleRowSize = featureToggleRow.size();
        int clickedToggleIndex = -1;
        for (int toggleIndex = 0; toggleIndex < featureToggleRowSize; toggleIndex++) {
            MapFeature.Texture featureIcon = featureToggleRow.get(toggleIndex);
            minX += featureIcon.width();
            if (minX > mouseX) {
                clickedToggleIndex = toggleIndex;
                break;
            }
            minX += HORIZONTAL_FEATURE_TOGGLE_SPACING;
        }
        if (clickedToggleIndex == -1) {
            return false;
        }
        MapFeature feature = TOGGLEABLE_FEATURES.get(clickedRowIndex * this.featureToggleLocations.getFirst().size() + clickedToggleIndex);
        if (!Configs.ToggledFeatures.remove(feature)) {
            Configs.ToggledFeatures.add(feature);
        }
        return true;
    }

    @Override
    public void onClose() {
        super.onClose();
        this.tileCache.values().forEach(Tile::close);
        SeedMapThreadingHelper.close(this.arena::close);
        Configs.save();
    }
}
