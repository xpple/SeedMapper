package dev.xpple.seedmapper.thread;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class SeedMapCache<K, V> {
    private final ObjectSet<K> pendingCalculations = ObjectSets.synchronize(new ObjectOpenHashSet<>());
    private final Object2ObjectMap<K, V> cache;

    private final SeedMapExecutor executor;

    public SeedMapCache(Object2ObjectMap<K, V> cache, SeedMapExecutor executor) {
        this.cache = cache;
        this.executor = executor;
    }

    /**
     * Returns the value associated with the given key if the key is present,
     * else computes it asynchronously using the mapping function.
     * @param key the key
     * @param mappingFunction the key to value function
     * @return the nonnull value iff present
     */
    public @Nullable V computeIfAbsent(K key, Function<K, V> mappingFunction) {
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

    /**
     * Returns the value associated with the given key if the key is present,
     * otherwise {@code null}.
     * @param key the key
     * @return the nullable value
     */
    public @Nullable V get(K key) {
        return this.cache.get(key);
    }
}
