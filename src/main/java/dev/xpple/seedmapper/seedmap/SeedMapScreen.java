package dev.xpple.seedmapper.seedmap;

import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.ItemStack;
import com.github.cubiomes.LootTableContext;
import com.github.cubiomes.Piece;
import com.github.cubiomes.Pos;
import com.github.cubiomes.StructureSaltConfig;
import com.github.cubiomes.StructureVariant;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.seedmapper.command.commands.LocateCommand;
import dev.xpple.seedmapper.config.Configs;
import dev.xpple.seedmapper.feature.StructureChecks;
import dev.xpple.seedmapper.util.ComponentUtils;
import dev.xpple.seedmapper.util.CubiomesHelper;
import dev.xpple.seedmapper.util.QuartPos2;
import dev.xpple.seedmapper.util.QuartPos2f;
import dev.xpple.seedmapper.util.SeedIdentifierWithDimension;
import dev.xpple.simplewaypoints.api.SimpleWaypointsAPI;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Gatherers;

import static dev.xpple.seedmapper.util.ChatBuilder.*;

public class SeedMapScreen extends Screen {

    private static final int HORIZONTAL_PADDING = 50;
    private static final int VERTICAL_PADDING = 50;

    private static final int HORIZONTAL_FEATURE_TOGGLE_SPACING = 5;
    private static final int VERTICAL_FEATURE_TOGGLE_SPACING = 1;
    private static final int FEATURE_TOGGLE_HEIGHT = 20;

    private static final int TELEPORT_FIELD_WIDTH = 70;
    private static final int WAYPOINT_NAME_FIELD_WIDTH = 100;

    private final SeedMapRenderer seedMapRenderer;

    private final int featureIconsCombinedWidth;

    private QuartPos2 mouseQuart;

    private int displayCoordinatesCopiedTicks = 0;

    @UnknownNullability
    private EditBox teleportEditBoxX;
    @UnknownNullability
    private EditBox teleportEditBoxZ;

    @UnknownNullability
    private EditBox waypointNameEditBox;

    private @Nullable SeedMapRenderer.FeatureWidget markerWidget = null;
    private @Nullable ChestLootWidget chestLootWidget = null;

    private final Registry<Enchantment> enchantmentsRegistry;
    private final Registry<MobEffect> mobEffectRegistry;

    public SeedMapScreen(SeedIdentifierWithDimension seedIdentifierWithDimension, BlockPos playerPos, Vec2 playerRotation) {
        super(Component.empty());
        this.seedMapRenderer = new SeedMapRenderer(seedIdentifierWithDimension, playerPos, playerRotation, this::horizontalPadding, this::verticalPadding);

        this.enchantmentsRegistry = this.minecraft.player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        this.mobEffectRegistry = this.minecraft.player.registryAccess().lookupOrThrow(Registries.MOB_EFFECT);

        this.featureIconsCombinedWidth = this.seedMapRenderer.getSeedMapData().getAvailableFeatures().stream()
            .map(feature -> feature.getDefaultTexture().width())
            .reduce((l, r) -> l + HORIZONTAL_FEATURE_TOGGLE_SPACING + r)
            .orElseThrow();

        this.mouseQuart = QuartPos2.fromBlockPos(playerPos);
    }

    @Override
    protected void init() {
        super.init();

        this.seedMapRenderer.centerX = this.width / 2;
        this.seedMapRenderer.centerY = this.height / 2;

        this.seedMapRenderer.seedMapWidth = 2 * (this.seedMapRenderer.centerX - this.horizontalPadding());
        this.seedMapRenderer.seedMapHeight = 2 * (this.seedMapRenderer.centerY - this.verticalPadding());

        this.createFeatureToggles();
        this.createTeleportField();
        this.createWaypointNameField();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);

