package dev.xpple.seedmapper.seedmap;

import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.Generator;
import com.github.cubiomes.Pos;
import com.github.cubiomes.Range;
import com.github.cubiomes.StructureConfig;
import com.github.cubiomes.SurfaceNoise;
import com.mojang.blaze3d.platform.InputConstants;
import dev.xpple.seedmapper.config.Configs;
import dev.xpple.seedmapper.feature.StructureChecks;
import dev.xpple.seedmapper.util.QuartPos2;
import dev.xpple.seedmapper.util.RegionPos;
import dev.xpple.seedmapper.util.SpiralLoop;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;

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

    // unsigned char color[3]
    private static final MemoryLayout RGB_LAYOUT = MemoryLayout.sequenceLayout(3, Cubiomes.C_CHAR);
    private static final MemorySegment biomeColours;

    static {
        biomeColours = Arena.global().allocate(RGB_LAYOUT, 256);
        Cubiomes.initBiomeColors(biomeColours);
    }

    public static final int BIOME_SCALE = 4;
    public static final int SCALED_CHUNK_SIZE = LevelChunkSection.SECTION_WIDTH / BIOME_SCALE;

    private static final int HORIZONTAL_PADDING = 50;
    private static final int VERTICAL_PADDING = 50;

    public static final int MIN_PIXELS_PER_BIOME = 1;
    public static final int MAX_PIXELS_PER_BIOME = 100;

    private static final int HORIZONTAL_STRUCTURE_TOGGLE_SPACING = 5;
    private static final int VERTICAL_STRUCTURE_TOGGLE_SPACING = 1;
    private static final int STRUCTURE_TOGGLE_HEIGHT = 20;

    private static final IntSupplier TILE_SIZE_PIXELS = () -> TilePos.TILE_SIZE_CHUNKS * SCALED_CHUNK_SIZE * Configs.PixelsPerBiome;

    private static final int[] toggleStructures = StructureData.Structure.STRUCTURE_ICONS.keySet().intStream().sorted().toArray();

    private static final int STRUCTURE_ICONS_COMBINED_WIDTH = StructureData.Structure.STRUCTURE_ICONS.values().stream()
        .map(StructureData.Structure.Texture::width)
        .collect(() -> new MutableInt(0), MutableInt::add, (l, r) -> l.add(r.addAndGet(HORIZONTAL_STRUCTURE_TOGGLE_SPACING))).intValue();

    private static final Long2ObjectMap<Int2ObjectMap<Object2ObjectMap<TilePos, int[]>>> biomeDataCache = new Long2ObjectOpenHashMap<>();
    private static final Long2ObjectMap<Int2ObjectMap<Object2ObjectMap<ChunkPos, StructureData>>> structureDataCache = new Long2ObjectOpenHashMap<>();

    private final Arena arena = Arena.ofConfined();

    private final long seed;
    private final int dimension;
    private final int version;

    private final MemorySegment generator;
    private final MemorySegment surfaceNoise;

    private final Object2ObjectMap<TilePos, Tile> tileCache = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<TilePos, int[]> biomeCache;
    private final Object2ObjectMap<ChunkPos, StructureData> structureCache;

    private final BlockPos playerPos;

    private QuartPos2 centerQuart;

    private int centerX;
    private int centerY;
    private int seedMapWidth;
    private int seedMapHeight;

    private List<List<StructureData.Structure.Texture>> structureToggleLocations;

    private QuartPos2 mouseQuart;

    private int displayCoordinatesCopiedTicks = 0;

    public SeedMapScreen(long seed, int dimension, int version, BlockPos playerPos) {
        super(Component.empty());
        this.seed = seed;
        this.dimension = dimension;
        this.version = version;

        this.generator = Generator.allocate(this.arena);
        Cubiomes.setupGenerator(this.generator, this.version, 0);
        Cubiomes.applySeed(this.generator, this.dimension, this.seed);

        this.surfaceNoise = SurfaceNoise.allocate(this.arena);
        Cubiomes.initSurfaceNoise(this.surfaceNoise, this.dimension, this.seed);

        this.biomeCache = biomeDataCache
            .computeIfAbsent(this.seed, _ -> new Int2ObjectArrayMap<>(3))
            .computeIfAbsent(this.dimension, _ -> new Object2ObjectOpenHashMap<>());
        this.structureCache = structureDataCache
            .computeIfAbsent(this.seed, _ -> new Int2ObjectArrayMap<>(3))
            .computeIfAbsent(this.dimension, _ -> new Object2ObjectOpenHashMap<>());

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

        this.structureToggleLocations = this.drawStructureToggles(guiGraphics);

        int tileSizePixels = TILE_SIZE_PIXELS.getAsInt();
        int horTileRadius = Math.ceilDiv(this.seedMapWidth, tileSizePixels) + 1;
        int verTileRadius = Math.ceilDiv(this.seedMapHeight, tileSizePixels) + 1;

        TilePos centerTile = TilePos.fromQuartPos(this.centerQuart);
        SpiralLoop.spiral(centerTile.x(), centerTile.z(), Math.max(horTileRadius, verTileRadius), (tileX, tileZ) -> {
            TilePos tilePos = new TilePos(tileX, tileZ);
            int relTileX = tilePos.x() - centerTile.x();
            int relTileZ = tilePos.z() - centerTile.z();
            if (relTileX < -horTileRadius || relTileX > horTileRadius || relTileZ < -verTileRadius || relTileZ > verTileRadius) {
                return false;
            }

            // first pass, compute biomes and store in texture
            Tile tile = this.tileCache.computeIfAbsent(tilePos, _ -> this.fillTile(tilePos));

            QuartPos2 relQuartPos = QuartPos2.fromTilePos(tilePos).subtract(this.centerQuart);
            int minX = this.centerX + Configs.PixelsPerBiome * relQuartPos.x();
            int minY = this.centerY + Configs.PixelsPerBiome * relQuartPos.z();
            int maxX = minX + tileSizePixels;
            int maxY = minY + tileSizePixels;

            if (maxX < HORIZONTAL_PADDING || minX > HORIZONTAL_PADDING + this.seedMapWidth) {
                return false;
            }
            if (maxY < VERTICAL_PADDING || minY > VERTICAL_PADDING + this.seedMapHeight) {
                return false;
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
            return false;
        });

        int horChunkRadius = Math.ceilDiv(this.seedMapWidth / 2, SCALED_CHUNK_SIZE * Configs.PixelsPerBiome);
        int verChunkRadius = Math.ceilDiv(this.seedMapHeight / 2, SCALED_CHUNK_SIZE * Configs.PixelsPerBiome);

        // second pass, compute structures
        Configs.ToggledStructures.forEach(structure -> {
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
            // currently only used for end cities
            StructureChecks.GenerationCheck generationCheck = StructureChecks.getGenerationCheck(structure);
            MemorySegment structurePos = Pos.allocate(this.arena);
            SpiralLoop.spiral(centerRegion.x(), centerRegion.z(), Math.max(horRegionRadius, verRegionRadius), (regionX, regionZ) -> {
                RegionPos regionPos = new RegionPos(regionX, regionZ, regionSize);
                RegionPos relRegion = regionPos.subtract(centerRegion);
                if (relRegion.x() < -horRegionRadius || relRegion.x() > horRegionRadius || relRegion.z() < -verRegionRadius || relRegion.z() > verRegionRadius) {
                    return false;
                }
                if (Cubiomes.getStructurePos(structure, this.version, this.seed, regionPos.x(), regionPos.z(), structurePos) == 0) {
                    return false;
                }
                ChunkPos chunkPos = new ChunkPos(SectionPos.blockToSectionCoord(Pos.x(structurePos)), SectionPos.blockToSectionCoord(Pos.z(structurePos)));

                StructureData structureData = this.structureCache.computeIfAbsent(chunkPos, _ -> new StructureData(chunkPos, new Int2ObjectArrayMap<>()));
                StructureData.Structure structures = structureData.structures().computeIfAbsent(structure, _ -> this.calculateStructurePos(regionPos, structure, structurePos, generationCheck));
                if (structures == null) {
                    return false;
                }
                StructureData.Structure.Texture structureIcon = structures.texture();
                QuartPos2 relQuartPos = QuartPos2.fromBlockPos(structures.blockPos()).subtract(this.centerQuart);
                int minX = this.centerX + Configs.PixelsPerBiome * relQuartPos.x() - structureIcon.width() / 2;
                int minY = this.centerY + Configs.PixelsPerBiome * relQuartPos.z() - structureIcon.height() / 2;
                int maxX = minX + structureIcon.width();
                int maxY = minY + structureIcon.height();

                if (maxX >= HORIZONTAL_PADDING + this.seedMapWidth || maxY >= VERTICAL_PADDING + this.seedMapHeight) {
                    return false;
                }
                if (minX < HORIZONTAL_PADDING || minY < VERTICAL_PADDING) {
                    return false;
                }

                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, structureIcon.resourceLocation(), minX, minY, 0, 0, structureIcon.width(), structureIcon.height(), structureIcon.width(), structureIcon.height());
                return false;
            });
        });

        // draw player position
        int playerMinX = this.centerX + Configs.PixelsPerBiome * (QuartPos.fromBlock(this.playerPos.getX()) - this.centerQuart.x()) - 10;
        int playerMinZ = this.centerY + Configs.PixelsPerBiome * (QuartPos.fromBlock(this.playerPos.getZ()) - this.centerQuart.z()) - 10;
        PlayerFaceRenderer.draw(guiGraphics, this.minecraft.player.getSkin(), playerMinX, playerMinZ, 20);

        // draw hovered coordinates
        Component coordinates = accent("x: %d, z: %d".formatted(QuartPos.toBlock(this.mouseQuart.x()), QuartPos.toBlock(this.mouseQuart.z())));
        if (this.displayCoordinatesCopiedTicks > 0) {
            coordinates = Component.translatable("seedMap.coordinatesCopied", coordinates);
        }
        guiGraphics.drawString(this.font, coordinates , HORIZONTAL_PADDING, VERTICAL_PADDING + this.seedMapHeight + 1, -1);
    }

    private Tile fillTile(TilePos tilePos) {
        Tile tile = new Tile(tilePos, this.seed, this.dimension);
        int[] biomeData = this.biomeCache.computeIfAbsent(tilePos, _ -> this.calculateBiomeData(tilePos));

        DynamicTexture texture = tile.texture();
        int width = texture.getPixels().getWidth();
        int height = texture.getPixels().getHeight();
        for (int relX = 0; relX < width; relX++) {
            for (int relZ = 0; relZ < height; relZ++) {
                long biome = biomeData[relX + relZ * width];
                MemorySegment colourArray = biomeColours.asSlice(biome * RGB_LAYOUT.byteSize());
                int red = colourArray.getAtIndex(Cubiomes.C_CHAR, 0) & 0xFF;
                int green = colourArray.getAtIndex(Cubiomes.C_CHAR, 1) & 0xFF;
                int blue = colourArray.getAtIndex(Cubiomes.C_CHAR, 2) & 0xFF;
                int colour = ARGB.color(red, green, blue);

                texture.getPixels().setPixel(relX, relZ, colour);
            }
        }

        texture.upload();
        return tile;
    }

    private List<List<StructureData.Structure.Texture>> drawStructureToggles(GuiGraphics guiGraphics) {
        // TODO: replace with Gatherers API?
        // TODO: only calculate on resize?
        int rows = Math.ceilDiv(STRUCTURE_ICONS_COMBINED_WIDTH, this.seedMapWidth);
        List<List<StructureData.Structure.Texture>> structureToggleLocations = new ArrayList<>(rows);
        int togglesPerRow = Math.ceilDiv(StructureData.Structure.STRUCTURE_ICONS.size(), rows);
        int toggleMinY = 1;
        for (int row = 0; row < rows - 1; row++) {
            List<StructureData.Structure.Texture> structureToggleRow = drawStructureTogglesInner(guiGraphics, row, togglesPerRow, togglesPerRow, HORIZONTAL_PADDING, toggleMinY);
            structureToggleLocations.add(structureToggleRow);
            toggleMinY += STRUCTURE_TOGGLE_HEIGHT + VERTICAL_STRUCTURE_TOGGLE_SPACING;
        }
        int togglesInLastRow = StructureData.Structure.STRUCTURE_ICONS.size() - togglesPerRow * (rows - 1);
        List<StructureData.Structure.Texture> lastStructureToggleRow = drawStructureTogglesInner(guiGraphics, rows - 1, togglesPerRow, togglesInLastRow, HORIZONTAL_PADDING, toggleMinY);
        structureToggleLocations.add(lastStructureToggleRow);
        return structureToggleLocations;
    }

    private static List<StructureData.Structure.Texture> drawStructureTogglesInner(GuiGraphics guiGraphics, int row, int togglesPerRow, int maxToggles, int toggleMinX, int toggleMinY) {
        List<StructureData.Structure.Texture> structureToggleRow = new ArrayList<>(maxToggles);
        for (int toggle = 0; toggle < maxToggles; toggle++) {
            int structure = toggleStructures[row * togglesPerRow + toggle];
            StructureData.Structure.Texture structureIcon = StructureData.Structure.STRUCTURE_ICONS.get(structure);
            structureToggleRow.add(structureIcon);
            int colour = -1;
            if (!Configs.ToggledStructures.contains(structure)) {
                colour = ARGB.color(255 / 2, 255, 255, 255);
            }
            drawStructureIcon(guiGraphics, structureIcon, toggleMinX, toggleMinY, colour);
            toggleMinX += structureIcon.width() + HORIZONTAL_STRUCTURE_TOGGLE_SPACING;
        }
        return structureToggleRow;
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

        long cacheSize = Cubiomes.getMinCacheSize(this.generator, Range.scale(range), Range.sx(range), Range.sy(range), Range.sz(range));
        MemorySegment biomeIds = this.arena.allocate(Cubiomes.C_INT, cacheSize);
        Cubiomes.genBiomes(this.generator, biomeIds, range);

        return biomeIds.toArray(Cubiomes.C_INT);
    }

    private StructureData.@Nullable Structure calculateStructurePos(RegionPos regionPos, int structure, MemorySegment structurePos, StructureChecks.GenerationCheck generationCheck) {
        if (!generationCheck.check(this.generator, this.surfaceNoise, regionPos.x(), regionPos.z(), structurePos)) {
            return null;
        }

        BlockPos blockPos = new BlockPos(Pos.x(structurePos), 0, Pos.z(structurePos));
        return new StructureData.Structure(structure, blockPos);
    }

    private static void drawStructureIcon(GuiGraphics guiGraphics, StructureData.Structure.Texture structureIcon, int minX, int minY, int colour) {
        int iconWidth = structureIcon.width();
        int iconHeight = structureIcon.height();
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, structureIcon.resourceLocation(), minX, minY, 0, 0, iconWidth, iconHeight, iconWidth, iconHeight, iconWidth, iconHeight, colour);
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
        int structureToggleRowCount = this.structureToggleLocations.size();
        int clickedRowIndex = -1;
        for (int rowIndex = 0; rowIndex < structureToggleRowCount; rowIndex++) {
            minY += STRUCTURE_TOGGLE_HEIGHT;
            if (minY > mouseY) {
                clickedRowIndex = rowIndex;
                break;
            }
            minY += VERTICAL_STRUCTURE_TOGGLE_SPACING;
        }
        if (clickedRowIndex == -1) {
            return false;
        }
        int minX = HORIZONTAL_PADDING;
        List<StructureData.Structure.Texture> structureToggleRow = this.structureToggleLocations.get(clickedRowIndex);
        int structureToggleRowSize = structureToggleRow.size();
        int clickedToggleIndex = -1;
        for (int toggleIndex = 0; toggleIndex < structureToggleRowSize; toggleIndex++) {
            StructureData.Structure.Texture structureIcon = structureToggleRow.get(toggleIndex);
            minX += structureIcon.width();
            if (minX > mouseX) {
                clickedToggleIndex = toggleIndex;
                break;
            }
            minX += HORIZONTAL_STRUCTURE_TOGGLE_SPACING;
        }
        if (clickedToggleIndex == -1) {
            return false;
        }
        int structure = toggleStructures[clickedRowIndex * this.structureToggleLocations.getFirst().size() + clickedToggleIndex];
        if (!Configs.ToggledStructures.remove(structure)) {
            Configs.ToggledStructures.add(structure);
        }
        return true;
    }

    @Override
    public void onClose() {
        super.onClose();
        this.arena.close();
        this.tileCache.values().forEach(Tile::close);
        Configs.save();
    }
}
