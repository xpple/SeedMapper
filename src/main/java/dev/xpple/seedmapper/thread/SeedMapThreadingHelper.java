package dev.xpple.seedmapper.thread;

import com.mojang.logging.LogUtils;
import dev.xpple.seedmapper.config.Configs;
import dev.xpple.seedmapper.util.TwoDTree;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class SeedMapThreadingHelper {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final ExecutorService seedMapExecutor = Executors.newFixedThreadPool(Configs.SeedMapThreads);
    private final List<CompletableFuture<?>> futures = new ArrayList<>();

    public CompletableFuture<int[]> submitBiomeCalculation(Supplier<int[]> task) {
        CompletableFuture<int[]> future = CompletableFuture.supplyAsync(() -> {
            try {
                return task.get();
            } catch (Throwable t) {
                LOGGER.error("An error occurred while executing biome calculations!", t);
                return null;
            }
        }, this.seedMapExecutor);
        this.futures.add(future);
        return future;
    }

    public CompletableFuture<TwoDTree> submitStrongholdCalculation(Supplier<TwoDTree> task) {
        CompletableFuture<TwoDTree> future = CompletableFuture.supplyAsync(() -> {
            try {
                return task.get();
            } catch (Throwable t) {
                LOGGER.error("An error occurred while executing stronghold calculations!", t);
                return null;
            }
        }, this.seedMapExecutor);
        this.futures.add(future);
        return future;
    }

    public void close(Runnable afterComplete) {
        CompletableFuture.allOf(this.futures.stream()
            .map(f -> f.handle((_, _) -> null))
            .toArray(CompletableFuture[]::new)
        )
            .thenRun(this.seedMapExecutor::shutdownNow)
            .thenRun(afterComplete);
        this.futures.clear();
    }
}
