package dev.xpple.seedmapper.simulation;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.VanillaDataPackProvider;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.SaveLoading;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.ApiServices;
import net.minecraft.util.Util;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class SimulatedServer extends IntegratedServer {

    private static final MinecraftClient client = MinecraftClient.getInstance();

    private SimulatedServer(Thread serverThread, MinecraftClient client, LevelStorage.Session session, ResourcePackManager resourcePackManager, SaveLoader saveLoader, ApiServices apiServices, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory) {
        super(serverThread, client, session, resourcePackManager, saveLoader, apiServices, worldGenerationProgressListenerFactory);
    }

    public static SimulatedServer newServer(long seed) throws Exception {
        LevelStorage.Session session = FakeLevelStorage.create().createSession("fake");

        ResourcePackManager resourcePackManager = new ResourcePackManager(new VanillaDataPackProvider());

        LevelInfo levelInfo = new LevelInfo("fake", GameMode.SPECTATOR, false, Difficulty.PEACEFUL, true, new GameRules(), DataConfiguration.SAFE_MODE);

        Function<DynamicRegistryManager, DimensionOptionsRegistryHolder> dimensionsRegistrySupplier = WorldPresets::createDemoOptions;

        GeneratorOptions generatorOptions = new GeneratorOptions(seed, true, false);

        SaveLoader saveLoader = createSaveLoader(resourcePackManager, levelInfo, dimensionsRegistrySupplier, generatorOptions);

        ApiServices apiServices = new ApiServices(new FakeMinecraftSessionService(), SignatureVerifier.NOOP, new FakeGameProfileRepository(), null);

        WorldGenerationProgressListenerFactory factory = r -> FakeWorldGenerationProgressListener.INSTANCE;

        return new SimulatedServer(null, client, session, resourcePackManager, saveLoader, apiServices, factory);
        //return MinecraftServer.startServer(thread -> new SimulatedServer(thread, client, session, resourcePackManager, saveLoader, apiServices, factory));
    }

    private static SaveLoader createSaveLoader(ResourcePackManager resourcePackManager, LevelInfo levelInfo, Function<DynamicRegistryManager, DimensionOptionsRegistryHolder> dimensionsRegistrySupplier, GeneratorOptions generatorOptions) throws Exception {
        SaveLoading.DataPacks dataPacks = new SaveLoading.DataPacks(resourcePackManager, levelInfo.getDataConfiguration(), false, false);
        return load(dataPacks, context -> {
            DimensionOptionsRegistryHolder.DimensionsConfig dimensionsConfig = dimensionsRegistrySupplier.apply(context.worldGenRegistryManager()).toConfig(context.dimensionsRegistryManager().get(RegistryKeys.DIMENSION));
            return new SaveLoading.LoadContext<>(new LevelProperties(levelInfo, generatorOptions, dimensionsConfig.specialWorldProperty(), dimensionsConfig.getLifecycle()), dimensionsConfig.toDynamicRegistryManager());
        }, SaveLoader::new);
    }

    private static SaveLoader load(SaveLoading.DataPacks dataPacks, SaveLoading.LoadContextSupplier<LevelProperties> loadContextSupplier, SaveLoading.SaveApplierFactory<LevelProperties, SaveLoader> saveApplierFactory) throws Exception {
        SaveLoading.ServerConfig serverConfig = new SaveLoading.ServerConfig(dataPacks, CommandManager.RegistrationEnvironment.INTEGRATED, 2);
        CompletableFuture<SaveLoader> completableFuture = SaveLoading.load(serverConfig, loadContextSupplier, saveApplierFactory, Util.getMainWorkerExecutor(), client);
        client.runTasks(completableFuture::isDone);
        return completableFuture.get();
    }

    @Override
    public boolean save(boolean suppressLogs, boolean flush, boolean force) {
        return true;
    }

    @Override
    public boolean saveAll(boolean suppressLogs, boolean flush, boolean force) {
        return true;
    }
}
