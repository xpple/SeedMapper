package dev.xpple.seedmapper.seedmap;

import com.github.cubiomes.CanyonCarverConfig;
import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.Generator;
import com.github.cubiomes.OreVeinParameters;
import com.github.cubiomes.Pos;
import com.github.cubiomes.Range;
import com.github.cubiomes.StructureConfig;
import com.github.cubiomes.TerrainNoise;
import dev.xpple.seedmapper.SeedMapper;
import dev.xpple.seedmapper.command.arguments.CanyonCarverArgument;
import dev.xpple.seedmapper.command.commands.LocateCommand;
import dev.xpple.seedmapper.config.Configs;
import dev.xpple.seedmapper.feature.StructureChecks;
import dev.xpple.seedmapper.thread.SeedMapCache;
import dev.xpple.seedmapper.thread.SeedMapExecutor;
import dev.xpple.seedmapper.util.BiomeSeedIdentifier;
import dev.xpple.seedmapper.util.BiomeSeedIdentifierWithDimension;
import dev.xpple.seedmapper.util.QuartPos2;
import dev.xpple.seedmapper.util.RegionPos;
import dev.xpple.seedmapper.util.SeedIdentifierWithDimension;
import dev.xpple.seedmapper.util.TwoDTree;
import it.unimi.dsi.fastutil.ints.AbstractIntCollection;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import org.jspecify.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import dev.xpple.seedmapper.util.RenewableSoftReference;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToIntBiFunction;
import java.util.stream.IntStream;

public class SeedMapData {
    public static final int BIOME_SCALE = 4;
    public static final int SCALED_CHUNK_SIZE = SectionPos.SECTION_SIZE / BIOME_SCALE;

    private static final RenewableSoftReference<Object2ObjectMap<BiomeSeedIdentifierWithDimension, ConcurrentHashMap<ObjectIntPair<TilePos>, int[]>>> biomeDataCache = new RenewableSoftReference<>(Object2ObjectOpenHashMap::new);
    private static final RenewableSoftReference<Object2ObjectMap<SeedIdentifierWithDimension, Object2ObjectMap<ChunkPos, ChunkStructureData>>> structureDataCache = new RenewableSoftReference<>(Object2ObjectOpenHashMap::new);
    public static final RenewableSoftReference<Object2ObjectMap<BiomeSeedIdentifier, @Nullable TwoDTree>> strongholdDataCache = new RenewableSoftReference<>(Object2ObjectOpenHashMap::new);
    private static final RenewableSoftReference<Object2ObjectMap<BiomeSeedIdentifier, ConcurrentHashMap<TilePos, OreVeinData>>> oreVeinDataCache = new RenewableSoftReference<>(Object2ObjectOpenHashMap::new);
    private static final RenewableSoftReference<Object2ObjectMap<BiomeSeedIdentifier, Object2ObjectMap<TilePos, BitSet>>> canyonDataCache = new RenewableSoftReference<>(Object2ObjectOpenHashMap::new);
    private static final RenewableSoftReference<Object2ObjectMap<BiomeSeedIdentifier, ConcurrentHashMap<TilePos, BitSet>>> slimeChunkDataCache = new RenewableSoftReference<>(Object2ObjectOpenHashMap::new);
    private static final RenewableSoftReference<Object2ObjectMap<BiomeSeedIdentifier, BlockPos>> spawnDataCache = new RenewableSoftReference<>(Object2ObjectOpenHashMap::new);

    private final SeedMapExecutor seedMapExecutor = new SeedMapExecutor();

    private final Arena arena = Arena.ofShared();

    private final long seed;
    private final int dimension;
    private final int version;
    private final int generatorFlags;
    private final BiomeSeedIdentifierWithDimension biomeSeedIdentifierWithDimension;
    private final SeedIdentifierWithDimension seedIdentifierWithDimension;

    /// [TerrainNoise] to be used for structure calculations. This is NOT thread safe.
    private final MemorySegment structureGenerator;
    /// [Generator] to be used for biome calculations. This is thread safe.
    private final MemorySegment biomeGenerator;
    private final @Nullable MemorySegment[] structureConfigs;
    private final PositionalRandomFactory oreVeinRandom;
    private final MemorySegment oreVeinParameters;
    private final @Nullable MemorySegment[] canyonCarverConfigs;

    private final SeedMapCache<ObjectIntPair<TilePos>, int[]> biomeCache;
    private final Object2ObjectMap<ChunkPos, ChunkStructureData> structureCache;
    private final SeedMapCache<TilePos, OreVeinData> oreVeinCache;
    private final Object2ObjectMap<TilePos, BitSet> canyonCache;
    private final SeedMapCache<TilePos, BitSet> slimeChunkCache;

