package dev.xpple.seedmapper.util.config;

import com.google.common.base.Joiner;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class SeedResolution implements Iterable<SeedResolution.Method> {

    private final List<Method> methods;

    public SeedResolution() {
        this.methods = List.of(Method.values());
    }

    public SeedResolution(List<Method> methods) {
        this.methods = methods;
    }

    @NotNull
    @Override
    public Iterator<Method> iterator() {
        return this.methods.iterator();
    }

    public enum Method implements StringIdentifiable {

        COMMAND_SOURCE("CommandSource"),
        SAVED_SEEDS_CONFIG("SavedSeedsConfig"),
        ONLINE_DATABASE("OnlineDatabase"),
        SEED_CONFIG("SeedConfig");

        public static final Codec<Method> CODEC = StringIdentifiable.createCodec(Method::values);

        private final String name;

        Method(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }

        @Override
        public String toString() {
            return this.asString();
        }
    }

    @Override
    public String toString() {
        return Joiner.on(" -> ").join(this.methods);
    }
}