        SeedIdentifierWithDimension seedIdentifierWithDimension = this.seedMapRenderer.getSeedMapData().getSeedIdentifierWithDimension();
        Component seedComponent = Component.translatable("seedMap.seed", accent(Long.toString(seedIdentifierWithDimension.seed())), Cubiomes.mc2str(seedIdentifierWithDimension.version()).getString(0), ComponentUtils.formatGeneratorFlags(seedIdentifierWithDimension.generatorFlags()));
        guiGraphicsExtractor.text(this.font, seedComponent, this.horizontalPadding(), this.verticalPadding() - this.font.lineHeight - 1, -1);

        this.seedMapRenderer.renderBiomes(guiGraphicsExtractor, mouseX, mouseY, partialTick);
        guiGraphicsExtractor.nextStratum();
        this.seedMapRenderer.renderFeatures(guiGraphicsExtractor, mouseX, mouseY, partialTick);
        guiGraphicsExtractor.nextStratum();
        this.seedMapRenderer.drawPlayerIndicator(guiGraphicsExtractor);
        guiGraphicsExtractor.nextStratum();
        if (this.markerWidget != null && this.markerWidget.withinBounds()) {
            this.markerWidget.render(guiGraphicsExtractor, -1);
        }
        if (this.chestLootWidget != null) {
            this.chestLootWidget.render(guiGraphicsExtractor, mouseX, mouseY, this.font);
        }

