package dev.xpple.seedmapper.mixin.simplewaypoints;

import dev.xpple.simplewaypoints.impl.SerializationHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SerializationHelper.class)
public interface SerializationHelperInvoker {
    @Invoker
    static String invokeUpgradeWorldIdentifier(String worldIdentifier) {
        throw new AssertionError();
    }
}
