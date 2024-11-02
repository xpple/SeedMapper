// Generated by jextract

package com.github.cubiomes;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

/**
 * {@snippet lang=c :
 * typedef int (mapfunc_t)(const struct Layer {
 *     mapfunc_t *getMap;
 *     int8_t mc;
 *     int8_t zoom;
 *     int8_t edge;
 *     int scale;
 *     uint64_t layerSalt;
 *     uint64_t startSalt;
 *     uint64_t startSeed;
 *     void *noise;
 *     void *data;
 *     Layer *p;
 *     Layer *p2;
 * } *, int *, int, int, int, int)
 * }
 */
public class mapfunc_t {

    mapfunc_t() {
        // Should not be called directly
    }

    /**
     * The function pointer signature, expressed as a functional interface
     */
    public interface Function {
        int apply(MemorySegment _x0, MemorySegment _x1, int _x2, int _x3, int _x4, int _x5);
    }

    private static final FunctionDescriptor $DESC = FunctionDescriptor.of(
        CubiomesHeaders.C_INT,
        CubiomesHeaders.C_POINTER,
        CubiomesHeaders.C_POINTER,
        CubiomesHeaders.C_INT,
        CubiomesHeaders.C_INT,
        CubiomesHeaders.C_INT,
        CubiomesHeaders.C_INT
    );

    /**
     * The descriptor of this function pointer
     */
    public static FunctionDescriptor descriptor() {
        return $DESC;
    }

    private static final MethodHandle UP$MH = CubiomesHeaders.upcallHandle(Function.class, "apply", $DESC);

    /**
     * Allocates a new upcall stub, whose implementation is defined by {@code fi}.
     * The lifetime of the returned segment is managed by {@code arena}
     */
    public static MemorySegment allocate(Function fi, Arena arena) {
        return Linker.nativeLinker().upcallStub(UP$MH.bindTo(fi), $DESC, arena);
    }

    private static final MethodHandle DOWN$MH = Linker.nativeLinker().downcallHandle($DESC);

    /**
     * Invoke the upcall stub {@code funcPtr}, with given parameters
     */
    public static int invoke(MemorySegment funcPtr,MemorySegment _x0, MemorySegment _x1, int _x2, int _x3, int _x4, int _x5) {
        try {
            return (int) DOWN$MH.invokeExact(funcPtr, _x0, _x1, _x2, _x3, _x4, _x5);
        } catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }
}

