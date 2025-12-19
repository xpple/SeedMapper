package dev.xpple.seedmapper.command;

import com.github.cubiomes.Cubiomes;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import dev.xpple.seedmapper.command.arguments.DimensionArgument;
import dev.xpple.seedmapper.command.arguments.SeedResolutionArgument;
import dev.xpple.seedmapper.command.arguments.VersionArgument;
import dev.xpple.seedmapper.config.Configs;
import dev.xpple.seedmapper.util.SeedDatabaseHelper;
import dev.xpple.seedmapper.util.SeedIdentifier;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.Optionull;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static dev.xpple.seedmapper.util.ChatBuilder.*;

public class CustomClientCommandSource extends ClientSuggestionProvider implements FabricClientCommandSource {

    private final Minecraft client;
    private final Entity entity;
    private final Vec3 position;
    private final Vec2 rotation;
    private final ClientLevel world;
    private final Map<String, Object> meta;

    public CustomClientCommandSource(ClientPacketListener listener, Minecraft minecraft, Entity entity, Vec3 position, Vec2 rotation, ClientLevel world, PermissionSet permissionSet, Map<String, Object> meta) {
        super(listener, minecraft, permissionSet);

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
        return new CustomClientCommandSource(source.getClient().getConnection(), source.getClient(), source.getEntity(), source.getPosition(), source.getRotation(), source.getWorld(), source.permissions(), new HashMap<>());
    }

    @Override
    public void sendFeedback(Component message) {
        this.client.gui.getChat().addMessage(message);
        this.client.getNarrator().saySystemChatQueued(message);
    }

    @Override
    public void sendError(Component message) {
        this.sendFeedback(error(Component.empty().append(message)));
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
    public @Nullable Object getMeta(String key) {
        return this.meta.get(key);
    }

    public CustomClientCommandSource withEntity(Entity entity) {
        return new CustomClientCommandSource(this.client.getConnection(), this.client, entity, this.position, this.rotation, this.world, this.permissions(), this.meta);
    }

    public CustomClientCommandSource withPosition(Vec3 position) {
        return new CustomClientCommandSource(this.client.getConnection(), this.client, this.entity, position, this.rotation, this.world, this.permissions(), this.meta);
    }

    public CustomClientCommandSource withRotation(Vec2 rotation) {
        return new CustomClientCommandSource(this.client.getConnection(), this.client, this.entity, this.position, rotation, this.world, this.permissions(), this.meta);
    }

    public CustomClientCommandSource withWorld(ClientLevel world) {
        return new CustomClientCommandSource(this.client.getConnection(), this.client, this.entity, this.position, this.rotation, world, this.permissions(), this.meta);
    }

    public CustomClientCommandSource withMeta(String key, Object value) {
        this.meta.put(key, value);
        return this;
    }

    public Pair<SeedResolutionArgument.SeedResolution.Method, SeedIdentifier> getSeed() throws CommandSyntaxException {
        SeedIdentifier seed;
        for (SeedResolutionArgument.SeedResolution.Method method : Configs.SeedResolutionOrder) {
            seed = switch (method) {
                case COMMAND_SOURCE -> Optionull.map(this.getMeta("seed"), s -> new SeedIdentifier((long) s));
                case SEED_CONFIG -> Configs.Seed;
                case SAVED_SEEDS_CONFIG -> {
                    String key = this.client.getConnection().getConnection().getRemoteAddress().toString();
                    yield Configs.SavedSeeds.get(key);
                }
                case ONLINE_DATABASE -> {
                    String key = this.client.getConnection().getConnection().getRemoteAddress().toString();
                    yield SeedDatabaseHelper.getSeed(key, this.getWorld().getBiomeManager().biomeZoomSeed);
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
        try {
            return DimensionArgument.dimension().parse(new StringReader(this.getWorld().dimension().identifier().getPath()));
        } catch (CommandSyntaxException _) {
        }
        return switch (this.getWorld().dimensionType().skybox()) {
            case NONE -> Cubiomes.DIM_NETHER();
            case OVERWORLD -> Cubiomes.DIM_OVERWORLD();
            case END -> Cubiomes.DIM_END();
        };
    }

    public int getVersion() throws CommandSyntaxException {
        Object versionMeta = this.getMeta("version");
        if (versionMeta != null) {
            return (int) versionMeta;
        }
        if (this.getSeed().getSecond().hasVersion()) {
            return this.getSeed().getSecond().version();
        }
        return VersionArgument.version().parse(new StringReader(SharedConstants.getCurrentVersion().name()));
    }

    public int getGeneratorFlags() throws CommandSyntaxException {
        Object generatorFlagsMeta = this.getMeta("generatorFlags");
        if (generatorFlagsMeta != null) {
            return (int) generatorFlagsMeta;
        }
        return this.getSeed().getSecond().generatorFlags();
    }
}
