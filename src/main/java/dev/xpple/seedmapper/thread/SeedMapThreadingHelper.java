package dev.xpple.seedmapper.thread;

import com.mojang.logging.LogUtils;
import dev.xpple.seedmapper.util.TwoDTree;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public final class SeedMapThreadingHelper {
    private SeedMapThreadingHelper() {
    }

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int MAX_THREADS = Math.min(Runtime.getRuntime().availableProcessors(), 5);

    private static final ExecutorService seedMapExecutor = Executors.newFixedThreadPool(MAX_THREADS);
    private static final List<CompletableFuture<?>> futures = new ArrayList<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(seedMapExecutor::shutdownNow));
    }

    public static CompletableFuture<int[]> submitBiomeCalculation(Supplier<int[]> task) {
        CompletableFuture<int[]> future = CompletableFuture.supplyAsync(() -> {
            try {
                return task.get();
            } catch (Throwable t) {
                LOGGER.error("An error occurred while executing biome calculations!", t);
                return null;
            }
        }, seedMapExecutor);
        futures.add(future);
        return future;
    }

    public static CompletableFuture<TwoDTree> submitStrongholdCalculation(Supplier<TwoDTree> task) {
        CompletableFuture<TwoDTree> future = CompletableFuture.supplyAsync(() -> {
            try {
                return task.get();
            } catch (Throwable t) {
                LOGGER.error("An error occurred while executing stronghold calculations!", t);
                return null;
            }
        }, seedMapExecutor);
        futures.add(future);
        return future;
    }

    public static void close(Runnable afterComplete) {
        CompletableFuture.allOf(futures.stream()
            .map(f -> f.handle((_, _) -> null))
            .toArray(CompletableFuture[]::new)
        ).thenRun(afterComplete);
        futures.clear();
    }
}
