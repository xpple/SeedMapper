package dev.xpple.seedmapper.mixin.entity;

import dev.xpple.seedmapper.command.ClientCommandManager;
import dev.xpple.seedmapper.command.CommandReader;
import dev.xpple.seedmapper.util.chat.ChatBuilder;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void sendChatMessage(String message, CallbackInfo ci) {
        if (message.startsWith("/")) {
            if (message.startsWith(ChatBuilder.runnableCommandPrefix)) {
                Runnable runnable = ChatBuilder.runnables.get(message.split(" ")[1]);

                if (runnable != null) runnable.run();

                ci.cancel();
            }

            CommandReader reader = new CommandReader(message);
            reader.skip();

            int cursor = reader.getCursor();

            String commandName = reader.canRead() ? reader.readUnquotedString() : "";
            reader.setCursor(cursor);

            if (ClientCommandManager.isClientSideCommand(commandName)) {
                ClientCommandManager.executeCommand(reader, message);
                ci.cancel();
            }
        }
    }
}