    private final ObjectArrayList<MapFeature> availableFeatures;

    public SeedMapData(SeedIdentifierWithDimension seedIdentifierWithDimension) {
        this.seedIdentifierWithDimension = seedIdentifierWithDimension;
        this.seed = this.seedIdentifierWithDimension.seed();
        this.version = this.seedIdentifierWithDimension.version();
        this.generatorFlags = this.seedIdentifierWithDimension.generatorFlags();
        this.dimension = this.seedIdentifierWithDimension.dimension();
        this.biomeSeedIdentifierWithDimension = new BiomeSeedIdentifierWithDimension(this.seed, this.version, this.generatorFlags, this.dimension);

        this.structureGenerator = TerrainNoise.allocate(arena);
        Cubiomes.setupTerrainNoise(this.structureGenerator, this.version, this.generatorFlags);
        Cubiomes.initTerrainNoise(this.structureGenerator, this.seed, this.dimension);
        this.biomeGenerator = Generator.allocate(this.arena);
        this.biomeGenerator.copyFrom(TerrainNoise.g(this.structureGenerator));

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

        this.availableFeatures = Arrays.stream(MapFeature.values())
            .filter(feature -> feature.getDimension() == this.dimension || feature.getDimension() == Cubiomes.DIM_UNDEF())
            .filter(feature -> this.version >= feature.availableSince())
            .sorted(Comparator.comparing(MapFeature::getName))
            .collect(ObjectArrayList::new, ObjectArrayList::add, ObjectArrayList::addAll);

        this.biomeCache = new SeedMapCache<>(biomeDataCache.get().computeIfAbsent(this.biomeSeedIdentifierWithDimension, _ -> new ConcurrentHashMap<>()), this.seedMapExecutor);
        this.structureCache = structureDataCache.get().computeIfAbsent(this.seedIdentifierWithDimension, _ -> new Object2ObjectOpenHashMap<>());
        this.slimeChunkCache = new SeedMapCache<>(slimeChunkDataCache.get().computeIfAbsent(this.biomeSeedIdentifierWithDimension.biomeSeedIdentifier(), _ -> new ConcurrentHashMap<>()), this.seedMapExecutor);
        this.oreVeinCache = new SeedMapCache<>(oreVeinDataCache.get().computeIfAbsent(this.biomeSeedIdentifierWithDimension.biomeSeedIdentifier(), _ -> new ConcurrentHashMap<>()), this.seedMapExecutor);
        this.canyonCache = canyonDataCache.get().computeIfAbsent(this.biomeSeedIdentifierWithDimension.biomeSeedIdentifier(), _ -> new Object2ObjectOpenHashMap<>());

        if (this.availableFeatures.contains(MapFeature.STRONGHOLD) && !strongholdDataCache.get().containsKey(this.biomeSeedIdentifierWithDimension.biomeSeedIdentifier())) {
            this.seedMapExecutor.submitCalculation(() -> LocateCommand.calculateStrongholds(this.seed, this.version, this.generatorFlags))
                .thenAccept(tree -> {
                    if (tree != null) {
                        strongholdDataCache.get().put(this.biomeSeedIdentifierWithDimension.biomeSeedIdentifier(), tree);
                    }
                });
        }
    }

    public int @Nullable [] getBiomeData(ObjectIntPair<TilePos> pair) {
        return this.biomeCache.computeIfAbsent(pair, p -> this.calculateBiomeData(p.left(), p.rightInt()));
    }

    public @Nullable BitSet getSlimeChunkData(TilePos tilePos) {
        return this.slimeChunkCache.computeIfAbsent(tilePos, this::calculateSlimeChunkData);
    }

    public @Nullable MemorySegment getStructureConfig(int structure) {
        return this.structureConfigs[structure];
    }

    public ChunkStructureData getChunkStructureData(ChunkPos chunkPos) {
        return this.structureCache.computeIfAbsent(chunkPos, _ -> new ChunkStructureData(chunkPos, new Int2ObjectArrayMap<>()));
    }

    public @Nullable StructureData getStructureData(ChunkStructureData chunkStructureData, MapFeature feature, RegionPos regionPos, MemorySegment structurePos, StructureChecks.GenerationCheck generationCheck) {
        return chunkStructureData.structures().computeIfAbsent(feature.getStructureId(), _ -> this.calculateStructureData(feature, regionPos, structurePos, generationCheck));
    }

    public @Nullable TwoDTree getStrongholdData() {
        return strongholdDataCache.get().get(this.biomeSeedIdentifierWithDimension.biomeSeedIdentifier());
    }

