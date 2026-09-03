package dev.xpple.seedmapper.util;

import java.lang.ref.SoftReference;
import java.util.function.Supplier;

public class RenewableSoftReference<T> {
    private final Supplier<T> supplier;
    private volatile SoftReference<T> reference;

    public RenewableSoftReference(Supplier<T> supplier) {
        this.supplier = supplier;
        this.reference = new SoftReference<>(supplier.get());
    }

    public T get() {
        T t = this.reference.get();
        if (t != null) {
            return t;
        }
        synchronized (this) {
            t = this.reference.get();
            if (t == null) {
                t = this.supplier.get();
                this.reference = new SoftReference<>(t);
            }
            return t;
        }
    }
}
