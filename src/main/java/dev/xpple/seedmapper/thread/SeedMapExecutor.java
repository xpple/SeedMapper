package dev.xpple.seedmapper.thread;

import com.mojang.logging.LogUtils;
import dev.xpple.seedmapper.config.Configs;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class SeedMapExecutor {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final ExecutorService seedMapExecutor = Executors.newFixedThreadPool(Configs.SeedMapThreads);
    private final List<CompletableFuture<?>> futures = new ArrayList<>();

    public <T> CompletableFuture<T> submitCalculation(Supplier<T> task) {
        CompletableFuture<T> future = CompletableFuture.supplyAsync(() -> {
            try {
                return task.get();
            } catch (Throwable t) {
                LOGGER.error("An error occurred while performing seed map calculations!", t);
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
