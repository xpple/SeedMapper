package dev.xpple.seedmapper.thread;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class SeedMapCache<K, V> {
    private final Set<K> pendingCalculations = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final ConcurrentHashMap<K, @Nullable V> cache;

    private final SeedMapExecutor executor;

    public SeedMapCache(ConcurrentHashMap<K, @Nullable V> cache, SeedMapExecutor executor) {
        this.cache = cache;
        this.executor = executor;
    }

    /// Returns the value associated with the given key if the key is present,
    /// else computes it asynchronously using the mapping function.
    /// @param key the key
    /// @param mappingFunction the key to value function
    /// @return the nonnull value iff present
    public @Nullable V computeIfAbsent(K key, Function<K, @Nullable V> mappingFunction) {
        V value = this.cache.get(key);
        if (value != null) {
            return value;
        }
        if (!this.pendingCalculations.add(key)) {
            return null;
        }
        this.executor.submitCalculation(() -> mappingFunction.apply(key)).thenAccept(data -> {
            if (data != null) {
                this.cache.put(key, data);
                this.pendingCalculations.remove(key);
            }
        });
        return null;
    }

    /// Returns the value associated with the given key if the key is present,
    /// otherwise `null`.
    /// @param key the key
    /// @return the nullable value
    public @Nullable V get(K key) {
        return this.cache.get(key);
    }
}
