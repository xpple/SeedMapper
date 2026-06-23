package dev.xpple.seedmapper.jni;

import com.github.cubiomes.Cubiomes;
import com.github.cubiomes.StructureConfig;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.permissions.PermissionSet;
import org.jspecify.annotations.Nullable;

import java.lang.foreign.MemorySegment;

@SuppressWarnings("unused")
public class StructureConfigOverride {
    public static int getStructureConfig_override(int stype, int mc, long sconf) {
        MemorySegment structureConfig = MemorySegment.ofAddress(sconf).reinterpret(StructureConfig.sizeof());
        if (Cubiomes.getStructureConfig(stype, mc, structureConfig) == 0) {
            return 0;
        }
        CustomClientCommandSource commandSource = makeCommandSource();
        if (commandSource == null) {
            return 1;
        }
        Integer salt;
        try {
            salt = commandSource.getCustomStructureSalts().get(stype);
        } catch (CommandSyntaxException _) {
            return 1;
        }
        if (salt != null) {
            StructureConfig.salt(structureConfig, salt);
        }
        return 1;
    }

    private static @Nullable CustomClientCommandSource makeCommandSource() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientPacketListener connection = minecraft.getConnection();
        if (connection == null) {
            return null;
        }
        PermissionSet playerPermissions = permission -> {
            LocalPlayer player = minecraft.player;
            return player != null && player.permissions().hasPermission(permission);
        };

        ClientSuggestionProvider suggestionProvider = new ClientSuggestionProvider(connection, minecraft, playerPermissions.union(ClientPacketListener.ALLOW_RESTRICTED_COMMANDS));
        return CustomClientCommandSource.of((FabricClientCommandSource) suggestionProvider);
    }
}
