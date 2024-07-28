package dev.xpple.seedmapper.mixin.fabric;

import dev.xpple.seedmapper.simulation.FakeLogger;
import net.fabricmc.fabric.impl.biome.modification.BiomeModificationImpl;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BiomeModificationImpl.class)
public class MixinBiomeModificationImpl {
    @SuppressWarnings("ShadowModifiers")
    @Shadow private static Logger LOGGER;

    static {
        LOGGER = new FakeLogger(LOGGER);
    }
}
