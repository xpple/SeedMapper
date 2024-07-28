package dev.xpple.seedmapper.mixin.server;

import dev.xpple.seedmapper.simulation.FakeLogger;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
    @SuppressWarnings("ShadowModifiers")
    @Shadow private static Logger LOGGER;

    static {
        LOGGER = new FakeLogger(LOGGER);
    }
}