    public @Nullable OreVeinData getOreVeinData(TilePos tilePos) {
        return this.oreVeinCache.computeIfAbsent(tilePos, this::calculateOreVein);
    }

    public BitSet getCanyonData(TilePos tilePos) {
        return this.canyonCache.computeIfAbsent(tilePos, this::calculateCanyonData);
    }

    public BlockPos getSpawn() {
        return spawnDataCache.get().computeIfAbsent(this.biomeSeedIdentifierWithDimension.biomeSeedIdentifier(), _ -> this.calculateSpawnData());
    }

    private int[] calculateBiomeData(TilePos tilePos, int seedMapBiomeY) {
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
            Range.y(range, seedMapBiomeY / Range.scale(range));
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
                RandomSource random = WorldgenRandom.seedSlimeChunk(chunkPos.x() + relChunkX, chunkPos.z() + relChunkZ, this.seed, 987234911L);
                slimeChunks.set(relChunkX + relChunkZ * TilePos.TILE_SIZE_CHUNKS, random.nextInt(10) == 0);
            }
        }
        return slimeChunks;
    }

    private @Nullable StructureData calculateStructureData(MapFeature feature, RegionPos regionPos, MemorySegment structurePos, StructureChecks.GenerationCheck generationCheck) {
        if (!generationCheck.check(this.structureGenerator, regionPos.x(), regionPos.z(), structurePos)) {
            return null;
        }

        BlockPos pos = new BlockPos(Pos.x(structurePos), 0, Pos.z(structurePos));
        OptionalInt optionalBiome = getBiome(QuartPos2.fromBlockPos(pos));
        MapFeature.Texture texture;
        if (optionalBiome.isEmpty()) {
            texture = feature.getDefaultTexture();
        } else {
            texture = feature.getVariantTexture(this.structureGenerator, this.seedIdentifierWithDimension, pos.getX(), pos.getZ(), optionalBiome.getAsInt());
        }
        return new StructureData(pos, texture);
    }

    private @Nullable OreVeinData calculateOreVein(TilePos tilePos) {
        ChunkPos chunkPos = tilePos.toChunkPos();
        for (int relChunkX = 0; relChunkX < TilePos.TILE_SIZE_CHUNKS; relChunkX++) {
            for (int relChunkZ = 0; relChunkZ < TilePos.TILE_SIZE_CHUNKS; relChunkZ++) {
                int minBlockX = SectionPos.sectionToBlockCoord(chunkPos.x() + relChunkZ);
                int minBlockZ = SectionPos.sectionToBlockCoord(chunkPos.z() + relChunkZ);
                RandomSource rnd = this.oreVeinRandom.at(minBlockX, 0, minBlockZ);
                BlockPos pos = new BlockPos(minBlockX + rnd.nextInt(SectionPos.SECTION_SIZE), 0, minBlockZ + rnd.nextInt(SectionPos.SECTION_SIZE));
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
                    int chunkX = chunkPos.x() + relChunkX;
                    int chunkZ = chunkPos.z() + relChunkZ;
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

    public int getBiomeYHeight() {
        if (this.dimension == Cubiomes.DIM_OVERWORLD()) {
            return Configs.SeedMapBiomeY;
        }
        return 64;
    }

    public OptionalInt getBiome(QuartPos2 pos) {
        TilePos tilePos = TilePos.fromQuartPos(pos);
        ObjectIntPair<TilePos> pair = ObjectIntPair.of(tilePos, this.getBiomeYHeight());
        int[] biomeCache = this.biomeCache.get(pair);
        if (biomeCache == null) {
            return OptionalInt.empty();
        }
        QuartPos2 quartPos = QuartPos2.fromTilePos(tilePos);
        QuartPos2 relQuartPos = pos.subtract(quartPos);
        return OptionalInt.of(biomeCache[relQuartPos.x() + relQuartPos.z() * Tile.TEXTURE_SIZE]);
    }

    private BlockPos calculateSpawnData() {
        try (Arena tempArena = Arena.ofConfined()) {
            MemorySegment pos = Cubiomes.getSpawn(tempArena, this.biomeGenerator);
            return new BlockPos(Pos.x(pos), 0, Pos.z(pos));
        }
    }

    public void close() {
        this.seedMapExecutor.close(this.arena::close);
        Configs.save();
    }

    public List<MapFeature> getAvailableFeatures() {
        return this.availableFeatures;
    }

    public SeedIdentifierWithDimension getSeedIdentifierWithDimension() {
        return this.seedIdentifierWithDimension;
    }
}