        // draw hovered coordinates and biome
        MutableComponent coordinates = accent("x: %d, y: %d, z: %d".formatted(QuartPos.toBlock(this.mouseQuart.x()), this.seedMapRenderer.getSeedMapData().getBiomeYHeight(), QuartPos.toBlock(this.mouseQuart.z())));
        OptionalInt optionalBiome = this.seedMapRenderer.getSeedMapData().getBiome(this.mouseQuart);
        if (optionalBiome.isPresent()) {
            coordinates = coordinates.append(" [%s]".formatted(Cubiomes.biome2str(seedIdentifierWithDimension.version(), optionalBiome.getAsInt()).getString(0)));
        }
        if (this.displayCoordinatesCopiedTicks > 0) {
            coordinates = Component.translatable("seedMap.coordinatesCopied", coordinates);
        }
        guiGraphicsExtractor.text(this.font, coordinates, this.horizontalPadding(), this.verticalPadding() + this.seedMapRenderer.seedMapHeight + 1, -1);
    }

    private void createFeatureToggles() {
        // TODO: only calculate on resize?
        int rows = Math.ceilDiv(this.featureIconsCombinedWidth, this.seedMapRenderer.seedMapWidth);
        int togglesPerRow = Math.ceilDiv(this.seedMapRenderer.getSeedMapData().getAvailableFeatures().size(), rows);
        List<List<MapFeature>> toggleRows = this.seedMapRenderer.getSeedMapData().getAvailableFeatures().stream().gather(Gatherers.windowFixed(togglesPerRow)).toList();

        int toggleMinY = 1;
        for (List<MapFeature> rowToggles : toggleRows) {
            int toggleMinX = this.horizontalPadding();
            for (MapFeature feature : rowToggles) {
                MapFeature.Texture featureIcon = feature.getDefaultTexture();
                this.addRenderableWidget(new FeatureToggleWidget(feature, toggleMinX, toggleMinY));
                toggleMinX += featureIcon.width() + HORIZONTAL_FEATURE_TOGGLE_SPACING;
            }
            toggleMinY += FEATURE_TOGGLE_HEIGHT + VERTICAL_FEATURE_TOGGLE_SPACING;
        }
    }

    private void createTeleportField() {
        this.teleportEditBoxX = new EditBox(this.font, this.width / 2 - TELEPORT_FIELD_WIDTH, this.verticalPadding() + this.seedMapRenderer.seedMapHeight + 1, TELEPORT_FIELD_WIDTH, 20, Component.translatable("seedMap.teleportEditBoxX"));
        this.teleportEditBoxX.setHint(Component.literal("X"));
        this.teleportEditBoxX.setMaxLength(9);
        this.addRenderableWidget(this.teleportEditBoxX);
        this.teleportEditBoxZ = new EditBox(this.font, this.width / 2, this.verticalPadding() + this.seedMapRenderer.seedMapHeight + 1, TELEPORT_FIELD_WIDTH, 20, Component.translatable("seedMap.teleportEditBoxZ"));
        this.teleportEditBoxZ.setHint(Component.literal("Z"));
        this.teleportEditBoxZ.setMaxLength(9);
        this.addRenderableWidget(this.teleportEditBoxZ);
    }

    private void createWaypointNameField() {
        this.waypointNameEditBox = new EditBox(this.font, this.horizontalPadding() + seedMapRenderer.seedMapWidth - WAYPOINT_NAME_FIELD_WIDTH, this.verticalPadding() + this.seedMapRenderer.seedMapHeight + 1, WAYPOINT_NAME_FIELD_WIDTH, 20, Component.translatable("seedMap.waypointNameEditBox"));
        this.waypointNameEditBox.setHint(Component.literal("Waypoint name"));
        this.addRenderableWidget(this.waypointNameEditBox);
    }

    public void moveCenter(QuartPos2f newCenter) {
        this.seedMapRenderer.moveCenter(newCenter);
        if (this.markerWidget != null) {
            this.markerWidget.updatePosition();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.displayCoordinatesCopiedTicks > 0) {
            this.displayCoordinatesCopiedTicks--;
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        this.seedMapRenderer.updateFeatureWidgets();
        if (this.markerWidget != null) {
            this.markerWidget.updatePosition();
        }
        this.chestLootWidget = null;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        this.handleMapMouseMoved(mouseX, mouseY);
    }

    private void handleMapMouseMoved(double mouseX, double mouseY) {
        if (mouseX < this.horizontalPadding() || mouseX > this.horizontalPadding() + this.seedMapRenderer.seedMapWidth || mouseY < this.verticalPadding() || mouseY > this.verticalPadding() + this.seedMapRenderer.seedMapHeight) {
            return;
        }

        int relXQuart = (int) ((mouseX - this.seedMapRenderer.centerX) / Configs.PixelsPerBiome);
        int relZQuart = (int) ((mouseY - this.seedMapRenderer.centerY) / Configs.PixelsPerBiome);

        this.mouseQuart = QuartPos2.fromQuartPos2f(this.seedMapRenderer.getCenterQuart().add(relXQuart, relZQuart));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }

        if (!minecraft.hasControlDown()) {
            return this.zoomMap(mouseX, mouseY, scrollX, scrollY);
        }

        return this.changeBiomeY(mouseX, mouseY, scrollX, scrollY);
    }

    private boolean zoomMap(double mouseX, double mouseY, double scrollX, double scrollY) {
        float currentScroll = Mth.clamp((float) Configs.PixelsPerBiome / SeedMapRenderer.MAX_PIXELS_PER_BIOME, 0.0F, 1.0F);
        currentScroll = Mth.clamp(currentScroll - (float) (-scrollY / SeedMapRenderer.MAX_PIXELS_PER_BIOME), 0.0F, 1.0F);

        Configs.PixelsPerBiome = Math.max((int) (currentScroll * SeedMapRenderer.MAX_PIXELS_PER_BIOME + 0.5), SeedMapRenderer.MIN_PIXELS_PER_BIOME);

        this.seedMapRenderer.updateFeatureWidgets();

        if (this.markerWidget != null) {
            this.markerWidget.updatePosition();
        }
        return true;
    }

    private boolean changeBiomeY(double mouseX, double mouseY, double scrollX, double scrollY) {
        SeedIdentifierWithDimension seedIdentifierWithDimension = this.seedMapRenderer.getSeedMapData().getSeedIdentifierWithDimension();
        if (seedIdentifierWithDimension.dimension() != Cubiomes.DIM_OVERWORLD()) {
            return false;
        }
        int y = this.seedMapRenderer.getSeedMapData().getBiomeYHeight() - SeedMapRenderer.BIOME_Y_GRANULARITY * (int)Math.signum(scrollY);

        Configs.setSeedMapBiomeY(y);

        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double dragX, double dragY) {
        int button = mouseButtonEvent.button();
        if (button != InputConstants.MOUSE_BUTTON_LEFT) {
            return false;
        }
        double mouseX = mouseButtonEvent.x();
        double mouseY = mouseButtonEvent.y();
        if (mouseX < this.horizontalPadding() || mouseX > this.horizontalPadding() + seedMapRenderer.seedMapWidth || mouseY < this.verticalPadding() || mouseY > this.verticalPadding() + this.seedMapRenderer.seedMapHeight) {
            return false;
        }

        float relXQuart = (float) (-dragX / Configs.PixelsPerBiome);
        float relZQuart = (float) (-dragY / Configs.PixelsPerBiome);

        this.moveCenter(this.seedMapRenderer.getCenterQuart().add(relXQuart, relZQuart));
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        if (super.mouseClicked(mouseButtonEvent, doubleClick)) {
            return true;
        }
        int button = mouseButtonEvent.button();
        if (this.chestLootWidget != null && this.chestLootWidget.mouseClicked(mouseButtonEvent, doubleClick)) {
            return true;
        } else if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            this.chestLootWidget = null;
        }
        if (this.handleMapFeatureLeftClicked(mouseButtonEvent, doubleClick)) {
            return true;
        }
        if (this.handleMapMiddleClicked(mouseButtonEvent, doubleClick)) {
            return true;
        }
        if (this.handleMapRightClicked(mouseButtonEvent, doubleClick)) {
            return true;
        }
        return false;
    }

    private boolean handleMapFeatureLeftClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        int button = mouseButtonEvent.button();
        if (button != InputConstants.MOUSE_BUTTON_LEFT) {
            return false;
        }
        double mouseX = mouseButtonEvent.x();
        double mouseY = mouseButtonEvent.y();
        if (mouseX < this.horizontalPadding() || mouseX > this.horizontalPadding() + this.seedMapRenderer.seedMapWidth || mouseY < this.verticalPadding() || mouseY > this.verticalPadding() + this.seedMapRenderer.seedMapHeight) {
            return false;
        }
        Optional<SeedMapRenderer.FeatureWidget> optionalFeatureWidget = this.seedMapRenderer.getFeatureWidgetAt(mouseX, mouseY);
        if (optionalFeatureWidget.isEmpty()) {
            return false;
        }
        SeedMapRenderer.FeatureWidget widget = optionalFeatureWidget.get();
        this.showLoot(widget);
        return true;
    }

    private void showLoot(SeedMapRenderer.FeatureWidget widget) {
        SeedIdentifierWithDimension seedIdentifierWithDimension = this.seedMapRenderer.getSeedMapData().getSeedIdentifierWithDimension();
        MapFeature feature = widget.feature();
        int structure = feature.getStructureId();
        if (!LocateCommand.LOOT_SUPPORTED_STRUCTURES.contains(structure)) {
            return;
        }
        BlockPos pos = widget.featureLocation();
        OptionalInt optionalBiome = this.seedMapRenderer.getSeedMapData().getBiome(QuartPos2.fromBlockPos(pos));
        if (optionalBiome.isEmpty()) {
            return;
        }
        int biome = optionalBiome.getAsInt();
        // temporary arena so that everything will be deallocated after the loot is calculated
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment structureVariant = StructureVariant.allocate(tempArena);
            if (Cubiomes.getVariant(structureVariant, structure, seedIdentifierWithDimension.version(), seedIdentifierWithDimension.seed(), pos.getX(), pos.getZ(), biome) != 0) {
                biome = StructureVariant.biome(structureVariant) != -1 ? StructureVariant.biome(structureVariant) : biome;
            }
            MemorySegment structureSaltConfig = StructureSaltConfig.allocate(tempArena);
            if (Cubiomes.getStructureSaltConfig(structure, seedIdentifierWithDimension.version(), biome, structureSaltConfig) == 0) {
                return;
            }
            MemorySegment pieces = Piece.allocateArray(StructureChecks.MAX_END_CITY_AND_FORTRESS_PIECES, tempArena);
            int numPieces = Cubiomes.getStructurePieces(pieces, StructureChecks.MAX_END_CITY_AND_FORTRESS_PIECES, structure, structureSaltConfig, structureVariant, seedIdentifierWithDimension.version(), seedIdentifierWithDimension.seed(), pos.getX(), pos.getZ());
            if (numPieces <= 0) {
                return;
            }
            List<ChestLootData> chestLootDataList = new ArrayList<>();
            MemorySegment ltcPtr = tempArena.allocate(Cubiomes.C_POINTER);
            for (int pieceIdx = 0; pieceIdx < numPieces; pieceIdx++) {
                MemorySegment piece = Piece.asSlice(pieces, pieceIdx);
                int chestCount = Piece.chestCount(piece);
                if (chestCount == 0) {
                    continue;
                }
                String pieceName = Piece.name(piece).getString(0);
                MemorySegment chestPoses = Piece.chestPoses(piece);
                MemorySegment lootTables = Piece.lootTables(piece);
                MemorySegment lootSeeds = Piece.lootSeeds(piece);
                for (int chestIdx = 0; chestIdx < chestCount; chestIdx++) {
                    MemorySegment lootTable = lootTables.getAtIndex(ValueLayout.ADDRESS, chestIdx).reinterpret(Long.MAX_VALUE);
                    String lootTableString = lootTable.getString(0);
                    if (Cubiomes.init_loot_table_name(ltcPtr, lootTable, seedIdentifierWithDimension.version()) == 0) {
                        continue;
                    }
                    MemorySegment lootTableContext = ltcPtr.get(ValueLayout.ADDRESS, 0).reinterpret(LootTableContext.sizeof());
                    MemorySegment chestPosInternal = Pos.asSlice(chestPoses, chestIdx);
                    BlockPos chestPos = new BlockPos(Pos.x(chestPosInternal), 0, Pos.z(chestPosInternal));
                    long lootSeed = lootSeeds.getAtIndex(Cubiomes.C_LONG_LONG, chestIdx);
                    Cubiomes.set_loot_prng_type(lootTableContext, Cubiomes.JAVA_RANDOM());
                    Cubiomes.set_loot_seed(lootTableContext, lootSeed);
                    Cubiomes.generate_loot(lootTableContext);
                    int lootCount = LootTableContext.generated_item_count(lootTableContext);
                    SimpleContainer container = new SimpleContainer(3 * 9);
                    for (int lootIdx = 0; lootIdx < lootCount; lootIdx++) {
                        MemorySegment itemStackInternal = ItemStack.asSlice(LootTableContext.generated_items(lootTableContext), lootIdx);
                        var itemStack = CubiomesHelper.convertItemStack(lootTableContext, itemStackInternal, this.enchantmentsRegistry);
                        CubiomesHelper.setMobEffectAsLore(itemStack, itemStackInternal, this.mobEffectRegistry);
                        container.addItem(itemStack);
                    }
                    chestLootDataList.add(new ChestLootData(structure, pieceName, chestPos, lootSeed, lootTableString, container));
                }
            }
            if (!chestLootDataList.isEmpty()) {
                this.chestLootWidget = new ChestLootWidget(widget.x() + widget.width() / 2, widget.y() + widget.height() / 2, chestLootDataList);
            }
        }
    }

    private boolean handleMapMiddleClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        int button = mouseButtonEvent.button();
        if (button != InputConstants.MOUSE_BUTTON_MIDDLE) {
            return false;
        }
        double mouseX = mouseButtonEvent.x();
        double mouseY = mouseButtonEvent.y();
        if (mouseX < this.horizontalPadding() || mouseX > this.horizontalPadding() + seedMapRenderer.seedMapWidth || mouseY < this.verticalPadding() || mouseY > this.verticalPadding() + this.seedMapRenderer.seedMapHeight) {
            return false;
        }
        this.minecraft.keyboardHandler.setClipboard("%d ~ %d".formatted(QuartPos.toBlock(this.mouseQuart.x()), QuartPos.toBlock(this.mouseQuart.z())));
        this.displayCoordinatesCopiedTicks = SharedConstants.TICKS_PER_SECOND;
        return true;
    }

    private boolean handleMapRightClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        int button = mouseButtonEvent.button();
        if (button != InputConstants.MOUSE_BUTTON_RIGHT) {
            return false;
        }
        double mouseX = mouseButtonEvent.x();
        double mouseY = mouseButtonEvent.y();
        if (mouseX < this.horizontalPadding() || mouseX > this.horizontalPadding() + this.seedMapRenderer.seedMapWidth || mouseY < this.verticalPadding() || mouseY > this.verticalPadding() + this.seedMapRenderer.seedMapHeight) {
            return false;
        }

        this.markerWidget = this.seedMapRenderer.createFeatureWidget(MapFeature.WAYPOINT, this.mouseQuart.toBlockPos().atY(63));
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (super.keyPressed(keyEvent)) {
            return true;
        }
        if (this.handleTeleportFieldEnter(keyEvent)) {
            return true;
        }
        if (this.handleWaypointNameFieldEnter(keyEvent)) {
            return true;
        }
        return false;
    }

    private boolean handleTeleportFieldEnter(KeyEvent keyEvent) {
        int keyCode = keyEvent.key();
        if (keyCode != InputConstants.KEY_RETURN) {
            return false;
        }
        if (!this.teleportEditBoxX.isActive() && !this.teleportEditBoxZ.isActive()) {
            return false;
        }
        int x, z;
        try {
            x = Integer.parseInt(this.teleportEditBoxX.getValue());
            z = Integer.parseInt(this.teleportEditBoxZ.getValue());
        } catch (NumberFormatException _) {
            return false;
        }
        if (x < -Level.MAX_LEVEL_SIZE || x > Level.MAX_LEVEL_SIZE) {
            return false;
        }
        if (z < -Level.MAX_LEVEL_SIZE || z > Level.MAX_LEVEL_SIZE) {
            return false;
        }
        this.moveCenter(new QuartPos2f(QuartPos.fromBlock(x), QuartPos.fromBlock(z)));
        this.teleportEditBoxX.setValue("");
        this.teleportEditBoxZ.setValue("");
        return true;
    }

    private boolean handleWaypointNameFieldEnter(KeyEvent keyEvent) {
        int keyCode = keyEvent.key();
        if (keyCode != InputConstants.KEY_RETURN) {
            return false;
        }
        if (this.markerWidget == null) {
            return false;
        }
        if (!this.waypointNameEditBox.isActive()) {
            return false;
        }
        String waypointName = this.waypointNameEditBox.getValue().trim();
        if (waypointName.isEmpty()) {
            return false;
        }
        SimpleWaypointsAPI waypointsApi = SimpleWaypointsAPI.getInstance();
        String identifier = waypointsApi.getWorldIdentifier(this.minecraft);
        if (identifier == null) {
            return false;
        }
        try {
            waypointsApi.addWaypoint(identifier, CubiomesHelper.getMinecraftDimension(this.seedMapRenderer.getSeedMapData().getSeedIdentifierWithDimension().dimension()), waypointName, this.markerWidget.featureLocation());
        } catch (CommandSyntaxException e) {
            LocalPlayer player = this.minecraft.player;
            if (player != null) {
                player.sendSystemMessage(error((MutableComponent) e.getRawMessage()));
            }
            return false;
        }
        this.waypointNameEditBox.setValue("");
        return true;
    }

    private int horizontalPadding() {
        return HORIZONTAL_PADDING;
    }

    private int verticalPadding() {
        return VERTICAL_PADDING;
    }

    @Override
    public void onClose() {
        super.onClose();
        this.seedMapRenderer.close();
    }
}
