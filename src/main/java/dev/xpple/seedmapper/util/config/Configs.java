package dev.xpple.seedmapper.util.config;

import com.google.common.base.Joiner;
import com.google.common.primitives.Primitives;
import net.minecraft.block.Block;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static dev.xpple.seedmapper.SeedMapper.CLIENT;

@SuppressWarnings("unused")
public class Configs {

    @Config
    public static Long Seed = ConfigHelper.initSeed();

    @Config(adder = "addSavedSeed", putter = "none")
    public static final Map<String, Long> SavedSeeds = ConfigHelper.initSavedSeeds();
    public static void addSavedSeed(Object seed) {
        String key = CLIENT.getNetworkHandler().getConnection().getAddress().toString();
        SavedSeeds.put(key, (Long) seed);
    }

    @Config
    public static final List<Block> IgnoredBlocks = ConfigHelper.initIgnoredBlocks();

    @Config
    public static boolean AutoOverlay = ConfigHelper.initAutoOverlay();

    @Config
    public static final Map<Block, Integer> BlockColours = ConfigHelper.initBlockColours();

    public static Object get(String config) {
        Field field = configs.get(config);
        if (field == null) {
            throw new IllegalArgumentException();
        }
        try {
            return field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    public static String toString(String config) {
        Object value = get(config);
        String toString;
        if (value instanceof List<?> list) {
            toString = "[" + Joiner.on(", ").join(list) + "]";
        } else if (value instanceof Map<?, ?> map) {
            toString = "{" + Joiner.on(", ").withKeyValueSeparator("=").join(map) + "}";
        } else {
            toString = String.valueOf(value);
        }
        return toString;
    }

    public static void set(String config, Object value) {
        Consumer<Object> setter = setters.get(config);
        if (setter == null) {
            throw new IllegalArgumentException();
        }
        setter.accept(value);
        ConfigHelper.save();
    }

    public static void add(String config, Object value) {
        Consumer<Object> adder = adders.get(config);
        if (adder == null) {
            throw new IllegalArgumentException();
        }
        adder.accept(value);
        ConfigHelper.save();
    }

    public static void put(String config, Object key, Object value) {
        BiConsumer<Object, Object> putter = putters.get(config);
        if (putter == null) {
            throw new IllegalArgumentException();
        }
        putter.accept(key, value);
        ConfigHelper.save();
    }

    public static void remove(String config, Object value) {
        Consumer<Object> remover = removers.get(config);
        if (remover == null) {
            throw new IllegalArgumentException();
        }
        remover.accept(value);
        ConfigHelper.save();
    }

    public static Class<?> getType(String name) {
        Field field = configs.get(name);
        if (field == null) {
            throw new IllegalArgumentException();
        }
        return field.getType();
    }

    public static Type[] getParameterTypes(String name) {
        Field field = configs.get(name);
        if (field == null) {
            throw new IllegalArgumentException();
        }
        return ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
    }

    public static Set<String> getConfigs() {
        return configs.keySet();
    }

    public static Set<String> getSetters() {
        return setters.keySet();
    }

    public static Set<String> getAdders() {
        return adders.keySet();
    }

    public static Set<String> getPutters() {
        return putters.keySet();
    }

    public static Set<String> getRemovers() {
        return removers.keySet();
    }

    private static final Map<String, Field> configs = new HashMap<>();
    private static final Map<String, Consumer<Object>> setters = new HashMap<>();
    private static final Map<String, Consumer<Object>> adders = new HashMap<>();
    private static final Map<String, BiConsumer<Object, Object>> putters = new HashMap<>();
    private static final Map<String, Consumer<Object>> removers = new HashMap<>();

    static {
        for (Field field : Configs.class.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Config.class)) {
                continue;
            }
            String fieldName = field.getName();
            configs.put(fieldName, field);
            Config annotation = field.getAnnotation(Config.class);
            Class<?> type = field.getType();
            if (type.isAssignableFrom(List.class)) {
                String adder = annotation.adder();
                if (adder.equals("none")) {
                    // do nothing
                } else if (adder.isEmpty()) {
                    Method add;
                    try {
                        add = List.class.getDeclaredMethod("add", Object.class);
                    } catch (ReflectiveOperationException e) {
                        throw new AssertionError(e);
                    }
                    adders.put(fieldName, value -> {
                        try {
                            add.invoke(field.get(null), value);
                        } catch (ReflectiveOperationException e) {
                            throw new AssertionError(e);
                        }
                    });
                } else {
                    Method adderMethod;
                    try {
                        adderMethod = Configs.class.getDeclaredMethod(adder, Object.class);
                    } catch (ReflectiveOperationException e) {
                        throw new AssertionError(e);
                    }
                    adders.put(fieldName, value -> {
                        try {
                            adderMethod.invoke(null, value);
                        } catch (ReflectiveOperationException e) {
                            throw new AssertionError(e);
                        }
                    });
                }
                String remover = annotation.remover();
                if (remover.equals("none")) {
                    // do nothing
                } else if (remover.isEmpty()) {
                    Method remove;
                    try {
                        remove = List.class.getDeclaredMethod("remove", Object.class);
                    } catch (ReflectiveOperationException e) {
                        throw new AssertionError(e);
                    }
                    removers.put(fieldName, value -> {
                        try {
                            remove.invoke(field.get(null), value);
                        } catch (ReflectiveOperationException e) {
                            throw new AssertionError(e);
                        }
                    });
                } else {
                    Method removerMethod;
                    try {
                        removerMethod = Configs.class.getDeclaredMethod(adder, Object.class);
                    } catch (ReflectiveOperationException e) {
                        throw new AssertionError(e);
                    }
                    removers.put(fieldName, value -> {
                        try {
                            removerMethod.invoke(null, value);
                        } catch (ReflectiveOperationException e) {
                            throw new AssertionError(e);
                        }
                    });
                }
            } else if (type.isAssignableFrom(Map.class)) {
                String adder = annotation.adder();
                if (adder.equals("none")) {
                    // do nothing
                } else if (!adder.isEmpty()) {
                    Method adderMethod;
                    try {
                        adderMethod = Configs.class.getDeclaredMethod(adder, Object.class);
                    } catch (ReflectiveOperationException e) {
                        throw new AssertionError(e);
                    }
                    adders.put(fieldName, value -> {
                        try {
                            adderMethod.invoke(null, value);
                        } catch (ReflectiveOperationException e) {
                            throw new AssertionError(e);
                        }
                    });
                }
                String putter = annotation.putter();
                if (putter.equals("none")) {
                    // do nothing
                } else if (putter.isEmpty()) {
                    Method put;
                    try {
                        put = Map.class.getDeclaredMethod("put", Object.class, Object.class);
                    } catch (ReflectiveOperationException e) {
                        throw new AssertionError(e);
                    }
                    putters.put(fieldName, (key, value) -> {
                        try {
                            put.invoke(field.get(null), key, value);
                        } catch (ReflectiveOperationException e) {
                            throw new AssertionError(e);
                        }
                    });
                } else {
                    Method putterMethod;
                    try {
                        putterMethod = Configs.class.getDeclaredMethod(putter, Object.class, Object.class);
                    } catch (ReflectiveOperationException e) {
                        throw new AssertionError(e);
                    }
                    putters.put(fieldName, (key, value) -> {
                        try {
                            putterMethod.invoke(null, key, value);
                        } catch (ReflectiveOperationException e) {
                            throw new AssertionError(e);
                        }
                    });
                }
                String remover = annotation.remover();
                if (remover.equals("none")) {
                    // do nothing
                } else if (remover.isEmpty()) {
                    Method remove;
                    try {
                        remove = Map.class.getDeclaredMethod("remove", Object.class);
                    } catch (ReflectiveOperationException e) {
                        throw new AssertionError(e);
                    }
                    removers.put(fieldName, value -> {
                        try {
                            remove.invoke(field.get(null), value);
                        } catch (ReflectiveOperationException e) {
                            throw new AssertionError(e);
                        }
                    });
                } else {
                    Method removerMethod;
                    try {
                        removerMethod = Configs.class.getDeclaredMethod(putter, Object.class);
                    } catch (ReflectiveOperationException e) {
                        throw new AssertionError(e);
                    }
                    removers.put(fieldName, value -> {
                        try {
                            removerMethod.invoke(null, value);
                        } catch (ReflectiveOperationException e) {
                            throw new AssertionError(e);
                        }
                    });
                }
            } else {
                String setter = annotation.setter();
                if (setter.equals("none")) {
                    continue;
                }
                if (setter.isEmpty()) {
                    setters.put(fieldName, value -> {
                        try {
                            field.set(null, value);
                        } catch (ReflectiveOperationException e) {
                            throw new AssertionError(e);
                        }
                    });
                } else {
                    Method setterMethod;
                    try {
                        setterMethod = Configs.class.getDeclaredMethod(setter, type);
                    } catch (ReflectiveOperationException e) {
                        throw new AssertionError(e);
                    }
                    setters.put(fieldName, value -> {
                        try {
                            setterMethod.invoke(null, value);
                        } catch (ReflectiveOperationException e) {
                            throw new AssertionError(e);
                        }
                    });
                }
            }
        }
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Config {
        String setter() default "";
        String adder() default "";
        String putter() default "";
        String remover() default "";
    }
}
