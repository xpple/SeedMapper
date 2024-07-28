package dev.xpple.seedmapper.mixin.advancement;

import dev.xpple.seedmapper.simulation.FakeLogger;
import net.minecraft.advancement.AdvancementManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AdvancementManager.class)
public class MixinAdvancementManager {
    @SuppressWarnings("ShadowModifiers")
    @Shadow private static Logger LOGGER;

    static {
        LOGGER = new FakeLogger(LOGGER);
    }
}
