package dev.xpple.seedmapper.util;

@FunctionalInterface
public interface CheckedSupplier<T, E extends Exception> {
    T get() throws E;
}
