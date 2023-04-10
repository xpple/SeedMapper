package dev.xpple.seedmapper.mixin.recipe;

import dev.xpple.seedmapper.simulation.FakeLogger;
import net.minecraft.recipe.RecipeManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RecipeManager.class)
public class MixinRecipeManager {
    @SuppressWarnings("ShadowModifiers")
    @Shadow private static Logger LOGGER;

    static {
        LOGGER = new FakeLogger(LOGGER);
    }
}
