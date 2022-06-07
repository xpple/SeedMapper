package dev.xpple.seedmapper.command;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;

import static dev.xpple.seedmapper.SeedMapper.CLIENT;

public class CustomClientCommandSource extends ClientCommandSource implements FabricClientCommandSource {

    private final Entity entity;
    private final Vec3d position;
    private final Vec2f rotation;
    private final ClientWorld world;
    private final Map<String, Object> meta;

    public CustomClientCommandSource(ClientPlayNetworkHandler networkHandler, MinecraftClient client, Entity entity, Vec3d position, Vec2f rotation, ClientWorld world, Map<String, Object> meta) {
        super(networkHandler, client);

        this.entity = entity;
        this.position = position;
        this.rotation = rotation;
        this.world = world;
        this.meta = meta;
    }

    public static CustomClientCommandSource of(FabricClientCommandSource source) {
        if (source instanceof CustomClientCommandSource custom) {
            return custom;
        }
        return new CustomClientCommandSource(CLIENT.getNetworkHandler(), CLIENT, source.getEntity(), source.getPosition(), source.getRotation(), source.getWorld(), new HashMap<>());
    }

    @Override
    public void sendFeedback(Text message) {
    }

    @Override
    public void sendError(Text message) {
    }

    @Override
    public MinecraftClient getClient() {
        return CLIENT;
    }

    @Override
    public ClientPlayerEntity getPlayer() {
        return getClient().player;
    }

    @Override
    public Entity getEntity() {
        return this.entity;
    }

    @Override
    public Vec3d getPosition() {
        return this.position;
    }

    @Override
    public Vec2f getRotation() {
        return this.rotation;
    }

    @Override
    public ClientWorld getWorld() {
        return this.world;
    }

    @Override
    public Object getMeta(String key) {
        return this.meta.get(key);
    }

    public CustomClientCommandSource withEntity(Entity entity) {
        return new CustomClientCommandSource(CLIENT.getNetworkHandler(), CLIENT, entity, this.position, this.rotation, this.world, this.meta);
    }

    public CustomClientCommandSource withPosition(Vec3d position) {
        return new CustomClientCommandSource(CLIENT.getNetworkHandler(), CLIENT, this.entity, position, this.rotation, this.world, this.meta);
    }

    public CustomClientCommandSource withRotation(Vec2f rotation) {
        return new CustomClientCommandSource(CLIENT.getNetworkHandler(), CLIENT, this.entity, this.position, rotation, this.world, this.meta);
    }

    public CustomClientCommandSource withWorld(ClientWorld world) {
        return new CustomClientCommandSource(CLIENT.getNetworkHandler(), CLIENT, this.entity, this.position, this.rotation, world, this.meta);
    }

    public CustomClientCommandSource withMeta(String key, Object value) {
        this.meta.put(key, value);
        return this;
    }
}
