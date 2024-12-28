package dev.xpple.seedmapper.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import dev.xpple.seedmapper.command.arguments.DimensionArgument;
import dev.xpple.seedmapper.command.arguments.SeedResolutionArgument;
import dev.xpple.seedmapper.command.arguments.VersionArgument;
import dev.xpple.seedmapper.config.Configs;
import dev.xpple.seedmapper.util.SeedDatabaseHelper;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class CustomClientCommandSource extends ClientSuggestionProvider implements FabricClientCommandSource {

    private final Minecraft client;
    private final Entity entity;
    private final Vec3 position;
    private final Vec2 rotation;
    private final ClientLevel world;
    private final Map<String, Object> meta;

    public CustomClientCommandSource(ClientPacketListener listener, Minecraft minecraft, Entity entity, Vec3 position, Vec2 rotation, ClientLevel world, Map<String, Object> meta) {
        super(listener, minecraft);

        this.client = minecraft;
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
        return new CustomClientCommandSource(source.getClient().getConnection(), source.getClient(), source.getEntity(), source.getPosition(), source.getRotation(), source.getWorld(), new HashMap<>());
    }

    @Override
    public void sendFeedback(Component message) {
        this.client.gui.getChat().addMessage(message);
        this.client.getNarrator().sayNow(message);
    }

    @Override
    public void sendError(Component message) {
        this.sendFeedback(Component.empty().append(message).withStyle(ChatFormatting.RED));
    }

    @Override
    public Minecraft getClient() {
        return this.client;
    }

    @Override
    public LocalPlayer getPlayer() {
        return this.getClient().player;
    }

    @Override
    public Entity getEntity() {
        return this.entity;
    }

    @Override
    public Vec3 getPosition() {
        return this.position;
    }

    @Override
    public Vec2 getRotation() {
        return this.rotation;
    }

    @Override
    public ClientLevel getWorld() {
        return this.world;
    }

    @Override
    public Object getMeta(String key) {
        return this.meta.get(key);
    }

    public CustomClientCommandSource withEntity(Entity entity) {
        return new CustomClientCommandSource(this.client.getConnection(), this.client, entity, this.position, this.rotation, this.world, this.meta);
    }

    public CustomClientCommandSource withPosition(Vec3 position) {
        return new CustomClientCommandSource(this.client.getConnection(), this.client, this.entity, position, this.rotation, this.world, this.meta);
    }

    public CustomClientCommandSource withRotation(Vec2 rotation) {
        return new CustomClientCommandSource(this.client.getConnection(), this.client, this.entity, this.position, rotation, this.world, this.meta);
    }

    public CustomClientCommandSource withWorld(ClientLevel world) {
        return new CustomClientCommandSource(this.client.getConnection(), this.client, this.entity, this.position, this.rotation, world, this.meta);
    }

    public CustomClientCommandSource withMeta(String key, Object value) {
        this.meta.put(key, value);
        return this;
    }

    public Pair<SeedResolutionArgument.SeedResolution.Method, Long> getSeed() throws CommandSyntaxException {
        Long seed;
        for (SeedResolutionArgument.SeedResolution.Method method : Configs.SeedResolutionOrder) {
            seed = switch (method) {
                case COMMAND_SOURCE -> (Long) this.getMeta("seed");
                case SEED_CONFIG -> Configs.Seed;
                case SAVED_SEEDS_CONFIG -> {
                    String key = this.client.getConnection().getConnection().getRemoteAddress().toString();
                    yield Configs.SavedSeeds.get(key);
                }
                case ONLINE_DATABASE -> {
                    String key = this.client.getConnection().getConnection().getRemoteAddress().toString();
                    yield SeedDatabaseHelper.getSeed(key);
                }
            };
            if (seed != null) {
                return Pair.of(method, seed);
            }
        }
        throw CommandExceptions.NO_SEED_AVAILABLE_EXCEPTION.create();
    }

    public int getDimension() throws CommandSyntaxException {
        Object dimensionMeta = this.getMeta("dimension");
        if (dimensionMeta != null) {
            return (int) dimensionMeta;
        }
        return DimensionArgument.dimension().parse(new StringReader(this.getWorld().dimension().location().getPath()));
    }

    public int getVersion() throws CommandSyntaxException {
        Object versionMeta = this.getMeta("version");
        if (versionMeta != null) {
            return (int) versionMeta;
        }
        return VersionArgument.version().parse(new StringReader(SharedConstants.getCurrentVersion().getName()));
    }
}
