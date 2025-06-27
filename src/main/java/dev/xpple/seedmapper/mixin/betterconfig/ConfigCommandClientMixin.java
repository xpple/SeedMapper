package dev.xpple.seedmapper.mixin.betterconfig;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import dev.xpple.betterconfig.command.client.ConfigCommandClient;
import dev.xpple.seedmapper.SeedMapper;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

@Mixin(ConfigCommandClient.class)
public class ConfigCommandClientMixin {
    @Inject(method = "register", at = @At("TAIL"))
    private static void registerConfigCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext buildContext, CallbackInfo ci) {
        CommandNode<FabricClientCommandSource> configRoot = dispatcher.getRoot().getChild("cconfig").getChild(SeedMapper.MOD_ID);
        dispatcher.register(literal("sm:config").redirect(configRoot));
    }
}
