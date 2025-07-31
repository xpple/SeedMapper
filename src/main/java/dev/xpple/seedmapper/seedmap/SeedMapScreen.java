package dev.xpple.seedmapper.seedmap;

import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.Generator;
import com.github.cubiomes.Pos;
import com.github.cubiomes.Range;
import com.github.cubiomes.StructureConfig;
import com.github.cubiomes.SurfaceNoise;
import com.google.common.base.Suppliers;
import dev.xpple.seedmapper.config.Configs;
import dev.xpple.seedmapper.feature.StructureChecks;
import dev.xpple.seedmapper.util.QuartPos2;
import dev.xpple.seedmapper.util.RegionPos;
import dev.xpple.seedmapper.util.SpiralLoop;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
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
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static dev.xpple.seedmapper.util.ChatBuilder.*;

public class SeedMapScreen extends Screen {

    private static final int BIOME_SCALE = 4;
    private static final int SCALED_CHUNK_SIZE = LevelChunkSection.SECTION_WIDTH / BIOME_SCALE;

    private static final int HORIZONTAL_PADDING = 50;
    private static final int VERTICAL_PADDING = 50;

    public static final int MIN_PIXELS_PER_BIOME = 1;
    public static final int MAX_PIXELS_PER_BIOME = 100;

    private static final int HORIZONTAL_STRUCTURE_TOGGLE_SPACING = 5;
    private static final int VERTICAL_STRUCTURE_TOGGLE_SPACING = 1;
    private static final int STRUCTURE_TOGGLE_HEIGHT = 20;

    private static final int[] toggleStructures = StructureData.Structure.STRUCTURE_ICONS.keySet().intStream().sorted().toArray();

    private static final int STRUCTURE_ICONS_COMBINED_WIDTH = StructureData.Structure.STRUCTURE_ICONS.values().stream()
        .map(StructureData.Structure.Texture::width)
        .collect(() -> new MutableInt(0), MutableInt::add, (l, r) -> l.add(r.addAndGet(HORIZONTAL_STRUCTURE_TOGGLE_SPACING))).intValue();

    private static final Long2ObjectMap<Int2ObjectMap<Object2ObjectMap<ChunkPos, ChunkData>>> dataCache = new Long2ObjectOpenHashMap<>();

    private final long seed;
    private final int dimension;
    private final int version;

    private final BlockPos playerPos;

    private QuartPos2 centerQuart;

    private int centerX;
    private int centerY;
    private int seedMapWidth;
    private int seedMapHeight;
    private int seedMapStartX;
    private int seedMapStartY;
    private int horChunkRadius;
    private int verChunkRadius;

    private List<List<StructureData.Structure.Texture>> structureToggleLocations;

    private DynamicTexture texture;

    private QuartPos2 mouseQuart;

    private int displayCoordinatesCopiedTicks = 0;

    public SeedMapScreen(long seed, int dimension, int version, BlockPos playerPos) {
        super(Component.empty());
        this.seed = seed;
        this.dimension = dimension;
        this.version = version;

        this.playerPos = playerPos;

        this.centerQuart = QuartPos2.fromBlockPos(playerPos);
        this.mouseQuart = new QuartPos2(this.centerQuart.x(), this.centerQuart.z());
    }

