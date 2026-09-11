package dev.xpple.seedmapper.seedmap;

import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.Pos;
import com.github.cubiomes.StructureConfig;
import dev.xpple.seedmapper.SeedMapper;
import dev.xpple.seedmapper.config.Configs;
import dev.xpple.seedmapper.feature.StructureChecks;
import dev.xpple.seedmapper.util.CubiomesHelper;
import dev.xpple.seedmapper.util.QuartPos2;
import dev.xpple.seedmapper.util.QuartPos2f;
import dev.xpple.seedmapper.util.RegionPos;
import dev.xpple.seedmapper.util.SeedIdentifierWithDimension;
import dev.xpple.seedmapper.util.TwoDTree;
import dev.xpple.simplewaypoints.api.SimpleWaypointsAPI;
import dev.xpple.simplewaypoints.api.Waypoint;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SequenceLayout;
import java.util.BitSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntSupplier;
import java.util.stream.Stream;

public class SeedMapRenderer {
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
    private static final int[] biomeColors = new int[256];

    static {
        // unsigned char color[3]
        SequenceLayout rgbLayout = MemoryLayout.sequenceLayout(3, Cubiomes.C_CHAR);

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment biomeColorsInternal = arena.allocate(rgbLayout, biomeColors.length);
            Cubiomes.initBiomeColors(biomeColorsInternal);
            for (int biome = 0; biome < biomeColors.length; biome++) {
                MemorySegment colorArray = biomeColorsInternal.asSlice(biome * rgbLayout.byteSize());
                int red = colorArray.getAtIndex(Cubiomes.C_CHAR, 0) & 0xFF;
                int green = colorArray.getAtIndex(Cubiomes.C_CHAR, 1) & 0xFF;
                int blue = colorArray.getAtIndex(Cubiomes.C_CHAR, 2) & 0xFF;
                int color = ARGB.color(red, green, blue);
                biomeColors[biome] = color;
            }
        }
    }

    public static final int MIN_BIOME_Y = -64;
    public static final int MAX_BIOME_Y = 320;
    public static final int BIOME_Y_GRANULARITY = 16;

    public static final int MIN_PIXELS_PER_BIOME = 1;
    public static final int MAX_PIXELS_PER_BIOME = 100;

    private static final Identifier DIRECTION_ARROW_TEXTURE = Identifier.fromNamespaceAndPath(SeedMapper.MOD_ID, "textures/gui/arrow.png");

    private static final IntSupplier TILE_SIZE_PIXELS = () -> TilePos.TILE_SIZE_CHUNKS * SeedMapData.SCALED_CHUNK_SIZE * Configs.PixelsPerBiome;

    private static final Minecraft minecraft = Minecraft.getInstance();

    private final SeedMapData seedMapData;
    private BlockPos playerPos;
    private Vec2 playerRotation;
    private QuartPos2f centerQuart;
    private final IntSupplier horizontalPadding;
    private final IntSupplier verticalPadding;
    private final boolean rotateIcons;

    public int centerX;
    public int centerY;

    public int seedMapWidth;
    public int seedMapHeight;

    private final ObjectSet<FeatureWidget> featureWidgets = new ObjectOpenHashSet<>();

    private final Object2ObjectMap<ObjectIntPair<TilePos>, Tile> biomeTileCache = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<TilePos, Tile> slimeChunkTileCache = new Object2ObjectOpenHashMap<>();

    public SeedMapRenderer(SeedIdentifierWithDimension seedIdentifierWithDimension, BlockPos playerPos, Vec2 playerRotation, IntSupplier horizontalPadding, IntSupplier verticalPadding) {
        this(seedIdentifierWithDimension, playerPos, playerRotation, horizontalPadding, verticalPadding, false);
    }

    public SeedMapRenderer(SeedIdentifierWithDimension seedIdentifierWithDimension, BlockPos playerPos, Vec2 playerRotation, IntSupplier horizontalPadding, IntSupplier verticalPadding, boolean rotateIcons) {
        this.seedMapData = new SeedMapData(seedIdentifierWithDimension);

        this.playerPos = playerPos;
        this.playerRotation = playerRotation;
        this.centerQuart = QuartPos2f.fromQuartPos(QuartPos2.fromBlockPos(this.playerPos));
        this.horizontalPadding = horizontalPadding;
        this.verticalPadding = verticalPadding;
        this.rotateIcons = rotateIcons;
    }

    public void renderBiomes(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        int tileSizePixels = TILE_SIZE_PIXELS.getAsInt();
        int horTileRadius = Math.ceilDiv(this.seedMapWidth, tileSizePixels) + 1;
        int verTileRadius = Math.ceilDiv(this.seedMapHeight, tileSizePixels) + 1;

        TilePos centerTile = TilePos.fromQuartPos(QuartPos2.fromQuartPos2f(this.centerQuart));
        for (int relTileX = -horTileRadius; relTileX <= horTileRadius; relTileX++) {
            for (int relTileZ = -verTileRadius; relTileZ <= verTileRadius; relTileZ++) {
                TilePos tilePos = centerTile.add(relTileX, relTileZ);
                ObjectIntPair<TilePos> pair = ObjectIntPair.of(tilePos, this.seedMapData.getBiomeYHeight());

                // compute biomes and store in texture
                int[] biomeData = this.seedMapData.getBiomeData(pair);
                if (biomeData != null) {
                    Tile tile = this.biomeTileCache.computeIfAbsent(pair, _ -> this.createBiomeTile(tilePos, biomeData));
                    this.drawTile(guiGraphicsExtractor, tile);
                }

                // compute slime chunks and store in texture
                if (this.seedMapData.getAvailableFeatures().contains(MapFeature.SLIME_CHUNK) && Configs.ToggledFeatures.contains(MapFeature.SLIME_CHUNK)) {
                    BitSet slimeChunkData = this.seedMapData.getSlimeChunkData(tilePos);
                    if (slimeChunkData != null) {
                        Tile tile = this.slimeChunkTileCache.computeIfAbsent(tilePos, _ -> this.createSlimeChunkTile(tilePos, slimeChunkData));
                        this.drawTile(guiGraphicsExtractor, tile);
                    }
                }
            }
        }
    }

    public void renderFeatures(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        int tileSizePixels = TILE_SIZE_PIXELS.getAsInt();
        int horTileRadius = Math.ceilDiv(this.seedMapWidth, tileSizePixels) + 1;
        int verTileRadius = Math.ceilDiv(this.seedMapHeight, tileSizePixels) + 1;

        TilePos centerTile = TilePos.fromQuartPos(QuartPos2.fromQuartPos2f(this.centerQuart));

        int horChunkRadius = Math.ceilDiv(this.seedMapWidth / 2, SeedMapData.SCALED_CHUNK_SIZE * Configs.PixelsPerBiome);
        int verChunkRadius = Math.ceilDiv(this.seedMapHeight / 2, SeedMapData.SCALED_CHUNK_SIZE * Configs.PixelsPerBiome);

        SeedIdentifierWithDimension seedIdentifierWithDimension = this.seedMapData.getSeedIdentifierWithDimension();

        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment structurePos = Pos.allocate(tempArena);

            // compute structures
            Configs.ToggledFeatures.stream()
                .filter(this.seedMapData.getAvailableFeatures()::contains)
                .filter(f -> f.getStructureId() != -1)
                .forEach(feature -> {
                    int structure = feature.getStructureId();
                    MemorySegment structureConfig = this.seedMapData.getStructureConfig(structure);
                    if (structureConfig == null) {
                        return;
                    }
                    int regionSize = StructureConfig.regionSize(structureConfig);
                    RegionPos centerRegion = RegionPos.fromQuartPos(QuartPos2.fromQuartPos2f(this.centerQuart), regionSize);
                    int horRegionRadius = Math.ceilDiv(horChunkRadius, regionSize);
                    int verRegionRadius = Math.ceilDiv(verChunkRadius, regionSize);
                    StructureChecks.GenerationCheck generationCheck = StructureChecks.getGenerationCheck(structure);
                    for (int relRegionX = -horRegionRadius; relRegionX <= horRegionRadius; relRegionX++) {
                        for (int relRegionZ = -verRegionRadius; relRegionZ <= verRegionRadius; relRegionZ++) {
                            RegionPos regionPos = centerRegion.add(relRegionX, relRegionZ);
                            if (Cubiomes.getStructurePos(structure, seedIdentifierWithDimension.version(), seedIdentifierWithDimension.seed(), regionPos.x(), regionPos.z(), structurePos) == 0) {
                                continue;
                            }
                            ChunkPos chunkPos = new ChunkPos(SectionPos.blockToSectionCoord(Pos.x(structurePos)), SectionPos.blockToSectionCoord(Pos.z(structurePos)));

                            ChunkStructureData chunkStructureData = this.seedMapData.getChunkStructureData(chunkPos);
                            StructureData data = this.seedMapData.getStructureData(chunkStructureData, feature, regionPos, structurePos, generationCheck);
                            if (data == null) {
                                continue;
                            }
                            this.addFeatureWidget(feature, data.texture(), data.pos());
                        }
                    }
                });
        }

        guiGraphicsExtractor.nextStratum();

        // draw strongholds
        if (this.seedMapData.getAvailableFeatures().contains(MapFeature.STRONGHOLD) && Configs.ToggledFeatures.contains(MapFeature.STRONGHOLD)) {
            TwoDTree tree = this.seedMapData.getStrongholdData();
            if (tree != null) {
                for (BlockPos strongholdPos : tree) {
                    this.addFeatureWidget(MapFeature.STRONGHOLD, strongholdPos);
                }
            }
        }

        // compute ore veins
        if ((this.seedMapData.getAvailableFeatures().contains(MapFeature.COPPER_ORE_VEIN) || this.seedMapData.getAvailableFeatures().contains(MapFeature.IRON_ORE_VEIN))
            && (Configs.ToggledFeatures.contains(MapFeature.COPPER_ORE_VEIN) || Configs.ToggledFeatures.contains(MapFeature.IRON_ORE_VEIN))) {
            for (int relTileX = -horTileRadius; relTileX <= horTileRadius; relTileX++) {
                for (int relTileZ = -verTileRadius; relTileZ <= verTileRadius; relTileZ++) {
                    TilePos tilePos = new TilePos(centerTile.x() + relTileX, centerTile.z() + relTileZ);
                    OreVeinData oreVeinData = this.seedMapData.getOreVeinData(tilePos);
                    if (oreVeinData == null) {
                        continue;
                    }
                    if (Configs.ToggledFeatures.contains(oreVeinData.oreVeinType())) {
                        this.addFeatureWidget(oreVeinData.oreVeinType(), oreVeinData.blockPos());
                    }
                }
            }
        }

        // compute canyons
        if ((this.seedMapData.getAvailableFeatures().contains(MapFeature.CANYON)) && Configs.ToggledFeatures.contains(MapFeature.CANYON)) {
            for (int relTileX = -horTileRadius; relTileX <= horTileRadius; relTileX++) {
                for (int relTileZ = -verTileRadius; relTileZ <= verTileRadius; relTileZ++) {
                    TilePos tilePos = new TilePos(centerTile.x() + relTileX, centerTile.z() + relTileZ);
                    ChunkPos chunkPos = tilePos.toChunkPos();
                    BitSet canyonData = this.seedMapData.getCanyonData(tilePos);
                    canyonData.stream().forEach(i -> {
                        int relChunkX = i % TilePos.TILE_SIZE_CHUNKS;
                        int relChunkZ = i / TilePos.TILE_SIZE_CHUNKS;
                        int chunkX = chunkPos.x() + relChunkX;
                        int chunkZ = chunkPos.z() + relChunkZ;
                        this.addFeatureWidget(MapFeature.CANYON, new BlockPos(SectionPos.sectionToBlockCoord(chunkX), 0, SectionPos.sectionToBlockCoord(chunkZ)));
                    });
                }
            }
        }

        // draw waypoints
        if (this.seedMapData.getAvailableFeatures().contains(MapFeature.WAYPOINT) && Configs.ToggledFeatures.contains(MapFeature.WAYPOINT)) {
            SimpleWaypointsAPI waypointsApi = SimpleWaypointsAPI.getInstance();
            String identifier = waypointsApi.getWorldIdentifier(minecraft);
            if (identifier != null) {
                Map<String, Waypoint> worldWaypoints = waypointsApi.getWorldWaypoints(identifier);
                if (worldWaypoints != null) {
                    worldWaypoints.forEach((name, waypoint) -> {
                        if (!waypoint.dimension().equals(CubiomesHelper.getMinecraftDimension(seedIdentifierWithDimension.dimension()))) {
                            return;
                        }
                        FeatureWidget widget = this.addFeatureWidget(MapFeature.WAYPOINT, waypoint.location());
                        if (widget == null) {
                            return;
                        }
                        int waypointCenterX = widget.x + widget.width() / 2;
                        int waypointCenterY = widget.y + widget.width() / 2;
                        var pose = guiGraphicsExtractor.pose();
                        pose.pushMatrix();
                        if (this.rotateIcons && Configs.RotateMinimap) {
                            pose.translate(waypointCenterX, waypointCenterY);
                            pose.rotate((float) (Math.toRadians(this.playerRotation.y) - Math.PI));
                            pose.translate(-waypointCenterX, -waypointCenterY);
                        }
                        guiGraphicsExtractor.centeredText(minecraft.font, name, waypointCenterX, waypointCenterY + widget.height() / 2, ARGB.color(255, waypoint.color()));
                        pose.popMatrix();
                    });
                }
            }
        }

        // calculate spawn point
        if (this.seedMapData.getAvailableFeatures().contains(MapFeature.WORLD_SPAWN) && Configs.ToggledFeatures.contains(MapFeature.WORLD_SPAWN)) {
            BlockPos spawnPoint = this.seedMapData.getSpawn();
            this.addFeatureWidget(MapFeature.WORLD_SPAWN, spawnPoint);
        }

        this.drawFeatureIcons(guiGraphicsExtractor);
    }

    private void drawTile(GuiGraphicsExtractor guiGraphicsExtractor, Tile tile) {
        TilePos tilePos = tile.pos();
        QuartPos2f relTileQuart = QuartPos2f.fromQuartPos(QuartPos2.fromTilePos(tilePos)).subtract(this.centerQuart);
        int tileSizePixels = TILE_SIZE_PIXELS.getAsInt();
        int minX = this.centerX + Mth.floor(Configs.PixelsPerBiome * relTileQuart.x());
        int minY = this.centerY + Mth.floor(Configs.PixelsPerBiome * relTileQuart.z());
        int maxX = minX + tileSizePixels;
        int maxY = minY + tileSizePixels;

        if (maxX < this.horizontalPadding.getAsInt() || minX > this.horizontalPadding.getAsInt() + this.seedMapWidth) {
            return;
        }
        if (maxY < this.verticalPadding.getAsInt() || minY > this.verticalPadding.getAsInt() + this.seedMapHeight) {
            return;
        }

        float u0, u1, v0, v1;
        if (minX < this.horizontalPadding.getAsInt()) {
            u0 = (float) (this.horizontalPadding.getAsInt() - minX) / tileSizePixels;
            minX = this.horizontalPadding.getAsInt();
        } else u0 = 0;
        if (maxX > this.horizontalPadding.getAsInt() + this.seedMapWidth) {
            u1 = 1 - ((float) (maxX - this.horizontalPadding.getAsInt() - this.seedMapWidth) / tileSizePixels);
            maxX = this.horizontalPadding.getAsInt() + this.seedMapWidth;
        } else u1 = 1;
        if (minY < this.verticalPadding.getAsInt()) {
            v0 = (float) (this.verticalPadding.getAsInt() - minY) / tileSizePixels;
            minY = this.verticalPadding.getAsInt();
        } else v0 = 0;
        if (maxY > this.verticalPadding.getAsInt() + this.seedMapHeight) {
            v1 = 1 - ((float) (maxY - this.verticalPadding.getAsInt() - this.seedMapHeight) / tileSizePixels);
            maxY = this.verticalPadding.getAsInt() + this.seedMapHeight;
        } else v1 = 1;

        guiGraphicsExtractor.innerBlit(RenderPipelines.GUI_TEXTURED, tile.texture().getTextureView(), tile.texture().getSampler(), minX, minY, maxX, maxY, u0, u1, v0, v1, 0xFF_FFFFFF);
    }

    private Tile createBiomeTile(TilePos tilePos, int[] biomeData) {
        SeedIdentifierWithDimension seedIdentifierWithDimension = this.seedMapData.getSeedIdentifierWithDimension();
        Tile tile = new Tile(tilePos, seedIdentifierWithDimension.seed(), seedIdentifierWithDimension.dimension());
        DynamicTexture texture = tile.texture();
        int width = texture.getPixels().getWidth();
        int height = texture.getPixels().getHeight();
        for (int relX = 0; relX < width; relX++) {
            for (int relZ = 0; relZ < height; relZ++) {
                int biome = biomeData[relX + relZ * width];
                texture.getPixels().setPixel(relX, relZ, biomeColors[biome]);
            }
        }
        texture.upload();
        return tile;
    }

    private Tile createSlimeChunkTile(TilePos tilePos, BitSet slimeChunkData) {
        SeedIdentifierWithDimension seedIdentifierWithDimension = this.seedMapData.getSeedIdentifierWithDimension();
        Tile tile = new Tile(tilePos, seedIdentifierWithDimension.seed(), seedIdentifierWithDimension.dimension());
        DynamicTexture texture = tile.texture();
        for (int relChunkX = 0; relChunkX < TilePos.TILE_SIZE_CHUNKS; relChunkX++) {
            for (int relChunkZ = 0; relChunkZ < TilePos.TILE_SIZE_CHUNKS; relChunkZ++) {
                boolean isSlimeChunk = slimeChunkData.get(relChunkX + relChunkZ * TilePos.TILE_SIZE_CHUNKS);
                if (isSlimeChunk) {
                    texture.getPixels().fillRect(SeedMapData.SCALED_CHUNK_SIZE * relChunkX, SeedMapData.SCALED_CHUNK_SIZE * relChunkZ, SeedMapData.SCALED_CHUNK_SIZE, SeedMapData.SCALED_CHUNK_SIZE, 0xFF_00FF00);
                }
            }
        }
        texture.upload();
        return tile;
    }

    private @Nullable FeatureWidget addFeatureWidget(MapFeature feature, BlockPos pos) {
        return this.addFeatureWidget(feature, feature.getDefaultTexture(), pos);
    }

    private @Nullable FeatureWidget addFeatureWidget(MapFeature feature, MapFeature.Texture variantTexture, BlockPos pos) {
        FeatureWidget widget = new FeatureWidget(feature, variantTexture, pos);
        if (!widget.withinBounds()) {
            return null;
        }

        this.featureWidgets.add(widget);
        return widget;
    }

    private void drawFeatureIcons(GuiGraphicsExtractor guiGraphicsExtractor) {
        for (ObjectIterator<FeatureWidget> iterator = this.featureWidgets.iterator(); iterator.hasNext();) {
            FeatureWidget widget = iterator.next();
            if (Configs.ToggledFeatures.contains(widget.feature)) {
                MapFeature.Texture texture = widget.texture();
                this.drawIcon(guiGraphicsExtractor, texture.identifier(), widget.x, widget.y, texture.width(), texture.height(), 0xFF_FFFFFF);
            } else {
                iterator.remove();
            }
        }
    }

    public void drawPlayerIndicator(GuiGraphicsExtractor guiGraphicsExtractor) {
        if (!this.seedMapData.getAvailableFeatures().contains(MapFeature.PLAYER_ICON) || !Configs.ToggledFeatures.contains(MapFeature.PLAYER_ICON)) {
            return;
        }
        QuartPos2f relPlayerQuart = QuartPos2f.fromQuartPos(QuartPos2.fromBlockPos(this.playerPos)).subtract(this.centerQuart);
        int playerMinX = this.centerX + Mth.floor(Configs.PixelsPerBiome * relPlayerQuart.x()) - 10;
        int playerMinY = this.centerY + Mth.floor(Configs.PixelsPerBiome * relPlayerQuart.z()) - 10;
        int playerMaxX = playerMinX + 20;
        int playerMaxY = playerMinY + 20;
        if (playerMinX < this.horizontalPadding.getAsInt() || playerMaxX > this.horizontalPadding.getAsInt() + this.seedMapWidth || playerMinY < this.verticalPadding.getAsInt() || playerMaxY > this.verticalPadding.getAsInt() + this.seedMapHeight) {
            return;
        }
        PlayerFaceExtractor.extractRenderState(guiGraphicsExtractor, minecraft.player.getSkin(), playerMinX, playerMinY, 20);

        this.drawDirectionArrow(guiGraphicsExtractor, playerMinX, playerMinY);
    }

    public void drawDirectionArrow(GuiGraphicsExtractor guiGraphicsExtractor, int playerMinX, int playerMinY) {
        guiGraphicsExtractor.pose().pushMatrix();
        Matrix3x2f transform = guiGraphicsExtractor.pose() // transformations are applied in reverse order
            .translate(10, 10)
            .translate(playerMinX, playerMinY)
            .rotate((float) (Math.toRadians(this.playerRotation.y) + Math.PI))
            .translate(-10, -10)
            .translate(0, -30)
            ;
        boolean withinBounds = Stream.of(new Vector2f(20, 0), new Vector2f(20, 20), new Vector2f(0, 20), new Vector2f(0, 0))
            .map(transform::transformPosition)
            .allMatch(v -> v.x >= this.horizontalPadding.getAsInt() && v.x <= this.horizontalPadding.getAsInt() + this.seedMapWidth &&
                v.y >= this.verticalPadding.getAsInt() && v.y <= this.verticalPadding.getAsInt() + this.seedMapHeight);
        if (withinBounds) {
            drawIconStatic(guiGraphicsExtractor, DIRECTION_ARROW_TEXTURE, 0, 0, 20, 20, 0xFF_FFFFFF);
        }
        guiGraphicsExtractor.pose().popMatrix();
    }

    public void moveCenter(QuartPos2f newCenter) {
        this.centerQuart = newCenter;

        this.updateFeatureWidgets();
    }

    public void updateFeatureWidgets() {
        this.featureWidgets.removeIf(widget -> {
            widget.updatePosition();
            return !widget.withinBounds();
        });
    }

    public Optional<FeatureWidget> getFeatureWidgetAt(double mouseX, double mouseY) {
        return this.featureWidgets.stream()
            .filter(widget -> mouseX >= widget.x && mouseX <= widget.x + widget.width() && mouseY >= widget.y && mouseY <= widget.y + widget.height())
            .findAny();
    }

    public FeatureWidget createFeatureWidget(MapFeature feature, BlockPos featureLocation) {
        return new FeatureWidget(feature, featureLocation);
    }

    public class FeatureWidget {
        private int x;
        private int y;
        private final MapFeature feature;
        private final MapFeature.Texture featureTexture;
        private final BlockPos featureLocation;

        public FeatureWidget(MapFeature feature, BlockPos featureLocation) {
            this(feature, feature.getDefaultTexture(), featureLocation);
        }

        public FeatureWidget(MapFeature feature, MapFeature.Texture variantTexture, BlockPos featureLocation) {
            this.feature = feature;
            this.featureTexture = variantTexture;
            this.featureLocation = featureLocation;
            this.updatePosition();
        }

        public int x() {
            return this.x;
        }

        public int y() {
            return this.y;
        }

        public MapFeature feature() {
            return this.feature;
        }

        public MapFeature.Texture texture() {
            return this.featureTexture;
        }

        public BlockPos featureLocation() {
            return this.featureLocation;
        }

        public int width() {
            return this.featureTexture.width();
        }

        public int height() {
            return this.featureTexture.height();
        }

        public void updatePosition() {
            QuartPos2f relFeatureQuart = QuartPos2f.fromQuartPos(QuartPos2.fromBlockPos(this.featureLocation)).subtract(centerQuart);
            this.x = centerX + Mth.floor(Configs.PixelsPerBiome * relFeatureQuart.x()) - this.featureTexture.width() / 2;
            this.y = centerY + Mth.floor(Configs.PixelsPerBiome * relFeatureQuart.z()) - this.featureTexture.height() / 2;
        }

        public boolean withinBounds() {
            int minX = this.x;
            int minY = this.y;
            int maxX = minX + this.width();
            int maxY = minY + this.height();

            if (maxX >= horizontalPadding.getAsInt() + seedMapWidth || maxY >= verticalPadding.getAsInt() + seedMapHeight) {
                return false;
            }
            if (minX < horizontalPadding.getAsInt() || minY < verticalPadding.getAsInt()) {
                return false;
            }
            return true;
        }

        public void render(GuiGraphicsExtractor guiGraphicsExtractor, int color) {
            MapFeature.Texture texture = this.texture();
            int iconWidth = texture.width();
            int iconHeight = texture.height();

            drawIconStatic(guiGraphicsExtractor, texture.identifier(), this.x, this.y, iconWidth, iconHeight, color);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.feature, this.featureTexture, this.featureLocation);
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            FeatureWidget that = (FeatureWidget) o;
            return this.feature == that.feature && Objects.equals(this.featureTexture, that.featureTexture) && Objects.equals(this.featureLocation, that.featureLocation);
        }
    }

    private void drawIcon(GuiGraphicsExtractor guiGraphicsExtractor, Identifier identifier, int minX, int minY, int iconWidth, int iconHeight, int color) {
        var pose = guiGraphicsExtractor.pose();
        pose.pushMatrix();
        if (this.rotateIcons && Configs.RotateMinimap) {
            pose.translate(minX + (float) iconWidth / 2, minY + (float) iconWidth / 2);
            pose.rotate((float) (Math.toRadians(this.playerRotation.y) - Math.PI));
            pose.translate(-minX - (float) iconWidth / 2, -minY - (float) iconWidth / 2);
        }
        drawIconStatic(guiGraphicsExtractor, identifier, minX, minY, iconWidth, iconHeight, color);
        pose.popMatrix();
    }

    public static void drawIconStatic(GuiGraphicsExtractor guiGraphicsExtractor, Identifier identifier, int minX, int minY, int iconWidth, int iconHeight, int color) {
        // Skip intersection checks (GuiRenderState.hasIntersection) you would otherwise get when calling
        // GuiGraphics.blit as these checks incur a significant performance hit
        AbstractTexture texture = minecraft.getTextureManager().getTexture(identifier);
        BlitRenderState renderState = new BlitRenderState(RenderPipelines.GUI_TEXTURED, TextureSetup.singleTexture(texture.getTextureView(), texture.getSampler()), new Matrix3x2f(guiGraphicsExtractor.pose()), minX, minY, minX + iconWidth, minY + iconHeight, 0, 1, 0, 1, color, guiGraphicsExtractor.scissorStack.peek());
        guiGraphicsExtractor.guiRenderState.addBlitToCurrentLayer(renderState);
    }

    public SeedMapData getSeedMapData() {
        return this.seedMapData;
    }

    public void updatePlayerPosition(BlockPos pos) {
        this.playerPos = pos;
    }

    public void updatePlayerRotation(Vec2 vec2) {
        this.playerRotation = vec2;
    }

    public Vec2 getPlayerRotation() {
        return this.playerRotation;
    }

    public QuartPos2f getCenterQuart() {
        return this.centerQuart;
    }

    public void close() {
        this.seedMapData.close();
        this.biomeTileCache.values().forEach(Tile::close);
        this.slimeChunkTileCache.values().forEach(Tile::close);
    }
}
