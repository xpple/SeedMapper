package dev.xpple.seedmapper.seedmap;

import com.github.cubiomes.CanyonCarverConfig;
import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.EnchantInstance;
import com.github.cubiomes.Generator;
import com.github.cubiomes.ItemStack;
import com.github.cubiomes.LootTableContext;
import com.github.cubiomes.MobEffect;
import com.github.cubiomes.MobEffectInstance;
import com.github.cubiomes.OreVeinParameters;
import com.github.cubiomes.Piece;
import com.github.cubiomes.Pos;
import com.github.cubiomes.Range;
import com.github.cubiomes.StructureConfig;
import com.github.cubiomes.StructureSaltConfig;
import com.github.cubiomes.StructureVariant;
import com.github.cubiomes.SurfaceNoise;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.xpple.seedmapper.SeedMapper;
import dev.xpple.seedmapper.command.arguments.CanyonCarverArgument;
import dev.xpple.seedmapper.command.arguments.ItemAndEnchantmentsPredicateArgument;
import dev.xpple.seedmapper.command.commands.LocateCommand;
import dev.xpple.seedmapper.config.Configs;
import dev.xpple.seedmapper.feature.StructureChecks;
import dev.xpple.seedmapper.thread.SeedMapCache;
import dev.xpple.seedmapper.thread.SeedMapExecutor;
import dev.xpple.seedmapper.util.QuartPos2;
import dev.xpple.seedmapper.util.QuartPos2f;
import dev.xpple.seedmapper.util.RegionPos;
import dev.xpple.seedmapper.util.TwoDTree;
import dev.xpple.seedmapper.util.WorldIdentifier;
import dev.xpple.simplewaypoints.api.SimpleWaypointsAPI;
import dev.xpple.simplewaypoints.api.Waypoint;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.AbstractIntCollection;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.BlitRenderState;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.ToIntBiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
        SequenceLayout rgbLayout = MemoryLayout.sequenceLayout(3, Cubiomes.C_CHAR);

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
    //private static final int VERTICAL_PADDING = 0;
    public static final float MIN_PIXELS_PER_BIOME = 0.25F;
    public static final float MAX_PIXELS_PER_BIOME = 100.0F;

    private static final int VERTICAL_LOWER_PADDING = 20;

    private static final int HORIZONTAL_FEATURE_TOGGLE_SPACING = 1;
    private static final int VERTICAL_FEATURE_TOGGLE_SPACING = 1;

    private static final Identifier SEED_ICON_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/item/wheat_seeds.png");
    private static final int SEED_ICON_SIZE = 16;
    private static final int SEED_ICON_PADDING = 4;
    // can't find the chest texture
    private static final Identifier LOOT_SEARCH_ICON_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/item/chest_minecart.png");
    private static final int LOOT_SEARCH_ICON_SIZE = 16;
    private static final float MIN_STRUCTURE_REGION_PIXELS = 8.0F;
    private static final float MIN_CHUNK_PIXELS = 4.0F;

    //private static final int TELEPORT_FIELD_WIDTH = 70;
    //private static final int WAYPOINT_NAME_FIELD_WIDTH = 100;

    private static float tileSizePixels() {
        return TilePos.TILE_SIZE_CHUNKS * SCALED_CHUNK_SIZE * Configs.PixelsPerBiome;
    }

    private static final double FEATURE_TOGGLE_LOWER_PADDING_FACTOR = 0.95;

    private static final Object2ObjectMap<WorldIdentifier, Object2ObjectMap<TilePos, int[]>> biomeDataCache = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectMap<WorldIdentifier, Object2ObjectMap<ChunkPos, ChunkStructureData>> structureDataCache = new Object2ObjectOpenHashMap<>();
    public static final Object2ObjectMap<WorldIdentifier, TwoDTree> strongholdDataCache = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectMap<WorldIdentifier, Object2ObjectMap<TilePos, OreVeinData>> oreVeinDataCache = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectMap<WorldIdentifier, Object2ObjectMap<TilePos, BitSet>> canyonDataCache = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectMap<WorldIdentifier, Object2ObjectMap<TilePos, BitSet>> slimeChunkDataCache = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectMap<WorldIdentifier, BlockPos> spawnDataCache = new Object2ObjectOpenHashMap<>();

    private final SeedMapExecutor seedMapExecutor = new SeedMapExecutor();

    private final Arena arena = Arena.ofShared();

    private final long seed;
    private final int dimension;
    private final int version;
    private final int generatorFlags;
    private final WorldIdentifier worldIdentifier;

    /**
     * {@link Generator} to be used for biome calculations. This is thread safe.
     */
    private final MemorySegment biomeGenerator;
    /**
     * {@link Generator} to be used for structure calculations. This is NOT thread safe.
     */
    private final MemorySegment structureGenerator;
    private final @Nullable MemorySegment[] structureConfigs;
    private final MemorySegment surfaceNoise;
    private final PositionalRandomFactory oreVeinRandom;
    private final MemorySegment oreVeinParameters;
    private final @Nullable MemorySegment[] canyonCarverConfigs;

    private final Object2ObjectMap<TilePos, Tile> biomeTileCache = new Object2ObjectOpenHashMap<>();
    private final SeedMapCache<TilePos, int[]> biomeCache;
    private final Object2ObjectMap<ChunkPos, ChunkStructureData> structureCache;
    private final SeedMapCache<TilePos, OreVeinData> oreVeinCache;
    private final Object2ObjectMap<TilePos, BitSet> canyonCache;
    private final Object2ObjectMap<TilePos, Tile> slimeChunkTileCache = new Object2ObjectOpenHashMap<>();
    private final SeedMapCache<TilePos, BitSet> slimeChunkCache;

    private BlockPos playerPos;
    private Vec2 playerRotation;

    private QuartPos2f centerQuart;

    protected int centerX;
    protected int centerY;
    private int seedMapWidth;
    private int seedMapHeight;

    // A list of all features that can have their appearance on the map toggled on or off.
    private final List<MapFeature> toggleableFeatures;
    // The combined height of the images of *all* toggleable features. Includes padding.
    private final int featureIconsCombinedHeight;

    private final ObjectSet<FeatureWidget> featureWidgets = new ObjectOpenHashSet<>();

    private QuartPos2 mouseQuart;

    private int displayCoordinatesCopiedTicks = 0;

    private int lastMouseX = 0;
    private int lastMouseY = 0;

    private EditBox teleportEditBoxX;
    private EditBox teleportEditBoxZ;

    private EditBox waypointNameEditBox;

    private @Nullable FeatureWidget markerWidget = null;
    private @Nullable ChestLootWidget chestLootWidget = null;

    private static final Identifier DIRECTION_ARROW_TEXTURE = Identifier.fromNamespaceAndPath(SeedMapper.MOD_ID, "textures/gui/arrow.png");

    private Registry<Enchantment> enchantmentsRegistry;
    private Registry<net.minecraft.world.effect.MobEffect> mobEffectRegistry;

    public SeedMapScreen(long seed, int dimension, int version, int generatorFlags, BlockPos playerPos, Vec2 playerRotation) {
        super(Component.empty());
        this.seed = seed;
        this.dimension = dimension;
        this.version = version;
        this.generatorFlags = generatorFlags;
        this.worldIdentifier = new WorldIdentifier(this.seed, this.dimension, this.version, this.generatorFlags);

        this.biomeGenerator = Generator.allocate(this.arena);
        Cubiomes.setupGenerator(this.biomeGenerator, this.version, this.generatorFlags);
        Cubiomes.applySeed(this.biomeGenerator, this.dimension, this.seed);

        this.structureGenerator = Generator.allocate(this.arena);
        this.structureGenerator.copyFrom(this.biomeGenerator);

        this.structureConfigs = IntStream.range(0, Cubiomes.FEATURE_NUM())
            .mapToObj(structure -> {
                MemorySegment structureConfig = StructureConfig.allocate(this.arena);
                if (Cubiomes.getStructureConfig(structure, this.version, structureConfig) == 0) {
                    return null;
                }
                if (StructureConfig.dim(structureConfig) != this.dimension) {
                    return null;
                }
                return structureConfig;
            })
            .toArray(MemorySegment[]::new);

        this.surfaceNoise = SurfaceNoise.allocate(this.arena);
        Cubiomes.initSurfaceNoise(this.surfaceNoise, this.dimension, this.seed);

        this.oreVeinRandom = new XoroshiroRandomSource(this.seed).forkPositional().fromHashOf(Identifier.fromNamespaceAndPath(SeedMapper.MOD_ID, "ore_vein_feature")).forkPositional();
        this.oreVeinParameters = OreVeinParameters.allocate(this.arena);
        Cubiomes.initOreVeinNoise(this.oreVeinParameters, this.seed, this.version);

        this.canyonCarverConfigs = CanyonCarverArgument.CANYON_CARVERS.values().stream()
            .map(canyonCarver -> {
                MemorySegment ccc = CanyonCarverConfig.allocate(this.arena);
                if (Cubiomes.getCanyonCarverConfig(canyonCarver, this.version, ccc) == 0) {
                    return null;
                }
                return ccc;
            })
            .toArray(MemorySegment[]::new);

        this.toggleableFeatures = Arrays.stream(MapFeature.values())
            .filter(feature -> feature.getDimension() == this.dimension || feature.getDimension() == Cubiomes.DIM_UNDEF())
            .filter(feature -> this.version >= feature.availableSince())
            .sorted(Comparator.comparing(MapFeature::getName))
            .toList();

        this.biomeCache = new SeedMapCache<>(Object2ObjectMaps.synchronize(biomeDataCache.computeIfAbsent(this.worldIdentifier, _ -> new Object2ObjectOpenHashMap<>())), this.seedMapExecutor);
        this.structureCache = structureDataCache.computeIfAbsent(this.worldIdentifier, _ -> new Object2ObjectOpenHashMap<>());
        this.slimeChunkCache = new SeedMapCache<>(Object2ObjectMaps.synchronize(slimeChunkDataCache.computeIfAbsent(this.worldIdentifier, _ -> new Object2ObjectOpenHashMap<>())), this.seedMapExecutor);
        this.oreVeinCache = new SeedMapCache<>(oreVeinDataCache.computeIfAbsent(this.worldIdentifier, _ -> new Object2ObjectOpenHashMap<>()), this.seedMapExecutor);
        this.canyonCache = canyonDataCache.computeIfAbsent(this.worldIdentifier, _ -> new Object2ObjectOpenHashMap<>());

        if (this.toggleableFeatures.contains(MapFeature.STRONGHOLD) && !strongholdDataCache.containsKey(this.worldIdentifier)) {
            this.seedMapExecutor.submitCalculation(() -> LocateCommand.calculateStrongholds(this.seed, this.dimension, this.version, this.generatorFlags))
                .thenAccept(tree -> {
                    if (tree != null) {
                        strongholdDataCache.put(this.worldIdentifier, tree);
                    }
                });
        }

        this.featureIconsCombinedHeight = this.toggleableFeatures.stream()
            .map(feature -> feature.getDefaultTexture().height())
            .reduce((l, r) -> l + VERTICAL_FEATURE_TOGGLE_SPACING + r)
            .orElseThrow();

        this.playerPos = playerPos;
        this.playerRotation = playerRotation;

        this.centerQuart = QuartPos2f.fromQuartPos(QuartPos2.fromBlockPos(this.playerPos));
        this.mouseQuart = QuartPos2.fromQuartPos2f(this.centerQuart);
    }

    @Override
    protected void init() {
        super.init();

        this.centerX = this.width / 2;
        this.centerY = this.height / 2;

        this.seedMapWidth = this.width - this.horizontalPadding();
        this.seedMapHeight = this.height - VERTICAL_LOWER_PADDING;

        if (!this.isMinimap()) {
            this.createFeatureToggles();
            this.createTeleportField();
            this.createWaypointNameField();

            this.enchantmentsRegistry = this.minecraft.player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            this.mobEffectRegistry = this.minecraft.player.registryAccess().lookupOrThrow(Registries.MOB_EFFECT);
        }
   }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        // draw title
        //Component seedComponent = Component.translatable("seedMap.seed", accent(Long.toString(this.seed)), Cubiomes.mc2str(this.version).getString(0), ComponentUtils.formatGeneratorFlags(this.generatorFlags));
        //guiGraphics.drawString(this.font, seedComponent, this.horizontalPadding(), this.verticalPadding() - this.font.lineHeight - 1, -1);
        this.renderBiomes(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.nextStratum();
        this.renderFeatures(guiGraphics, mouseX, mouseY, partialTick);
        // draw hovered coordinates
        //MutableComponent coordinates = accent("x: %d, z: %d".formatted(QuartPos.toBlock(this.mouseQuart.x()), QuartPos.toBlock(this.mouseQuart.z())));
        //if (this.displayCoordinatesCopiedTicks > 0) {
        //    coordinates = Component.translatable("seedMap.coordinatesCopied", coordinates);
        //}
        //guiGraphics.drawString(this.font, coordinates, this.horizontalPadding(), this.verticalPadding() + this.seedMapHeight + 1, -1);

        if (!this.isMinimap()) {
            this.teleportEditBoxX.setHint(Component.literal("X: %d".formatted(QuartPos.toBlock(this.mouseQuart.x()))));
            this.teleportEditBoxZ.setHint(Component.literal("Z: %d".formatted(QuartPos.toBlock(this.mouseQuart.z()))));
        }

        if (this.isMouseOverMap(this.lastMouseX, this.lastMouseY)
            && !this.isMouseOverSeedWidget(this.lastMouseX, this.lastMouseY)
            && !this.isMouseOverLootSearchWidget(this.lastMouseX, this.lastMouseY)) {
            OptionalInt optionalBiome = getBiome(this.mouseQuart);
            if (optionalBiome.isPresent()) {
                Component tooltip = Component.literal(Cubiomes.biome2str(this.version, optionalBiome.getAsInt()).getString(0));
                List<ClientTooltipComponent> tooltips = List.of(ClientTooltipComponent.create(tooltip.getVisualOrderText()));
                guiGraphics.renderTooltip(this.font, tooltips, this.lastMouseX, this.lastMouseY, DefaultTooltipPositioner.INSTANCE, null);
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.nextStratum();
        this.drawSeedWidget(guiGraphics);
        this.drawLootSearchWidget(guiGraphics);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // No background; the map fills the screen.
    }

    protected void renderBiomes(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        float tileSizePixels = tileSizePixels();
        int horTileRadius = Mth.ceil(this.seedMapWidth / tileSizePixels) + 1;
        int verTileRadius = Mth.ceil(this.seedMapHeight / tileSizePixels) + 1;

        TilePos centerTile = TilePos.fromQuartPos(QuartPos2.fromQuartPos2f(this.centerQuart));
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
                if (this.shouldRenderFeature(MapFeature.SLIME_CHUNK)
                    && this.toggleableFeatures.contains(MapFeature.SLIME_CHUNK)
                    && Configs.ToggledFeatures.contains(MapFeature.SLIME_CHUNK)) {
                    BitSet slimeChunkData = this.slimeChunkCache.computeIfAbsent(tilePos, this::calculateSlimeChunkData);
                    if (slimeChunkData != null) {
                        Tile tile = this.slimeChunkTileCache.computeIfAbsent(tilePos, _ -> this.createSlimeChunkTile(tilePos, slimeChunkData));
                        this.drawTile(guiGraphics, tile);
                    }
                }
            }
        }
   }

   protected void renderFeatures(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
       this.featureWidgets.clear();
       float tileSizePixels = tileSizePixels();
       int horTileRadius = Mth.ceil(this.seedMapWidth / tileSizePixels) + 1;
       int verTileRadius = Mth.ceil(this.seedMapHeight / tileSizePixels) + 1;

       TilePos centerTile = TilePos.fromQuartPos(QuartPos2.fromQuartPos2f(this.centerQuart));

       int horChunkRadius = Mth.ceil((this.seedMapWidth / 2.0F) / (SCALED_CHUNK_SIZE * Configs.PixelsPerBiome));
       int verChunkRadius = Mth.ceil((this.seedMapHeight / 2.0F) / (SCALED_CHUNK_SIZE * Configs.PixelsPerBiome));

       // compute structures
       Configs.ToggledFeatures.stream()
           .filter(this.toggleableFeatures::contains)
           .filter(this::shouldRenderFeature)
           .filter(f -> f.getStructureId() != -1)
           .forEach(feature -> {
               int structure = feature.getStructureId();
               MemorySegment structureConfig = this.structureConfigs[structure];
               if (structureConfig == null) {
                   return;
               }
               int regionSize = StructureConfig.regionSize(structureConfig);
               RegionPos centerRegion = RegionPos.fromQuartPos(QuartPos2.fromQuartPos2f(this.centerQuart), regionSize);
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

                       ChunkStructureData chunkStructureData = this.structureCache.computeIfAbsent(chunkPos, _ -> new ChunkStructureData(chunkPos, new Int2ObjectArrayMap<>()));
                       StructureData data = chunkStructureData.structures().computeIfAbsent(structure, _ -> this.calculateStructureData(feature, regionPos, structurePos, generationCheck));
                       if (data == null) {
                           continue;
                       }
                       this.addFeatureWidget(feature, data.texture(), data.pos());
                   }
               }
           });

       guiGraphics.nextStratum();

       // draw strongholds
       if (this.shouldRenderFeature(MapFeature.STRONGHOLD)
           && this.toggleableFeatures.contains(MapFeature.STRONGHOLD)
           && Configs.ToggledFeatures.contains(MapFeature.STRONGHOLD)) {
           TwoDTree tree = strongholdDataCache.get(this.worldIdentifier);
           if (tree != null) {
               for (BlockPos strongholdPos : tree) {
                   this.addFeatureWidget(MapFeature.STRONGHOLD, strongholdPos);
               }
           }
       }

       // compute ore veins
       if ((this.shouldRenderFeature(MapFeature.COPPER_ORE_VEIN) || this.shouldRenderFeature(MapFeature.IRON_ORE_VEIN))
           && (this.toggleableFeatures.contains(MapFeature.COPPER_ORE_VEIN) || this.toggleableFeatures.contains(MapFeature.IRON_ORE_VEIN))
           && (Configs.ToggledFeatures.contains(MapFeature.COPPER_ORE_VEIN) || Configs.ToggledFeatures.contains(MapFeature.IRON_ORE_VEIN))) {
           for (int relTileX = -horTileRadius; relTileX <= horTileRadius; relTileX++) {
               for (int relTileZ = -verTileRadius; relTileZ <= verTileRadius; relTileZ++) {
                   TilePos tilePos = new TilePos(centerTile.x() + relTileX, centerTile.z() + relTileZ);
                   OreVeinData oreVeinData = this.oreVeinCache.computeIfAbsent(tilePos, this::calculateOreVein);
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
       if (this.shouldRenderFeature(MapFeature.CANYON)
           && (this.toggleableFeatures.contains(MapFeature.CANYON)) && Configs.ToggledFeatures.contains(MapFeature.CANYON)) {
           for (int relTileX = -horTileRadius; relTileX <= horTileRadius; relTileX++) {
               for (int relTileZ = -verTileRadius; relTileZ <= verTileRadius; relTileZ++) {
                   TilePos tilePos = new TilePos(centerTile.x() + relTileX, centerTile.z() + relTileZ);
                   ChunkPos chunkPos = tilePos.toChunkPos();
                   BitSet canyonData = this.canyonCache.computeIfAbsent(tilePos, this::calculateCanyonData);
                   canyonData.stream().forEach(i -> {
                       int relChunkX = i % TilePos.TILE_SIZE_CHUNKS;
                       int relChunkZ = i / TilePos.TILE_SIZE_CHUNKS;
                       int chunkX = chunkPos.x + relChunkX;
                       int chunkZ = chunkPos.z + relChunkZ;
                       this.addFeatureWidget(MapFeature.CANYON, new BlockPos(SectionPos.sectionToBlockCoord(chunkX), 0, SectionPos.sectionToBlockCoord(chunkZ)));
                   });
               }
           }
       }

       // draw waypoints
       if (this.shouldRenderFeature(MapFeature.WAYPOINT)
           && this.toggleableFeatures.contains(MapFeature.WAYPOINT)
           && Configs.ToggledFeatures.contains(MapFeature.WAYPOINT)) {
           SimpleWaypointsAPI waypointsApi = SimpleWaypointsAPI.getInstance();
           String identifier = waypointsApi.getWorldIdentifier(this.minecraft);
           Map<String, Waypoint> worldWaypoints = waypointsApi.getWorldWaypoints(identifier);
           worldWaypoints.forEach((name, waypoint) -> {
               if (!waypoint.dimension().equals(DIM_ID_TO_MC.get(this.dimension))) {
                   return;
               }
               FeatureWidget widget = this.addFeatureWidget(MapFeature.WAYPOINT, waypoint.location());
               if (widget == null) {
                   return;
               }
               int waypointCenterX = widget.x + widget.width() / 2;
               int waypointCenterY = widget.y + widget.width() / 2;
               var pose = guiGraphics.pose();
               pose.pushMatrix();
               if (this.isMinimap() && Configs.RotateMinimap) {
                   pose.translate(waypointCenterX, waypointCenterY);
                   pose.rotate((float) (Math.toRadians(this.playerRotation.y) - Math.PI));
                   pose.translate(-waypointCenterX, -waypointCenterY);
               }
               guiGraphics.drawCenteredString(this.font, name, waypointCenterX, waypointCenterY + widget.height() / 2, ARGB.color(255, waypoint.color()));
               pose.popMatrix();
           });
       }

       // draw player position
       this.drawPlayerIndicator(guiGraphics);

       // calculate spawn point
       if (this.toggleableFeatures.contains(MapFeature.WORLD_SPAWN) && Configs.ToggledFeatures.contains(MapFeature.WORLD_SPAWN)) {
           BlockPos spawnPoint = spawnDataCache.computeIfAbsent(this.worldIdentifier, _ -> this.calculateSpawnData());
           this.addFeatureWidget(MapFeature.WORLD_SPAWN, spawnPoint);
       }

       // draw feature icons
       this.drawFeatureIcons(guiGraphics);

       // draw marker
       if (!this.isMinimap()) {
           if (this.markerWidget != null && this.markerWidget.withinBounds()) {
               FeatureWidget.drawFeatureIcon(guiGraphics, this.markerWidget.featureTexture, this.markerWidget.x, this.markerWidget.y, -1);
           }
       }

       // draw chest loot widget
       if (!this.isMinimap()) {
           if (this.chestLootWidget != null) {
               this.chestLootWidget.render(guiGraphics, mouseX, mouseY, this.font);
           }
       }
   }

    private void drawTile(GuiGraphics guiGraphics, Tile tile) {
        TilePos tilePos = tile.pos();
        QuartPos2f relTileQuart = QuartPos2f.fromQuartPos(QuartPos2.fromTilePos(tilePos)).subtract(this.centerQuart);
        float tileSizePixels = tileSizePixels();
        int minX = this.centerX + Mth.floor(Configs.PixelsPerBiome * relTileQuart.x());
        int minY = this.centerY + Mth.floor(Configs.PixelsPerBiome * relTileQuart.z());
        int maxX = minX + Mth.ceil(tileSizePixels);
        int maxY = minY + Mth.ceil(tileSizePixels);

        if (maxX < this.horizontalPadding() || minX > this.horizontalPadding() + this.seedMapWidth) {
            return;
        }
        if (maxY < 0 || minY > this.seedMapHeight()) {
            return;
        }

        float u0, u1, v0, v1;
        if (minX < this.horizontalPadding()) {
            u0 = (this.horizontalPadding() - minX) / tileSizePixels;
            minX = this.horizontalPadding();
        } else u0 = 0;
        if (maxX > this.horizontalPadding() + this.seedMapWidth) {
            u1 = 1 - ((maxX - this.horizontalPadding() - this.seedMapWidth) / tileSizePixels);
            maxX = this.horizontalPadding() + this.seedMapWidth;
        } else u1 = 1;
        if (minY < 0) {
            v0 = (0 - minY) / tileSizePixels;
            minY = 0;
        } else v0 = 0;
        if (maxY > this.seedMapHeight()) {
            v1 = 1 - ((maxY - this.seedMapHeight()) / tileSizePixels);
            maxY = this.seedMapHeight();
        } else v1 = 1;

        //tile.texture().setFilter(Configs.PixelsPerBiome < 1.0F, false);
        guiGraphics.submitBlit(RenderPipelines.GUI_TEXTURED, tile.texture().getTextureView(), tile.texture().getSampler(), minX, minY, maxX, maxY, u0, u1, v0, v1, 0xFF_FFFFFF);
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

    private void drawFeatureIcons(GuiGraphics guiGraphics) {
        for (ObjectIterator<FeatureWidget> iterator = this.featureWidgets.iterator(); iterator.hasNext();) {
            FeatureWidget widget = iterator.next();
            if (Configs.ToggledFeatures.contains(widget.feature)) {
                MapFeature.Texture texture = widget.texture();
                this.drawIcon(guiGraphics, texture.identifier(), widget.x, widget.y, texture.width(), texture.height(), 0xFF_FFFFFF);
            } else {
                iterator.remove();
            }
        }
    }

    protected void drawPlayerIndicator(GuiGraphics guiGraphics) {
        if (!this.shouldRenderFeature(MapFeature.PLAYER_ICON)
            || !this.toggleableFeatures.contains(MapFeature.PLAYER_ICON)
            || !Configs.ToggledFeatures.contains(MapFeature.PLAYER_ICON)) {
            return;
        }
        QuartPos2f relPlayerQuart = QuartPos2f.fromQuartPos(QuartPos2.fromBlockPos(this.playerPos)).subtract(this.centerQuart);
        int playerMinX = this.centerX + Mth.floor(Configs.PixelsPerBiome * relPlayerQuart.x()) - 10;
        int playerMinY = this.centerY + Mth.floor(Configs.PixelsPerBiome * relPlayerQuart.z()) - 10;
        int playerMaxX = playerMinX + 20;
        int playerMaxY = playerMinY + 20;
        if (playerMinX < this.horizontalPadding() || playerMaxX > this.horizontalPadding() + this.seedMapWidth || playerMinY < 0 || playerMaxY > this.seedMapHeight()) {
            return;
        }
        PlayerFaceRenderer.draw(guiGraphics, this.minecraft.player.getSkin(), playerMinX, playerMinY, 20);

        this.drawDirectionArrow(guiGraphics, playerMinX, playerMinY);
    }

    protected void drawDirectionArrow(GuiGraphics guiGraphics, int playerMinX, int playerMinY) {
        guiGraphics.pose().pushMatrix();
        Matrix3x2f transform = guiGraphics.pose() // transformations are applied in reverse order
            .translate(10, 10)
            .translate(playerMinX, playerMinY)
            .rotate((float) (Math.toRadians(this.playerRotation.y) + Math.PI))
            .translate(-10, -10)
            .translate(0, -30)
        ;
        boolean withinBounds = Stream.of(new Vector2f(20, 0), new Vector2f(20, 20), new Vector2f(0, 20), new Vector2f(0, 0))
            .map(transform::transformPosition)
            .allMatch(v -> v.x >= this.horizontalPadding() && v.x <= this.horizontalPadding() + this.seedMapWidth &&
                v.y >= 0 && v.y <= this.seedMapHeight());
        if (withinBounds) {
            drawIconStatic(guiGraphics, DIRECTION_ARROW_TEXTURE, 0, 0, 20, 20, 0xFF_FFFFFF);
        }
        guiGraphics.pose().popMatrix();
    }

    // Draw a seed icon in the bottom-right which shows the seed in a tooltip when hovered.
    protected void drawSeedWidget(GuiGraphics guiGraphics) {
        int minX = this.getSeedWidgetMinX();
        int minY = this.getSeedWidgetMinY();
        int maxX = minX + SEED_ICON_SIZE;
        int maxY = minY + SEED_ICON_SIZE;

        drawIconStatic(guiGraphics, SEED_ICON_TEXTURE, minX, minY, SEED_ICON_SIZE, SEED_ICON_SIZE, 0xFF_FFFFFF);

        if (this.lastMouseX < minX || this.lastMouseX > maxX || this.lastMouseY < minY || this.lastMouseY > maxY) {
            return;
        }

        Component tooltip = Component.literal(Long.toString(this.seed));
        List<ClientTooltipComponent> tooltips = List.of(ClientTooltipComponent.create(tooltip.getVisualOrderText()), ClientTooltipComponent.create(Component.translatable("seedMap.clickToCopy").getVisualOrderText()));
        guiGraphics.renderTooltip(this.font, tooltips, this.lastMouseX, this.lastMouseY, DefaultTooltipPositioner.INSTANCE, null);
    }

    protected void drawLootSearchWidget(GuiGraphics guiGraphics) {
        int minX = this.getLootSearchWidgetMinX();
        int minY = this.getLootSearchWidgetMinY();
        int maxX = minX + LOOT_SEARCH_ICON_SIZE;
        int maxY = minY + LOOT_SEARCH_ICON_SIZE;

        drawIconStatic(guiGraphics, LOOT_SEARCH_ICON_TEXTURE, minX, minY, LOOT_SEARCH_ICON_SIZE, LOOT_SEARCH_ICON_SIZE, 0xFF_FFFFFF);

        if (this.lastMouseX < minX || this.lastMouseX > maxX || this.lastMouseY < minY || this.lastMouseY > maxY) {
            return;
        }

        Component tooltip = Component.literal("Loot search");
        List<ClientTooltipComponent> tooltips = List.of(ClientTooltipComponent.create(tooltip.getVisualOrderText()));
        guiGraphics.renderTooltip(this.font, tooltips, this.lastMouseX, this.lastMouseY, DefaultTooltipPositioner.INSTANCE, null);
    }

    private int getSeedWidgetMinX() {
        return this.width - SEED_ICON_SIZE - SEED_ICON_PADDING;
    }

    private int getSeedWidgetMinY() {
        return SEED_ICON_PADDING;
    }

    private int getLootSearchWidgetMinX() {
        return this.getSeedWidgetMinX() - LOOT_SEARCH_ICON_SIZE - SEED_ICON_PADDING;
    }

    private int getLootSearchWidgetMinY() {
        return this.getSeedWidgetMinY();
    }

    private boolean isMouseOverMap(double mouseX, double mouseY) {
        return mouseX >= this.horizontalPadding()
            && mouseX <= this.horizontalPadding() + this.seedMapWidth
            && mouseY >= 0
            && mouseY <= this.seedMapHeight();
    }

    private boolean isMouseOverSeedWidget(double mouseX, double mouseY) {
        int minX = this.getSeedWidgetMinX();
        int minY = this.getSeedWidgetMinY();
        int maxX = minX + SEED_ICON_SIZE;
        int maxY = minY + SEED_ICON_SIZE;
        return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY;
    }

    private boolean isMouseOverLootSearchWidget(double mouseX, double mouseY) {
        int minX = this.getLootSearchWidgetMinX();
        int minY = this.getLootSearchWidgetMinY();
        int maxX = minX + LOOT_SEARCH_ICON_SIZE;
        int maxY = minY + LOOT_SEARCH_ICON_SIZE;
        return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY;
    }

    private boolean shouldRenderFeature(MapFeature feature) {
        if (feature == MapFeature.WAYPOINT || feature == MapFeature.WORLD_SPAWN || feature == MapFeature.PLAYER_ICON) {
            return true;
        }
        if (feature == MapFeature.SLIME_CHUNK || feature == MapFeature.CANYON || feature == MapFeature.COPPER_ORE_VEIN || feature == MapFeature.IRON_ORE_VEIN) {
            float pixelsPerChunk = SCALED_CHUNK_SIZE * Configs.PixelsPerBiome;
            return pixelsPerChunk >= MIN_CHUNK_PIXELS;
        }
        int structureId = feature.getStructureId();
        if (structureId != -1 && this.structureConfigs != null) {
            MemorySegment structureConfig = this.structureConfigs[structureId];
            if (structureConfig != null) {
                int regionSize = StructureConfig.regionSize(structureConfig);
                float pixelsPerRegion = regionSize * SCALED_CHUNK_SIZE * Configs.PixelsPerBiome;
                return pixelsPerRegion >= MIN_STRUCTURE_REGION_PIXELS;
            }
        }
        return true;
    }

    // Formerly, this laid the feature toggles out in rows above the map.
    // Now they are laid out in columns besides the map.
    private void createFeatureToggles() {
        // TODO: replace with Gatherers API?
        // TODO: only calculate on resize?

        Pair<Integer, Double> columnCountAndScale = this.computeFeatureToggleScale();
        int columns = columnCountAndScale.left();
        double scale = columnCountAndScale.right();

        int togglesPerColumn = Math.ceilDiv(this.toggleableFeatures.size(), columns);

        int row = 0;
        int iconLeftX = 0, iconTopY = 0;
        int maxIconWidth = 0;
        for (MapFeature toggleableFeature : this.toggleableFeatures) {
            // Draw the icon.
            int iconHeight = (int)Math.ceil(scale*toggleableFeature.getDefaultTexture().height());
            this.addRenderableWidget(new FeatureToggleWidget(toggleableFeature, iconLeftX, iconTopY, scale));
            
            // Set up the position for where to draw the next icon.
            iconTopY += iconHeight + (int)Math.ceil(scale*VERTICAL_FEATURE_TOGGLE_SPACING);
            maxIconWidth = Math.max(maxIconWidth, (int)Math.ceil(scale*toggleableFeature.getDefaultTexture().width()));

            ++row;
            if (row >= togglesPerColumn) {
                // Begin a new column.
                row = 0;
                iconTopY = 0;
                iconLeftX += maxIconWidth + Math.ceil(scale*HORIZONTAL_FEATURE_TOGGLE_SPACING);
                maxIconWidth = 0;
            }
        }
    }

    private Pair<Integer, Double> computeFeatureToggleScale() {
        int baseColumnWidth = 0;
        for (MapFeature toggleableFeature : this.toggleableFeatures) {
            baseColumnWidth = Math.max(baseColumnWidth, toggleableFeature.getDefaultTexture().width());
        }

        int n = this.toggleableFeatures.size();
        Pair<Integer, Double> bestResult = null;
        for (int columns = 1; columns <= n; ++columns) {
            // Approximate the maximum column height. This doesn't work if the textures have very different heights.
            /// TODO: enforce textures being squares of the same size.
            int maxColumnHeight = this.featureIconsCombinedHeight / columns;

            double scaleX = (double)HORIZONTAL_PADDING / (columns * (baseColumnWidth + HORIZONTAL_FEATURE_TOGGLE_SPACING));
            double scaleY = (double)this.height * FEATURE_TOGGLE_LOWER_PADDING_FACTOR / maxColumnHeight;
            double scale = Math.min(scaleX, scaleY);

            if (scale <= 0) continue;

            if (bestResult == null || scale > bestResult.right())
                bestResult = Pair.of(columns, scale);
        }

        return bestResult;
    }

    private int[] calculateBiomeData(TilePos tilePos) {
        QuartPos2 quartPos = QuartPos2.fromTilePos(tilePos);
        int rangeSize = TilePos.TILE_SIZE_CHUNKS * SCALED_CHUNK_SIZE;

        // temporary arena so that everything will be deallocated after the biomes are calculated
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment range = Range.allocate(tempArena);
            Range.scale(range, BIOME_SCALE);
            Range.x(range, quartPos.x());
            Range.z(range, quartPos.z());
            Range.sx(range, rangeSize);
            Range.sz(range, rangeSize);
            Range.y(range, 63 / Range.scale(range)); // sea level
            Range.sy(range, 1);

            long cacheSize = Cubiomes.getMinCacheSize(this.biomeGenerator, Range.scale(range), Range.sx(range), Range.sy(range), Range.sz(range));
            MemorySegment biomeIds = tempArena.allocate(Cubiomes.C_INT, cacheSize);
            if (Cubiomes.genBiomes(this.biomeGenerator, biomeIds, range) == 0) {
                return biomeIds.toArray(Cubiomes.C_INT);
            }
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

    private @Nullable StructureData calculateStructureData(MapFeature feature, RegionPos regionPos, MemorySegment structurePos, StructureChecks.GenerationCheck generationCheck) {
        if (!generationCheck.check(this.structureGenerator, this.surfaceNoise, regionPos.x(), regionPos.z(), structurePos)) {
            return null;
        }

        BlockPos pos = new BlockPos(Pos.x(structurePos), 0, Pos.z(structurePos));
        OptionalInt optionalBiome = getBiome(QuartPos2.fromBlockPos(pos));
        MapFeature.Texture texture;
        if (optionalBiome.isEmpty()) {
            texture = feature.getDefaultTexture();
        } else {
            texture = feature.getVariantTexture(this.worldIdentifier, pos.getX(), pos.getZ(), optionalBiome.getAsInt());
        }
        return new StructureData(pos, texture);
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

    private BitSet calculateCanyonData(TilePos tilePos) {
        ToIntBiFunction<Integer, Integer> biomeFunction;
        if (this.version <= Cubiomes.MC_1_17()) {
            biomeFunction = (chunkX, chunkZ) -> getBiome(new QuartPos2(QuartPos.fromSection(chunkX), QuartPos.fromSection(chunkZ))).orElseGet(() -> Cubiomes.getBiomeAt(this.biomeGenerator, 4, chunkX << 2, 0, chunkZ << 2));
        } else {
            biomeFunction = (_, _) -> -1;
        }
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment rnd = tempArena.allocate(Cubiomes.C_LONG_LONG);
            BitSet canyons = new BitSet(TilePos.TILE_SIZE_CHUNKS * TilePos.TILE_SIZE_CHUNKS);
            ChunkPos chunkPos = tilePos.toChunkPos();
            for (int relChunkX = 0; relChunkX < TilePos.TILE_SIZE_CHUNKS; relChunkX++) {
                for (int relChunkZ = 0; relChunkZ < TilePos.TILE_SIZE_CHUNKS; relChunkZ++) {
                    int chunkX = chunkPos.x + relChunkX;
                    int chunkZ = chunkPos.z + relChunkZ;
                    for (int canyonCarver : CanyonCarverArgument.CANYON_CARVERS.values()) {
                        MemorySegment ccc = this.canyonCarverConfigs[canyonCarver];
                        if (ccc == null) {
                            continue;
                        }
                        int biome = biomeFunction.applyAsInt(chunkX, chunkZ);
                        if (Cubiomes.isViableCanyonBiome(canyonCarver, biome) == 0) {
                            continue;
                        }
                        if (Cubiomes.checkCanyonStart(this.seed, chunkX, chunkZ, ccc, rnd) == 0) {
                            continue;
                        }
                        canyons.set(relChunkX + relChunkZ * TilePos.TILE_SIZE_CHUNKS);
                        break;
                    }
                }
            }
            return canyons;
        }
    }

    private OptionalInt getBiome(QuartPos2 pos) {
        TilePos tilePos = TilePos.fromQuartPos(pos);
        int[] biomeCache = this.biomeCache.get(tilePos);
        if (biomeCache == null) {
            return OptionalInt.empty();
        }
        QuartPos2 quartPos = QuartPos2.fromTilePos(tilePos);
        QuartPos2 relQuartPos = pos.subtract(quartPos);
        return OptionalInt.of(biomeCache[relQuartPos.x() + relQuartPos.z() * Tile.TEXTURE_SIZE]);
    }

    private BlockPos calculateSpawnData() {
        MemorySegment pos = Cubiomes.getSpawn(this.arena, this.biomeGenerator);
        return new BlockPos(Pos.x(pos), 0, Pos.z(pos));
    }

    private void createTeleportField() {
        this.teleportEditBoxX = new EditBox(this.font, this.horizontalPadding(), this.height - 20, this.seedMapWidth / 4, 20, Component.translatable("seedMap.teleportEditBoxX"));
        this.teleportEditBoxX.setHint(Component.literal("x"));
        this.teleportEditBoxX.setMaxLength(9);
        this.addRenderableWidget(this.teleportEditBoxX);
        this.teleportEditBoxZ = new EditBox(this.font, this.horizontalPadding() + this.seedMapWidth / 4, this.height - 20, this.seedMapWidth / 4, 20, Component.translatable("seedMap.teleportEditBoxZ"));
        this.teleportEditBoxZ.setHint(Component.literal("z"));
        this.teleportEditBoxZ.setMaxLength(9);
        this.addRenderableWidget(this.teleportEditBoxZ);
    }

    private void createWaypointNameField() {
        this.waypointNameEditBox = new EditBox(this.font, this.horizontalPadding() + this.seedMapWidth / 2, this.height - 20, this.seedMapWidth / 2, 20, Component.translatable("seedMap.waypointNameEditBox"));
        this.waypointNameEditBox.setHint(Component.literal("Waypoint name"));
        this.addRenderableWidget(this.waypointNameEditBox);
    }

    protected void moveCenter(QuartPos2f newCenter) {
        this.centerQuart = newCenter;

        this.featureWidgets.removeIf(widget -> {
            widget.updatePosition();
            return !widget.withinBounds();
        });

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
        this.moveCenter(this.centerQuart);
        this.chestLootWidget = null;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        this.handleMapMouseMoved(mouseX, mouseY);
    }

    private void handleMapMouseMoved(double mouseX, double mouseY) {
        if (!this.isMouseOverMap(mouseX, mouseY)) {
            return;
        }

        int relXQuart = (int) ((mouseX - this.centerX) / Configs.PixelsPerBiome);
        int relZQuart = (int) ((mouseY - this.centerY) / Configs.PixelsPerBiome);

        this.mouseQuart = QuartPos2.fromQuartPos2f(this.centerQuart.add(relXQuart, relZQuart));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }

        float oldPixelsPerBiome = Configs.PixelsPerBiome;
        float currentScroll = Mth.clamp(oldPixelsPerBiome / MAX_PIXELS_PER_BIOME, MIN_PIXELS_PER_BIOME / MAX_PIXELS_PER_BIOME, 1.0F);
        currentScroll = Mth.clamp(currentScroll - (float) (-scrollY / MAX_PIXELS_PER_BIOME), MIN_PIXELS_PER_BIOME / MAX_PIXELS_PER_BIOME, 1.0F);
        float newPixelsPerBiome = currentScroll * MAX_PIXELS_PER_BIOME;
        Configs.PixelsPerBiome = newPixelsPerBiome;

        if (this.isMouseOverMap(mouseX, mouseY)) {
            float relXQuartOld = (float) ((mouseX - this.centerX) / oldPixelsPerBiome);
            float relZQuartOld = (float) ((mouseY - this.centerY) / oldPixelsPerBiome);
            QuartPos2f worldQuart = this.centerQuart.add(relXQuartOld, relZQuartOld);
            float relXQuartNew = (float) ((mouseX - this.centerX) / newPixelsPerBiome);
            float relZQuartNew = (float) ((mouseY - this.centerY) / newPixelsPerBiome);
            this.moveCenter(worldQuart.subtract(new QuartPos2f(relXQuartNew, relZQuartNew)));
        } else {
            this.featureWidgets.removeIf(widget -> {
                widget.updatePosition();
                return !widget.withinBounds();
            });

            if (this.markerWidget != null) {
                this.markerWidget.updatePosition();
            }
        }
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
        if (!this.isMouseOverMap(mouseX, mouseY)) {
            return false;
        }

        float relXQuart = (float) (-dragX / Configs.PixelsPerBiome);
        float relZQuart = (float) (-dragY / Configs.PixelsPerBiome);

        this.moveCenter(this.centerQuart.add(relXQuart, relZQuart));
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        if (super.mouseClicked(mouseButtonEvent, doubleClick)) {
            return true;
        }
        if (mouseButtonEvent.button() == InputConstants.MOUSE_BUTTON_LEFT && this.isMouseOverSeedWidget(mouseButtonEvent.x(), mouseButtonEvent.y())) {
            this.minecraft.keyboardHandler.setClipboard(Long.toString(this.seed));
            return true;
        }
        if (mouseButtonEvent.button() == InputConstants.MOUSE_BUTTON_LEFT && this.isMouseOverLootSearchWidget(mouseButtonEvent.x(), mouseButtonEvent.y())) {
            this.minecraft.setScreen(new LootSearchScreen(this, this.seed, this.dimension, this.version, this.generatorFlags, this.playerPos));
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
        if (mouseX < this.horizontalPadding() || mouseX > this.horizontalPadding() + this.seedMapWidth || mouseY < 0 || mouseY > this.seedMapHeight()) {
            return false;
        }
        Optional<FeatureWidget> optionalFeatureWidget = this.featureWidgets.stream()
            .filter(widget -> mouseX >= widget.x && mouseX <= widget.x + widget.width() && mouseY >= widget.y && mouseY <= widget.y + widget.height())
            .findAny();
        if (optionalFeatureWidget.isEmpty()) {
            return false;
        }
        FeatureWidget widget = optionalFeatureWidget.get();
        this.showLoot(widget);
        return true;
    }

    private void showLoot(FeatureWidget widget) {
        MapFeature feature = widget.feature;
        int structure = feature.getStructureId();
        if (!LocateCommand.LOOT_SUPPORTED_STRUCTURES.contains(structure)) {
            return;
        }
        BlockPos pos = widget.featureLocation;
        int biome = Cubiomes.getBiomeAt(this.biomeGenerator, BIOME_SCALE, QuartPos.fromBlock(pos.getX()), QuartPos.fromBlock(320), QuartPos.fromBlock(pos.getZ()));
        // temporary arena so that everything will be deallocated after the loot is calculated
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment structureVariant = StructureVariant.allocate(tempArena);
            Cubiomes.getVariant(structureVariant, structure, this.version, this.seed, pos.getX(), pos.getZ(), biome);
            biome = StructureVariant.biome(structureVariant) != -1 ? StructureVariant.biome(structureVariant) : biome;
            MemorySegment structureSaltConfig = StructureSaltConfig.allocate(tempArena);
            if (Cubiomes.getStructureSaltConfig(structure, this.version, biome, structureSaltConfig) == 0) {
                return;
            }
            MemorySegment pieces = Piece.allocateArray(StructureChecks.MAX_END_CITY_AND_FORTRESS_PIECES, tempArena);
            int numPieces = Cubiomes.getStructurePieces(pieces, StructureChecks.MAX_END_CITY_AND_FORTRESS_PIECES, structure, structureSaltConfig, structureVariant, this.version, this.seed, pos.getX(), pos.getZ());
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
                    if (Cubiomes.init_loot_table_name(ltcPtr, lootTable, this.version) == 0) {
                        continue;
                    }
                    MemorySegment lootTableContext = ltcPtr.get(ValueLayout.ADDRESS, 0).reinterpret(LootTableContext.sizeof());
                    MemorySegment chestPosInternal = Pos.asSlice(chestPoses, chestIdx);
                    BlockPos chestPos = new BlockPos(Pos.x(chestPosInternal), 0, Pos.z(chestPosInternal));
                    long lootSeed = lootSeeds.getAtIndex(Cubiomes.C_LONG_LONG, chestIdx);
                    Cubiomes.set_loot_seed(lootTableContext, lootSeed);
                    Cubiomes.generate_loot(lootTableContext);
                    int lootCount = LootTableContext.generated_item_count(lootTableContext);
                    SimpleContainer container = new SimpleContainer(3 * 9);
                    for (int lootIdx = 0; lootIdx < lootCount; lootIdx++) {
                        MemorySegment itemStackInternal = ItemStack.asSlice(LootTableContext.generated_items(lootTableContext), lootIdx);
                        int itemId = Cubiomes.get_global_item_id(lootTableContext, ItemStack.item(itemStackInternal));
                        Item item = ItemAndEnchantmentsPredicateArgument.ITEM_ID_TO_MC.get(itemId);
                        net.minecraft.world.item.ItemStack itemStack = new net.minecraft.world.item.ItemStack(item, ItemStack.count(itemStackInternal));
                        MemorySegment enchantments = ItemStack.enchantments(itemStackInternal);
                        int enchantmentCount = ItemStack.enchantment_count(itemStackInternal);
                        for (int enchantmentIdx = 0; enchantmentIdx < enchantmentCount; enchantmentIdx++) {
                            MemorySegment enchantInstance = EnchantInstance.asSlice(enchantments, enchantmentIdx);
                            int itemEnchantment = EnchantInstance.enchantment(enchantInstance);
                            ResourceKey<Enchantment> enchantmentResourceKey = ItemAndEnchantmentsPredicateArgument.ENCHANTMENT_ID_TO_MC.get(itemEnchantment);
                            Holder.Reference<Enchantment> enchantmentReference = this.enchantmentsRegistry.getOrThrow(enchantmentResourceKey);
                            itemStack.enchant(enchantmentReference, EnchantInstance.level(enchantInstance));
                        }
                        MemorySegment mobEffectInstance = ItemStack.mob_effect(itemStackInternal);
                        if (MobEffectInstance.effect(mobEffectInstance) != -1) {
                            MemorySegment mobEffectInternal = MobEffect.asSlice(Cubiomes.MOB_EFFECTS(), MobEffectInstance.effect(mobEffectInstance));
                            var mobEffect = this.mobEffectRegistry.getOptional(Identifier.parse(MobEffect.effect_name(mobEffectInternal).getString(0))).orElse(null);
                            if (mobEffect != null) {
                                SuspiciousStewEffects.Entry entry = new SuspiciousStewEffects.Entry(Holder.direct(mobEffect), MobEffectInstance.duration(mobEffectInstance));
                                net.minecraft.world.effect.MobEffectInstance effectInstance = entry.createEffectInstance();
                                MutableComponent description = PotionContents.getPotionDescription(effectInstance.getEffect(), effectInstance.getAmplifier());
                                MutableComponent lore = Component.translatable("seedMap.chestLoot.stewEffect", description, (float) entry.duration() / SharedConstants.TICKS_PER_SECOND);
                                itemStack.set(DataComponents.LORE, new ItemLore(List.of(lore)));
                            }
                        }
                        container.addItem(itemStack);
                    }
                    chestLootDataList.add(new ChestLootData(structure, pieceName, chestPos, lootSeed, lootTableString, container));
                }
            }
            if (!chestLootDataList.isEmpty()) {
                this.chestLootWidget = new ChestLootWidget(widget.x + widget.width() / 2, widget.y + widget.height() / 2, chestLootDataList);
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
        if (!this.isMouseOverMap(mouseX, mouseY)) {
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
        if (!this.isMouseOverMap(mouseX, mouseY)) {
            return false;
        }

        this.markerWidget = new FeatureWidget(MapFeature.WAYPOINT, this.mouseQuart.toBlockPos().atY(63));
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
        if (this.teleportEditBoxX == null || this.teleportEditBoxZ == null) {
            return false;
        }
        boolean hasTeleportFocus = this.teleportEditBoxX.isActive() || this.teleportEditBoxZ.isActive();
        if (!hasTeleportFocus) {
            return false;
        }
        int x, z;
        try {
            x = Integer.parseInt(this.teleportEditBoxX.getValue());
            z = Integer.parseInt(this.teleportEditBoxZ.getValue());
        } catch (NumberFormatException _) {
            this.clearEntryBoxFocus();
            return false;
        }
        if (x < -Level.MAX_LEVEL_SIZE || x > Level.MAX_LEVEL_SIZE) {
            this.clearEntryBoxFocus();
            return false;
        }
        if (z < -Level.MAX_LEVEL_SIZE || z > Level.MAX_LEVEL_SIZE) {
            this.clearEntryBoxFocus();
            return false;
        }
        this.moveCenter(new QuartPos2f(QuartPos.fromBlock(x), QuartPos.fromBlock(z)));
        this.teleportEditBoxX.setValue("");
        this.teleportEditBoxZ.setValue("");
        this.clearEntryBoxFocus();
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
        try {
            waypointsApi.addWaypoint(identifier, DIM_ID_TO_MC.get(this.dimension), waypointName, this.markerWidget.featureLocation);
        } catch (CommandSyntaxException e) {
            LocalPlayer player = this.minecraft.player;
            if (player != null) {
                player.displayClientMessage(error((MutableComponent) e.getRawMessage()), false);
            }
            return false;
        }
        this.waypointNameEditBox.setValue("");
        this.clearEntryBoxFocus();
        return true;
    }

    private void clearEntryBoxFocus() {
        if (this.teleportEditBoxX != null) {
            this.teleportEditBoxX.setFocused(false);
        }
        if (this.teleportEditBoxZ != null) {
            this.teleportEditBoxZ.setFocused(false);
        }
        if (this.waypointNameEditBox != null) {
            this.waypointNameEditBox.setFocused(false);
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        this.biomeTileCache.values().forEach(Tile::close);
        this.slimeChunkTileCache.values().forEach(Tile::close);
        this.seedMapExecutor.close(this.arena::close);
        Configs.save();
    }

    class FeatureWidget {
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

        public MapFeature.Texture texture() {
            return this.featureTexture;
        }

        public void updatePosition() {
            QuartPos2f relFeatureQuart = QuartPos2f.fromQuartPos(QuartPos2.fromBlockPos(this.featureLocation)).subtract(centerQuart);
            this.x = centerX + Mth.floor(Configs.PixelsPerBiome * relFeatureQuart.x()) - this.featureTexture.width() / 2;
            this.y = centerY + Mth.floor(Configs.PixelsPerBiome * relFeatureQuart.z()) - this.featureTexture.height() / 2;
        }

        private int width() {
            return this.featureTexture.width();
        }

        private int height() {
            return this.featureTexture.height();
        }

        public boolean withinBounds() {
            int minX = this.x;
            int minY = this.y;
            int maxX = minX + this.width();
            int maxY = minY + this.height();

            if (maxX >= horizontalPadding() + seedMapWidth || maxY >= seedMapHeight()) {
                return false;
            }
            if (minX < horizontalPadding() || minY < 0) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.feature, this.featureTexture, this.featureLocation);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            FeatureWidget that = (FeatureWidget) o;
            return this.feature == that.feature && Objects.equals(this.featureTexture, that.featureTexture) && Objects.equals(this.featureLocation, that.featureLocation);
        }

        static void drawFeatureIcon(GuiGraphics guiGraphics, MapFeature.Texture texture, int minX, int minY, int colour) {
            int iconWidth = texture.width();
            int iconHeight = texture.height();

            drawIconStatic(guiGraphics, texture.identifier(), minX, minY, iconWidth, iconHeight, colour);
        }
    }

    private void drawIcon(GuiGraphics guiGraphics, Identifier identifier, int minX, int minY, int iconWidth, int iconHeight, int colour) {
        var pose = guiGraphics.pose();
        pose.pushMatrix();
        if (this.isMinimap() && Configs.RotateMinimap) {
            pose.translate(minX + (float) iconWidth / 2, minY + (float) iconWidth / 2);
            pose.rotate((float) (Math.toRadians(this.playerRotation.y) - Math.PI));
            pose.translate(-minX - (float) iconWidth / 2, -minY - (float) iconWidth / 2);
        }
        drawIconStatic(guiGraphics, identifier, minX, minY, iconWidth, iconHeight, colour);
        pose.popMatrix();
    }

    public static void drawIconStatic(GuiGraphics guiGraphics, Identifier identifier, int minX, int minY, int iconWidth, int iconHeight, int colour) {
        // Skip intersection checks (GuiRenderState.hasIntersection) you would otherwise get when calling
        // GuiGraphics.blit as these checks incur a significant performance hit
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(identifier);
        BlitRenderState renderState = new BlitRenderState(RenderPipelines.GUI_TEXTURED, TextureSetup.singleTexture(texture.getTextureView(), texture.getSampler()), new Matrix3x2f(guiGraphics.pose()), minX, minY, minX + iconWidth, minY + iconHeight, 0, 1, 0, 1, colour, guiGraphics.scissorStack.peek());
        guiGraphics.guiRenderState.submitBlitToCurrentLayer(renderState);
    }

    private static final BiMap<Integer, ResourceKey<Level>> DIM_ID_TO_MC = ImmutableBiMap.of(
        Cubiomes.DIM_OVERWORLD(), Level.OVERWORLD,
        Cubiomes.DIM_NETHER(), Level.NETHER,
        Cubiomes.DIM_END(), Level.END
    );

    protected boolean isMinimap() {
        return false;
    }

    protected void updatePlayerPosition(BlockPos pos) {
        this.playerPos = pos;
    }

    protected void updatePlayerRotation(Vec2 vec2) {
        this.playerRotation = vec2;
    }

    public Vec2 getPlayerRotation() {
        return this.playerRotation;
    }

    protected int horizontalPadding() {
        return HORIZONTAL_PADDING;
    }

    //protected int verticalPadding() {
    //    return VERTICAL_PADDING;
    //}

    protected int seedMapHeight() {
        return this.height - VERTICAL_LOWER_PADDING;
    }

    protected long getSeed() {
        return this.seed;
    }

    protected int getDimension() {
        return this.dimension;
    }

    protected int getVersion() {
        return this.version;
    }

    protected int getGeneratorFlags() {
        return this.generatorFlags;
    }
}
