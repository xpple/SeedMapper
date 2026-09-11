package dev.xpple.seedmapper.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.SDLEventHandler;
import dev.xpple.seedmapper.seedmap.SeedMapScreen;
import net.minecraft.client.Minecraft;
import org.lwjgl.sdl.SDL_Event;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SDLEventHandler.class)
public class SDLEventHandlerMixin {
    @Unique
    private static final int SDL_EVENT_PINCH_UPDATE = 0x711;

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "pollEvents", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;handleEvent(Lorg/lwjgl/sdl/SDL_Event;)V"), cancellable = true)
    private void onPollEvents(CallbackInfo ci, @Local(name = "event") SDL_Event event) {
        if (event.type() == SDL_EVENT_PINCH_UPDATE) {
            if (this.minecraft.gui.screen() instanceof SeedMapScreen seedMapScreen) {
                this.minecraft.execute(() -> seedMapScreen.pinchUpdated(event.pinch().scale()));
                ci.cancel();
            }
        }
    }
}
