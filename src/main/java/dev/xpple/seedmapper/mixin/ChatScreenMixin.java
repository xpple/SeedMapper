package dev.xpple.seedmapper.mixin;

import dev.xpple.seedmapper.util.RunnableClickEventActionHelper;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Inject(method = "handleComponentClicked", at = @At("HEAD"), cancellable = true)
    private void executeCode(Style style, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        if (style == null) {
            return;
        }
        if (!(style.getClickEvent() instanceof ClickEvent.ChangePage(int page))) {
            return;
        }
        Runnable runnable = RunnableClickEventActionHelper.runnables.get(page);
        if (runnable == null) {
            return;
        }
        runnable.run();
        cir.setReturnValue(true);
    }
}
