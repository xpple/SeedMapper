package dev.xpple.seedmapper.seedmap;

import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.Generator;
import com.github.cubiomes.ItemStack;
import com.github.cubiomes.LootTableContext;
import com.github.cubiomes.Piece;
import com.github.cubiomes.Pos;
import com.github.cubiomes.StructureConfig;
import com.github.cubiomes.StructureSaltConfig;
import com.github.cubiomes.StructureVariant;
import com.github.cubiomes.SurfaceNoise;
import dev.xpple.seedmapper.command.commands.LocateCommand;
import dev.xpple.seedmapper.feature.StructureChecks;
import dev.xpple.seedmapper.thread.SeedMapExecutor;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static dev.xpple.seedmapper.util.ChatBuilder.*;

public class LootSearchScreen extends Screen {

    private static final int FIELD_WIDTH = 120;
    private static final int FIELD_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ROW_SPACING = 6;
    private static final int TAB_HEIGHT = 20;
    private static final int TAB_WIDTH = 80;
    private static final int LIST_ROW_HEIGHT = 12;
    private static final int LIST_PADDING = 8;
    private static final int RESULTS_TOP = 44;
    private static final int COLUMN_GAP = 6;
    private static final int STRUCTURE_ICON_SIZE = 16;
    private static final int STRUCTURE_ICON_GAP = 4;
    private static final int STRUCTURE_BUTTON_ROWS = 2;

    private final SeedMapScreen parent;
    private final SeedMapExecutor executor = new SeedMapExecutor();

    private enum Tab {
        SEARCH,
        RESULTS
    }

    private enum SortMode {
        COUNT,
        DISTANCE
    }

    private Tab activeTab = Tab.SEARCH;
    private SortMode structureSortMode = SortMode.COUNT;

    private @Nullable EditBox radiusEditBox;
    private @Nullable Button searchButton;
    private @Nullable Button searchTabButton;
    private @Nullable Button resultsTabButton;
    private @Nullable Button sortToggleButton;
    private @Nullable EditBox resultsSearchEditBox;
    private final List<StructureToggleButton> structureToggleButtons = new ArrayList<>();
    private final IntSet enabledStructureIds = new IntOpenHashSet();
    private final List<MapFeature> lootFeatures;

    private Component status = Component.empty();
    private boolean searching = false;
    private List<ItemResult> itemResults = new ArrayList<>();
    private int resultsScroll = 0;
    private @Nullable ItemResult selectedItem = null;
    private int structureScroll = 0;