    @Override
    protected void init() {
        super.init();

        this.centerX = this.width / 2;
        this.centerY = this.height / 2;

        this.seedMapStartX = this.centerX - HORIZONTAL_PADDING;
        this.seedMapStartY = this.centerY - VERTICAL_PADDING;

        this.seedMapWidth = 2 * this.seedMapStartX;
        this.seedMapHeight = 2 * this.seedMapStartY;

        this.texture = new DynamicTexture("Seed Map " + this.seed, this.seedMapWidth, this.seedMapHeight, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // draw seed
        Component seedComponent = Component.translatable("seedMap.seed", accent(Long.toString(this.seed)));
        guiGraphics.drawString(this.font, seedComponent, HORIZONTAL_PADDING, VERTICAL_PADDING - this.font.lineHeight - 1, -1);

        this.structureToggleLocations = this.drawStructureToggles(guiGraphics);

        this.horChunkRadius = Math.ceilDiv(this.seedMapStartX, SCALED_CHUNK_SIZE * Configs.PixelsPerBiome);
        this.verChunkRadius = Math.ceilDiv(this.seedMapStartY, SCALED_CHUNK_SIZE * Configs.PixelsPerBiome);

        Int2ObjectMap<Object2ObjectMap<ChunkPos, ChunkData>> dimensionCache = dataCache.computeIfAbsent(this.seed, _ -> new Int2ObjectArrayMap<>(3));
        Object2ObjectMap<ChunkPos, ChunkData> chunkCache = dimensionCache.computeIfAbsent(this.dimension, _ -> new Object2ObjectOpenHashMap<>());
        try (Arena arena = Arena.ofConfined()) {
            // TODO: cache?
            Supplier<MemorySegment> generatorSupplier = Suppliers.memoize(() -> {
                MemorySegment generator = Generator.allocate(arena);
                Cubiomes.setupGenerator(generator, this.version, 0);
                Cubiomes.applySeed(generator, this.dimension, this.seed);
                return generator;
            });

            ChunkPos centerChunk = this.centerQuart.toChunkPos();

            // first pass, calculate biomes
            SpiralLoop.spiral(centerChunk.x, centerChunk.z, Math.max(this.horChunkRadius, this.verChunkRadius), (chunkX, chunkZ) -> {
                ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
                int relChunkX = chunkPos.x - centerChunk.x;
                int relChunkZ = chunkPos.z - centerChunk.z;
                if (relChunkX > this.horChunkRadius || relChunkX < -this.horChunkRadius || relChunkZ > this.verChunkRadius || relChunkZ < -this.verChunkRadius) {
                    return false;
                }

                ChunkData chunkData = chunkCache.computeIfAbsent(chunkPos, _ -> calculateBiomeData(chunkPos, arena, generatorSupplier));

                for (QuartData quartData : chunkData.quartDataList()) {
                    QuartPos2 relQuartPos = quartData.quartPos().subtract(this.centerQuart);
                    int minX = this.seedMapStartX + Configs.PixelsPerBiome * relQuartPos.x();
                    int minY = this.seedMapStartY + Configs.PixelsPerBiome * relQuartPos.z();
                    this.fillRect(minX, minY, minX + Configs.PixelsPerBiome, minY + Configs.PixelsPerBiome, quartData.biomeColour());
                }

                return false;
            });

            this.texture.upload();
            guiGraphics.submitBlit(RenderPipelines.GUI_TEXTURED, this.texture.getTextureView(), HORIZONTAL_PADDING, VERTICAL_PADDING, HORIZONTAL_PADDING + this.seedMapWidth, VERTICAL_PADDING + this.seedMapHeight, 0, 1, 0, 1, -1);

            // second pass, calculate structures
            MemorySegment surfaceNoise = SurfaceNoise.allocate(arena);
            Cubiomes.initSurfaceNoise(surfaceNoise, this.dimension, this.seed);
            Configs.ToggledStructures.forEach(structure -> {
                MemorySegment structureConfig = StructureConfig.allocate(arena);
                if (Cubiomes.getStructureConfig(structure, this.version, structureConfig) == 0) {
                    return;
                }
                if (StructureConfig.dim(structureConfig) != this.dimension) {
                    return;
                }
                int regionSize = StructureConfig.regionSize(structureConfig);
                RegionPos centerRegion = RegionPos.fromChunkPos(centerChunk, regionSize);
                int horRegionRadius = Math.ceilDiv(this.horChunkRadius, regionSize);
                int verRegionRadius = Math.ceilDiv(this.verChunkRadius, regionSize);
                // currently only used for end cities
                StructureChecks.GenerationCheck generationCheck = StructureChecks.getGenerationCheck(structure);
                MemorySegment structurePos = Pos.allocate(arena);
                SpiralLoop.spiral(centerRegion.x(), centerRegion.z(), Math.max(horRegionRadius, verRegionRadius), (regionX, regionZ) -> {
                    RegionPos regionPos = new RegionPos(regionX, regionZ, regionSize);
                    RegionPos relRegion = regionPos.subtract(centerRegion);
                    if (relRegion.x() > horRegionRadius || relRegion.x() < -horRegionRadius || relRegion.z() > verRegionRadius || relRegion.z() < -verRegionRadius) {
                        return false;
                    }
                    if (Cubiomes.getStructurePos(structure, this.version, this.seed, regionPos.x(), regionPos.z(), structurePos) == 0) {
                        return false;
                    }
                    ChunkPos chunkPos = new ChunkPos(SectionPos.blockToSectionCoord(Pos.x(structurePos)), SectionPos.blockToSectionCoord(Pos.z(structurePos)));

                    ChunkData chunkData = chunkCache.get(chunkPos);
                    if (chunkData == null) {
                        return false;
                    }
                    StructureData.Structure structureData = chunkData.structureData().structures().computeIfAbsent(structure, _ -> calculateStructurePos(structure, regionPos, structurePos, generationCheck, generatorSupplier, surfaceNoise));
                    if (structureData == null) {
                        return false;
                    }
                    StructureData.Structure.Texture structureIcon = structureData.texture();
                    QuartPos2 relQuartPos = QuartPos2.fromBlockPos(structureData.blockPos()).subtract(this.centerQuart);
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
        }

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

    private static ChunkData calculateBiomeData(ChunkPos chunkPos, Arena arena, Supplier<MemorySegment> generatorSupplier) {
        MemorySegment generator = generatorSupplier.get();
        int minXQuart = QuartPos.fromBlock(chunkPos.getMinBlockX());
        int minZQuart = QuartPos.fromBlock(chunkPos.getMinBlockZ());

        MemorySegment range = Range.allocate(arena);
        Range.scale(range, BIOME_SCALE);
        Range.x(range, minXQuart);
        Range.z(range, minZQuart);
        Range.sx(range, SCALED_CHUNK_SIZE);
        Range.sz(range, SCALED_CHUNK_SIZE);
        Range.y(range, 63 / Range.scale(range)); // sea level
        Range.sy(range, 1);

        long size = Cubiomes.getMinCacheSize(generator, Range.scale(range), Range.sx(range), Range.sy(range), Range.sz(range));
        MemorySegment biomeIds = arena.allocate(Cubiomes.C_INT, size);
        Cubiomes.genBiomes(generator, biomeIds, range);

        List<QuartData> quartDataList = new ArrayList<>(SCALED_CHUNK_SIZE * SCALED_CHUNK_SIZE);

        for (int relX = 0; relX < SCALED_CHUNK_SIZE; relX++) {
            for (int relZ = 0; relZ < SCALED_CHUNK_SIZE; relZ++) {
                long index = relX + relZ * (long) Range.sx(range);
                int biome = biomeIds.getAtIndex(Cubiomes.C_INT, index);
                QuartPos2 quartPos = new QuartPos2(minXQuart + relX, minZQuart + relZ);
                QuartData quartData = new QuartData(quartPos, biome);
                quartDataList.add(quartData);
            }
        }

        return new ChunkData(chunkPos, quartDataList, new StructureData(chunkPos, new Int2ObjectOpenHashMap<>()));
    }

    private static @Nullable StructureData.Structure calculateStructurePos(int structure, RegionPos regionPos, MemorySegment structurePos, StructureChecks.GenerationCheck generationCheck, Supplier<MemorySegment> generatorSupplier, MemorySegment surfaceNoise) {
        MemorySegment generator = generatorSupplier.get();

        if (!generationCheck.check(generator, surfaceNoise, regionPos.x(), regionPos.z(), structurePos)) {
            return null;
        }

        BlockPos blockPos = new BlockPos(Pos.x(structurePos), 0, Pos.z(structurePos));
        return new StructureData.Structure(structure, blockPos);
    }

    private void fillRect(int minX, int minY, int maxX, int maxY, int colour) {
        if (minX >= this.seedMapWidth || minY >= this.seedMapHeight) {
            return;
        }
        if (maxX < 0 || maxY < 0) {
            return;
        }

        minX = Mth.clamp(minX, 0, this.seedMapWidth);
        minY = Mth.clamp(minY, 0, this.seedMapHeight);
        maxX = Mth.clamp(maxX, 0, this.seedMapWidth);
        maxY = Mth.clamp(maxY, 0, this.seedMapHeight);

        this.texture.getPixels().fillRect(minX, minY, maxX - minX, maxY - minY, colour);
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
        if (mouseX >= HORIZONTAL_PADDING && mouseX <= HORIZONTAL_PADDING + this.seedMapWidth && mouseY >= VERTICAL_PADDING && mouseY <= VERTICAL_PADDING + this.seedMapHeight) {
            // handle click on seed map
            this.minecraft.keyboardHandler.setClipboard("%d ~ %d".formatted(QuartPos.toBlock(this.mouseQuart.x()), QuartPos.toBlock(this.mouseQuart.z())));
            this.displayCoordinatesCopiedTicks = SharedConstants.TICKS_PER_SECOND;
            return true;
        }

        if (mouseX >= HORIZONTAL_PADDING && mouseX <= HORIZONTAL_PADDING + this.seedMapWidth && mouseY >= 1 && mouseY <= VERTICAL_PADDING) {
            // handle structure toggle
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
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        if (this.texture != null) {
            this.texture.close();
        }
        Configs.save();
    }
}
