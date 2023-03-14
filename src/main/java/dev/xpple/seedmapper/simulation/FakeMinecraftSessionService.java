package dev.xpple.seedmapper.simulation;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;

public class FakeMinecraftSessionService implements MinecraftSessionService {
    @Override
    public void joinServer(GameProfile profile, String authenticationToken, String serverId) {
    }

    @Override
    public GameProfile hasJoinedServer(GameProfile user, String serverId, InetAddress address) {
        return null;
    }

    @Override
    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTextures(GameProfile profile, boolean requireSecure) {
        return Collections.emptyMap();
    }

    @Override
    public GameProfile fillProfileProperties(GameProfile profile, boolean requireSecure) {
        return null;
    }

    @Override
    public String getSecurePropertyValue(Property property) throws InsecurePublicKeyException {
        return property.getValue();
    }
}