    public LootSearchScreen(SeedMapScreen parent) {
        super(Component.translatable("seedMap.lootSearch.title"));
        this.parent = parent;
        this.lootFeatures = Stream.of(MapFeature.values())
            .filter(feature -> LocateCommand.LOOT_SUPPORTED_STRUCTURES.contains(feature.getStructureId()))
            .filter(feature -> feature.getDimension() == this.parent.getDimension())
            .toList();
        this.lootFeatures.forEach(feature -> this.enabledStructureIds.add(feature.getStructureId()));
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int tabsY = 12;
        this.searchTabButton = Button.builder(Component.translatable("seedMap.lootSearch.tab.search"), button -> this.setActiveTab(Tab.SEARCH))
            .bounds(centerX - TAB_WIDTH - 4, tabsY, TAB_WIDTH, TAB_HEIGHT)
            .build();
        this.resultsTabButton = Button.builder(Component.translatable("seedMap.lootSearch.tab.results"), button -> this.setActiveTab(Tab.RESULTS))
            .bounds(centerX + 4, tabsY, TAB_WIDTH, TAB_HEIGHT)
            .build();
        this.resultsTabButton.active = false;
        this.addRenderableWidget(this.searchTabButton);
        this.addRenderableWidget(this.resultsTabButton);

        int startY = this.height / 2 - FIELD_HEIGHT - ROW_SPACING;

        this.radiusEditBox = new EditBox(this.font, centerX - FIELD_WIDTH / 2, startY, FIELD_WIDTH, FIELD_HEIGHT, Component.translatable("seedMap.lootSearch.radius"));
        this.radiusEditBox.setHint(Component.translatable("seedMap.lootSearch.radiusHint"));
        this.radiusEditBox.setMaxLength(7);
        this.radiusEditBox.setValue("2000");
        this.addRenderableWidget(this.radiusEditBox);

        this.searchButton = Button.builder(Component.translatable("seedMap.lootSearch.search"), button -> this.startSearch())
            .bounds(centerX - BUTTON_WIDTH / 2, startY + FIELD_HEIGHT + ROW_SPACING, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();
        this.addRenderableWidget(this.searchButton);

        int iconsTop = startY + FIELD_HEIGHT + ROW_SPACING + BUTTON_HEIGHT + ROW_SPACING;
        int columns = (int) Math.ceil(this.lootFeatures.size() / (double) STRUCTURE_BUTTON_ROWS);
        int totalWidth = columns * STRUCTURE_ICON_SIZE + (columns - 1) * STRUCTURE_ICON_GAP;
        int iconStartX = centerX - totalWidth / 2;
        for (int i = 0; i < this.lootFeatures.size(); i++) {
            int row = i / columns;
            int col = i % columns;
            int x = iconStartX + col * (STRUCTURE_ICON_SIZE + STRUCTURE_ICON_GAP);
            int y = iconsTop + row * (STRUCTURE_ICON_SIZE + STRUCTURE_ICON_GAP);
            MapFeature feature = this.lootFeatures.get(i);
            StructureToggleButton button = new StructureToggleButton(x, y, feature);
            this.structureToggleButtons.add(button);
            this.addRenderableWidget(button);
        }

        int detailsLeft = this.width / 2 + LIST_PADDING;
        int sortButtonY = this.height - LIST_PADDING - TAB_HEIGHT;
        this.sortToggleButton = Button.builder(this.sortButtonLabel(), button -> this.toggleSortMode())
            .bounds(detailsLeft, sortButtonY, TAB_WIDTH + 20, TAB_HEIGHT)
            .build();
        this.addRenderableWidget(this.sortToggleButton);

        int listLeft = LIST_PADDING;
        int listWidth = this.width / 2 - LIST_PADDING * 2;
        int searchY = this.height - LIST_PADDING - FIELD_HEIGHT;
        this.resultsSearchEditBox = new EditBox(this.font, listLeft, searchY, listWidth, FIELD_HEIGHT, Component.translatable("seedMap.lootSearch.filter"));
        this.resultsSearchEditBox.setHint(Component.translatable("seedMap.lootSearch.filterHint"));
        this.addRenderableWidget(this.resultsSearchEditBox);

        this.updateTabVisibility();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (this.activeTab == Tab.SEARCH) {
            this.renderSearchTab(guiGraphics);
        } else {
            this.renderResultsTab(guiGraphics, mouseX, mouseY);
        }
    }

    private void renderSearchTab(GuiGraphics guiGraphics) {
        guiGraphics.drawCenteredString(this.font, this.getTitle(), this.width / 2, this.height / 2 - FIELD_HEIGHT - ROW_SPACING - this.font.lineHeight - 4, 0xFF_FFFFFF);
        if (!this.status.getString().isEmpty()) {
            guiGraphics.drawCenteredString(this.font, this.status, this.width / 2, this.height / 2 + FIELD_HEIGHT + BUTTON_HEIGHT + ROW_SPACING * 2, 0xFF_FFFFFF);
        }
    }

    private void renderResultsTab(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int listLeft = LIST_PADDING;
        int listTop = RESULTS_TOP;
        int listBottom = this.height - LIST_PADDING - FIELD_HEIGHT - ROW_SPACING;
        int listHeight = Math.max(0, listBottom - listTop);

        guiGraphics.drawString(this.font, Component.translatable("seedMap.lootSearch.results"), listLeft, listTop - this.font.lineHeight - 4, 0xFF_FFFFFF);

        List<ItemResult> filteredResults = this.getFilteredResults();
        if (filteredResults.isEmpty()) {
            guiGraphics.drawString(this.font, Component.translatable("seedMap.lootSearch.noResults"), listLeft, listTop, 0xFF_A0A0A0);
            return;
        }

        int visibleRows = Math.max(1, listHeight / LIST_ROW_HEIGHT);
        int maxScroll = Math.max(0, filteredResults.size() - visibleRows);
        this.resultsScroll = Mth.clamp(this.resultsScroll, 0, maxScroll);

        int startIndex = this.resultsScroll;
        int endIndex = Math.min(filteredResults.size(), startIndex + visibleRows);
        int y = listTop;
        for (int i = startIndex; i < endIndex; i++) {
            ItemResult result = filteredResults.get(i);
            int color = Objects.equals(this.selectedItem, result) ? 0xFF_FFFFFF : 0xFF_C0C0C0;
            Component line = Component.literal("%s x%d".formatted(result.displayName(this.parent.getVersion()), result.totalCount));
            guiGraphics.drawString(this.font, line, listLeft, y, color);
            y += LIST_ROW_HEIGHT;
        }

        int gutterShift = Math.min(24, this.width / 20);
        int detailsLeft = this.width / 2 + LIST_PADDING - gutterShift;
        int detailsTop = listTop;
        if (this.selectedItem == null) {
            guiGraphics.drawString(this.font, Component.translatable("seedMap.lootSearch.selectItem"), detailsLeft, detailsTop, 0xFF_A0A0A0);
            return;
        }

        guiGraphics.drawString(this.font, Component.literal(this.selectedItem.displayName(this.parent.getVersion())), detailsLeft, detailsTop, 0xFF_FFFFFF);
        guiGraphics.drawString(this.font, Component.translatable("seedMap.lootSearch.total", this.selectedItem.totalCount), detailsLeft, detailsTop + LIST_ROW_HEIGHT, 0xFF_C0C0C0);

        int structureListTop = detailsTop + LIST_ROW_HEIGHT * 3;
        int structureListHeight = this.height - structureListTop - LIST_PADDING - TAB_HEIGHT - ROW_SPACING;

        List<StructureEntry> structures = this.selectedItem.sortedStructures(this.structureSortMode, this.parent.getPlayerPos());
        if (structures.isEmpty()) {
            guiGraphics.drawString(this.font, Component.translatable("seedMap.lootSearch.noStructures"), detailsLeft, structureListTop, 0xFF_A0A0A0);
            return;
        }

        int detailsRight = this.width - LIST_PADDING;
        int detailsWidth = detailsRight - detailsLeft;
        int countWidth = this.font.width("999999");
        int maxCoordWidth = this.font.width("-30000000");
        int minCoordWidth = this.font.width("0");
        int iconSize = 12;
        int remainingForCoords = detailsWidth - countWidth - iconSize - COLUMN_GAP * 3;
        int coordWidth = Mth.clamp(remainingForCoords / 2, minCoordWidth, maxCoordWidth);

        int xColX = detailsLeft;
        int zColX = xColX + coordWidth + COLUMN_GAP;
        int countColX = zColX + coordWidth + COLUMN_GAP;
        int iconColX = countColX + countWidth + COLUMN_GAP;

        guiGraphics.drawString(this.font, Component.translatable("seedMap.lootSearch.column.x"), xColX, structureListTop - LIST_ROW_HEIGHT, 0xFF_C0C0C0);
        guiGraphics.drawString(this.font, Component.translatable("seedMap.lootSearch.column.z"), zColX, structureListTop - LIST_ROW_HEIGHT, 0xFF_C0C0C0);
        guiGraphics.drawString(this.font, Component.translatable("seedMap.lootSearch.column.count"), countColX, structureListTop - LIST_ROW_HEIGHT, 0xFF_C0C0C0);
        guiGraphics.drawString(this.font, Component.translatable("seedMap.lootSearch.column.type"), iconColX, structureListTop - LIST_ROW_HEIGHT, 0xFF_C0C0C0);

        int structureVisibleRows = Math.max(1, structureListHeight / LIST_ROW_HEIGHT);
        int structureMaxScroll = Math.max(0, structures.size() - structureVisibleRows);
        this.structureScroll = Mth.clamp(this.structureScroll, 0, structureMaxScroll);
        int structureStartIndex = this.structureScroll;
        int structureEndIndex = Math.min(structures.size(), structureStartIndex + structureVisibleRows);
        Component hoverTooltip = null;
        int structureY = structureListTop;
        for (int i = structureStartIndex; i < structureEndIndex; i++) {
            StructureEntry entry = structures.get(i);
            guiGraphics.drawString(this.font, Component.literal(Integer.toString(entry.pos.getX())), xColX, structureY, 0xFF_FFFFFF);
            guiGraphics.drawString(this.font, Component.literal(Integer.toString(entry.pos.getZ())), zColX, structureY, 0xFF_FFFFFF);
            guiGraphics.drawString(this.font, Component.literal(Integer.toString(entry.count)), countColX, structureY, 0xFF_FFFFFF);
            MapFeature.Texture texture = this.getStructureTexture(entry.structureId);
            int iconX = iconColX;
            int iconY = structureY + (LIST_ROW_HEIGHT - iconSize) / 2;
            if (texture != null) {
                SeedMapScreen.drawIconStatic(guiGraphics, texture.identifier(), iconX, iconY, iconSize, iconSize, 0xFF_FFFFFF);
            } else {
            guiGraphics.drawString(this.font, Component.literal("?"), iconX, structureY, 0xFF_FFFFFF);
            }
            if (mouseX >= iconX && mouseX <= iconX + iconSize && mouseY >= iconY && mouseY <= iconY + iconSize) {
                hoverTooltip = Component.literal(Cubiomes.struct2str(entry.structureId).getString(0));
            }
            structureY += LIST_ROW_HEIGHT;
        }
        if (hoverTooltip != null) {
            guiGraphics.renderTooltip(this.font, List.of(ClientTooltipComponent.create(hoverTooltip.getVisualOrderText())), mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
        }

    }

    private void startSearch() {
        if (this.searching || this.radiusEditBox == null) {
            return;
        }
        int radius;
        try {
            radius = Integer.parseInt(this.radiusEditBox.getValue());
        } catch (NumberFormatException _) {
        this.status = error(Component.translatable("seedMap.lootSearch.error.invalidRadius").getString());
        return;
        }
        if (radius <= 0 || radius > Level.MAX_LEVEL_SIZE) {
        this.status = error(Component.translatable("seedMap.lootSearch.error.radiusRange").getString());
        return;
        }

        this.searching = true;
        this.status = base(Component.translatable("seedMap.lootSearch.searching").getString());
        if (this.searchButton != null) {
            this.searchButton.active = false;
        }
        if (this.resultsTabButton != null) {
            this.resultsTabButton.active = false;
        }

        int clampedRadius = radius;
        this.executor.submitCalculation(() -> this.searchLoot(clampedRadius))
            .thenAccept(result -> {
                Minecraft.getInstance().schedule(() -> {
                    if (result == null) {
                        this.status = error(Component.translatable("seedMap.lootSearch.error.searchFailed").getString());
                        this.itemResults = new ArrayList<>();
                        this.selectedItem = null;
                    } else if (result.items.isEmpty()) {
                        this.status = warn(Component.translatable("seedMap.lootSearch.noLoot").getString());
                        this.itemResults = new ArrayList<>();
                        this.selectedItem = null;
                    } else {
                        this.status = base(Component.translatable("seedMap.lootSearch.foundTypes", result.items.size()).getString());
                        this.itemResults = result.items.values().stream()
                            .sorted((a, b) -> Integer.compare(b.totalCount, a.totalCount))
                            .toList();
                        this.resultsScroll = 0;
                        this.structureScroll = 0;
                        this.selectedItem = this.itemResults.getFirst();
                        if (this.resultsTabButton != null) {
                            this.resultsTabButton.active = true;
                        }
                        this.setActiveTab(Tab.RESULTS);
                    }
                    this.searching = false;
                    if (this.searchButton != null) {
                        this.searchButton.active = true;
                    }
                    if (this.resultsTabButton != null && !this.itemResults.isEmpty()) {
                        this.resultsTabButton.active = true;
                    }
                });
            });
    }

    private @Nullable SearchResults searchLoot(int radiusBlocks) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment generator = Generator.allocate(arena);
            Cubiomes.setupGenerator(generator, this.parent.getVersion(), this.parent.getGeneratorFlags());
            Cubiomes.applySeed(generator, this.parent.getDimension(), this.parent.getSeed());

            MemorySegment surfaceNoise = SurfaceNoise.allocate(arena);
            Cubiomes.initSurfaceNoise(surfaceNoise, this.parent.getDimension(), this.parent.getSeed());

            SearchResults results = new SearchResults();
            MemorySegment structurePos = Pos.allocate(arena);
            MemorySegment pieces = Piece.allocateArray(StructureChecks.MAX_END_CITY_AND_FORTRESS_PIECES, arena);
            MemorySegment structureVariant = StructureVariant.allocate(arena);
            MemorySegment structureSaltConfig = StructureSaltConfig.allocate(arena);
            MemorySegment ltcPtr = arena.allocate(Cubiomes.C_POINTER);

            int centerX = this.parent.getPlayerPos().getX();
            int centerZ = this.parent.getPlayerPos().getZ();
            int radiusSq = radiusBlocks * radiusBlocks;

            for (int structure : LocateCommand.LOOT_SUPPORTED_STRUCTURES) {
                if (!this.enabledStructureIds.contains(structure)) {
                    continue;
                }
                MemorySegment structureConfig = StructureConfig.allocate(arena);
                if (Cubiomes.getStructureConfig(structure, this.parent.getVersion(), structureConfig) == 0) {
                    continue;
                }
                if (StructureConfig.dim(structureConfig) != this.parent.getDimension()) {
                    continue;
                }
                int regionSizeBlocks = StructureConfig.regionSize(structureConfig) << 4;
                int minRegionX = Mth.floor((centerX - radiusBlocks) / (float) regionSizeBlocks);
                int maxRegionX = Mth.floor((centerX + radiusBlocks) / (float) regionSizeBlocks);
                int minRegionZ = Mth.floor((centerZ - radiusBlocks) / (float) regionSizeBlocks);
                int maxRegionZ = Mth.floor((centerZ + radiusBlocks) / (float) regionSizeBlocks);

                StructureChecks.GenerationCheck generationCheck = StructureChecks.getGenerationCheck(structure);
                for (int regionX = minRegionX; regionX <= maxRegionX; regionX++) {
                    for (int regionZ = minRegionZ; regionZ <= maxRegionZ; regionZ++) {
                        if (Cubiomes.getStructurePos(structure, this.parent.getVersion(), this.parent.getSeed(), regionX, regionZ, structurePos) == 0) {
                            continue;
                        }
                        int posX = Pos.x(structurePos);
                        int posZ = Pos.z(structurePos);
                        int dx = posX - centerX;
                        int dz = posZ - centerZ;
                        if (dx * dx + dz * dz > radiusSq) {
                            continue;
                        }
                        if (!generationCheck.check(generator, surfaceNoise, regionX, regionZ, structurePos)) {
                            continue;
                        }
                        int biome = Cubiomes.getBiomeAt(generator, 4, posX >> 2, 320 >> 2, posZ >> 2);
                        Cubiomes.getVariant(structureVariant, structure, this.parent.getVersion(), this.parent.getSeed(), posX, posZ, biome);
                        biome = StructureVariant.biome(structureVariant) != -1 ? StructureVariant.biome(structureVariant) : biome;
                        if (Cubiomes.getStructureSaltConfig(structure, this.parent.getVersion(), biome, structureSaltConfig) == 0) {
                            continue;
                        }
                        int numPieces = Cubiomes.getStructurePieces(pieces, StructureChecks.MAX_END_CITY_AND_FORTRESS_PIECES, structure, structureSaltConfig, structureVariant, this.parent.getVersion(), this.parent.getSeed(), posX, posZ);
                        if (numPieces <= 0) {
                            continue;
                        }
                        for (int pieceIdx = 0; pieceIdx < numPieces; pieceIdx++) {
                            MemorySegment piece = Piece.asSlice(pieces, pieceIdx);
                            int chestCount = Piece.chestCount(piece);
                            if (chestCount <= 0) {
                                continue;
                            }
                            MemorySegment lootTables = Piece.lootTables(piece);
                            MemorySegment lootSeeds = Piece.lootSeeds(piece);
                            for (int chestIdx = 0; chestIdx < chestCount; chestIdx++) {
                                MemorySegment lootTable = lootTables.getAtIndex(ValueLayout.ADDRESS, chestIdx).reinterpret(Long.MAX_VALUE);
                                if (Cubiomes.init_loot_table_name(ltcPtr, lootTable, this.parent.getVersion()) == 0) {
                                    continue;
                                }
                                MemorySegment lootTableContext = ltcPtr.get(ValueLayout.ADDRESS, 0).reinterpret(LootTableContext.sizeof());
                                Cubiomes.set_loot_seed(lootTableContext, lootSeeds.getAtIndex(Cubiomes.C_LONG_LONG, chestIdx));
                                Cubiomes.generate_loot(lootTableContext);
                                int lootCount = LootTableContext.generated_item_count(lootTableContext);
                                for (int lootIdx = 0; lootIdx < lootCount; lootIdx++) {
                                    MemorySegment itemStackInternal = ItemStack.asSlice(LootTableContext.generated_items(lootTableContext), lootIdx);
                                    int itemId = Cubiomes.get_global_item_id(lootTableContext, ItemStack.item(itemStackInternal));
                                    int count = ItemStack.count(itemStackInternal);
                                    results.addItem(itemId, count, new BlockPos(posX, 0, posZ), structure);
                                }
                            }
                        }
                    }
                }
            }
            return results;
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        this.executor.close(() -> {});
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (super.mouseClicked(event, isDoubleClick)) {
            return true;
        }
        if (this.activeTab != Tab.RESULTS || isDoubleClick) {
            return false;
        }
        int mouseX = (int)event.x();
        int mouseY = (int)event.y();
        int listLeft = LIST_PADDING;
        int listTop = RESULTS_TOP;
        int listWidth = this.width / 2 - LIST_PADDING * 2;
        int listBottom = this.height - LIST_PADDING - FIELD_HEIGHT - ROW_SPACING;
        int listRight = listLeft + listWidth;
        //int listBottom = listTop + listHeight;
        if (mouseX < listLeft || mouseX > listRight || mouseY < listTop || mouseY > listBottom) {
            return false;
        }
        List<ItemResult> filteredResults = this.getFilteredResults();
        int index = this.resultsScroll + (mouseY - listTop) / LIST_ROW_HEIGHT;
        if (index >= 0 && index < filteredResults.size()) {
            this.selectedItem = filteredResults.get(index);
            this.structureScroll = 0;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.activeTab != Tab.RESULTS || this.itemResults.isEmpty()) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        int listLeft = LIST_PADDING;
        int listTop = RESULTS_TOP;
        int listWidth = this.width / 2 - LIST_PADDING * 2;
        int listBottom = this.height - LIST_PADDING - FIELD_HEIGHT - ROW_SPACING;
        int listHeight = Math.max(0, listBottom - listTop);
        int listRight = listLeft + listWidth;
        //int listBottom = listTop + listHeight;
        int delta = scrollY > 0 ? -1 : 1;

        if (mouseX >= listLeft && mouseX <= listRight && mouseY >= listTop && mouseY <= listBottom) {
            int visibleRows = Math.max(1, listHeight / LIST_ROW_HEIGHT);
            int maxScroll = Math.max(0, this.getFilteredResults().size() - visibleRows);
            this.resultsScroll = Mth.clamp(this.resultsScroll + delta, 0, maxScroll);
            return true;
        }

        int gutterShift = Math.min(24, this.width / 20);
        int detailsLeft = this.width / 2 + LIST_PADDING - gutterShift;
        int structureListTop = listTop + LIST_ROW_HEIGHT * 3;
        int structureListHeight = this.height - structureListTop - LIST_PADDING - TAB_HEIGHT - ROW_SPACING;
        int structureListRight = this.width - LIST_PADDING;
        int structureListBottom = structureListTop + structureListHeight;
        if (mouseX >= detailsLeft && mouseX <= structureListRight && mouseY >= structureListTop && mouseY <= structureListBottom && this.selectedItem != null) {
            List<StructureEntry> structures = this.selectedItem.sortedStructures(this.structureSortMode, this.parent.getPlayerPos());
            int visibleRows = Math.max(1, structureListHeight / LIST_ROW_HEIGHT);
            int maxScroll = Math.max(0, structures.size() - visibleRows);
            this.structureScroll = Mth.clamp(this.structureScroll + delta, 0, maxScroll);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void setActiveTab(Tab tab) {
        this.activeTab = tab;
        this.updateTabVisibility();
    }

    private void updateTabVisibility() {
        boolean searchVisible = this.activeTab == Tab.SEARCH;
        if (this.radiusEditBox != null) {
            this.radiusEditBox.visible = searchVisible;
        }
        if (this.searchButton != null) {
            this.searchButton.visible = searchVisible;
        }
        if (this.searchTabButton != null) {
            this.searchTabButton.active = this.activeTab != Tab.SEARCH;
        }
        if (this.resultsTabButton != null) {
            boolean hasResults = !this.itemResults.isEmpty();
            this.resultsTabButton.active = this.activeTab != Tab.RESULTS && hasResults;
        }
        if (this.sortToggleButton != null) {
            this.sortToggleButton.visible = this.activeTab == Tab.RESULTS;
            this.sortToggleButton.active = this.activeTab == Tab.RESULTS && !this.itemResults.isEmpty();
            this.sortToggleButton.setMessage(this.sortButtonLabel());
        }
        if (this.resultsSearchEditBox != null) {
            this.resultsSearchEditBox.visible = this.activeTab == Tab.RESULTS;
            this.resultsSearchEditBox.setValue(this.resultsSearchEditBox.getValue());
        }
        boolean showSearchControls = this.activeTab == Tab.SEARCH;
        for (StructureToggleButton button : this.structureToggleButtons) {
            button.visible = showSearchControls;
            button.active = showSearchControls;
        }
    }

    private static final class SearchResults {
        private final Map<Integer, ItemResult> items = new HashMap<>();

        void addItem(int itemId, int count, BlockPos pos, int structureId) {
            ItemResult result = this.items.computeIfAbsent(itemId, ItemResult::new);
            result.totalCount += count;
            result.addPosition(pos);
            result.addStructureEntry(structureId, pos, count);
        }
    }

    private static final class ItemResult {
        private final int itemId;
        private int totalCount = 0;
        private final List<BlockPos> positions = new ArrayList<>();
        private final Map<StructureKey, StructureEntry> structureEntries = new HashMap<>();

        private ItemResult(int itemId) {
            this.itemId = itemId;
        }

        private void addPosition(BlockPos pos) {
            if (this.positions.isEmpty() || !this.positions.getLast().equals(pos)) {
                this.positions.add(pos);
            }
        }

        private void addStructureEntry(int structureId, BlockPos pos, int count) {
            StructureKey key = new StructureKey(structureId, pos);
            StructureEntry entry = this.structureEntries.computeIfAbsent(key, StructureEntry::new);
            entry.count += count;
        }

        private List<StructureEntry> sortedStructures(SortMode sortMode, BlockPos playerPos) {
            return this.structureEntries.values().stream()
                .sorted((a, b) -> {
                    if (sortMode == SortMode.COUNT) {
                        int byCount = Integer.compare(b.count, a.count);
                        if (byCount != 0) {
                            return byCount;
                        }
                        return Long.compare(a.distanceSq(playerPos), b.distanceSq(playerPos));
                    }
                    int byDistance = Long.compare(a.distanceSq(playerPos), b.distanceSq(playerPos));
                    if (byDistance != 0) {
                        return byDistance;
                    }
                    return Integer.compare(b.count, a.count);
                })
                .toList();
        }

        private String displayName(int version) {
            String name = Cubiomes.global_id2item_name(this.itemId, version).getString(0);
            return name.contains(":") ? name.substring(name.indexOf(':') + 1) : name;
        }
    }

    private void toggleSortMode() {
        this.structureSortMode = this.structureSortMode == SortMode.COUNT ? SortMode.DISTANCE : SortMode.COUNT;
        this.structureScroll = 0;
        if (this.sortToggleButton != null) {
            this.sortToggleButton.setMessage(this.sortButtonLabel());
        }
    }

    private Component sortButtonLabel() {
        return Component.translatable("seedMap.lootSearch.sort", Component.translatable(this.structureSortMode == SortMode.COUNT
            ? "seedMap.lootSearch.sort.count"
            : "seedMap.lootSearch.sort.distance"));
    }

    private @Nullable MapFeature.Texture getStructureTexture(int structureId) {
        for (MapFeature feature : MapFeature.values()) {
            if (feature.getStructureId() == structureId) {
                return feature.getDefaultTexture();
            }
        }
        return null;
    }

    private List<ItemResult> getFilteredResults() {
        if (this.resultsSearchEditBox == null) {
            return this.itemResults;
        }
        String query = this.resultsSearchEditBox.getValue().trim().toLowerCase();
        if (query.isEmpty()) {
            return this.itemResults;
        }
        return this.itemResults.stream()
            .filter(result -> result.displayName(this.parent.getVersion()).toLowerCase().contains(query))
            .toList();
    }

    private record StructureKey(int structureId, BlockPos pos) {}

    private static final class StructureEntry {
        private final int structureId;
        private final BlockPos pos;
        private int count = 0;

        private StructureEntry(StructureKey key) {
            this.structureId = key.structureId();
            this.pos = key.pos();
        }

        private long distanceSq(BlockPos playerPos) {
            long dx = (long) this.pos.getX() - playerPos.getX();
            long dz = (long) this.pos.getZ() - playerPos.getZ();
            return dx * dx + dz * dz;
        }
    }

    private final class StructureToggleButton extends Button {
        private final MapFeature feature;

        private StructureToggleButton(int x, int y, MapFeature feature) {
            super(x, y, STRUCTURE_ICON_SIZE, STRUCTURE_ICON_SIZE, Component.literal(feature.getName()), (Button button) -> {
                if (!(button instanceof StructureToggleButton toggle)) return;
                toggle.onPress();
            }, DEFAULT_NARRATION);
            this.feature = feature;
            this.setTooltip(Tooltip.create(Component.literal(this.feature.getName())));
        }

        private void onPress() {
            int structureId = this.feature.getStructureId();
            if (!enabledStructureIds.remove(structureId)) {
                enabledStructureIds.add(structureId);
            }
        }

        @Override
        protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            int color = enabledStructureIds.contains(this.feature.getStructureId()) ? 0xFF_FFFFFF : 0x80_FFFFFF;
            MapFeature.Texture texture = this.feature.getDefaultTexture();
            SeedMapScreen.drawIconStatic(guiGraphics, texture.identifier(), this.getX(), this.getY(), this.getWidth(), this.getHeight(), color);
        }
    }
}
