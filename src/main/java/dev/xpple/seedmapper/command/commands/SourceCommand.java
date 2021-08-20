package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.mixin.command.DefaultPosArgumentAccessor;
import dev.xpple.seedmapper.mixin.command.LookingPosArgumentAccessor;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.command.argument.LookingPosArgument;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.*;
import static net.minecraft.command.argument.DimensionArgumentType.dimension;
import static net.minecraft.command.argument.EntityArgumentType.entity;
import static net.minecraft.command.argument.RotationArgumentType.rotation;
import static net.minecraft.command.argument.Vec3ArgumentType.vec3;

public class SourceCommand extends ClientCommand {
    @Override
    protected void build() {
        LiteralCommandNode<FabricClientCommandSource> root = DISPATCHER.register(literal("source"));
        argumentBuilder
                .then(literal("run")
                        .redirect(DISPATCHER.getRoot(), CommandContext::getSource))
                .then(literal("as")
                        .then(argument("entity", entity())
                                .redirect(root, ctx -> CustomClientCommandSource.of(ctx.getSource()).withEntity(ctx.getArgument("entity", Entity.class)))))
                .then(literal("positioned")
                        .then(argument("pos", vec3())
                                .redirect(root, ctx -> {
                                    PosArgument posArgument = ctx.getArgument("pos", PosArgument.class);
                                    if (posArgument instanceof DefaultPosArgument defaultArg) {
                                        DefaultPosArgumentAccessor argAccessor = (DefaultPosArgumentAccessor) defaultArg;
                                        Vec3d source = ctx.getSource().getPosition();
                                        Vec3d modified = new Vec3d(argAccessor.getX().toAbsoluteCoordinate(source.x), argAccessor.getY().toAbsoluteCoordinate(source.y), argAccessor.getZ().toAbsoluteCoordinate(source.z));
                                        return CustomClientCommandSource.of(ctx.getSource()).withPosition(modified);
                                    } else if (posArgument instanceof LookingPosArgument lookingArg) {
                                        LookingPosArgumentAccessor argAccessor = (LookingPosArgumentAccessor) lookingArg;
                                        Vec2f vec2f = ctx.getSource().getRotation();
                                        Vec3d vec3d = ctx.getSource().getPosition();
                                        float f = MathHelper.cos((vec2f.y + 90.0F) * 0.017453292F);
                                        float g = MathHelper.sin((vec2f.y + 90.0F) * 0.017453292F);
                                        float h = MathHelper.cos(-vec2f.x * 0.017453292F);
                                        float i = MathHelper.sin(-vec2f.x * 0.017453292F);
                                        float j = MathHelper.cos((-vec2f.x + 90.0F) * 0.017453292F);
                                        float k = MathHelper.sin((-vec2f.x + 90.0F) * 0.017453292F);
                                        Vec3d vec3d2 = new Vec3d(f * h, i, g * h);
                                        Vec3d vec3d3 = new Vec3d(f * j, k, g * j);
                                        Vec3d vec3d4 = vec3d2.crossProduct(vec3d3).multiply(-1.0D);
                                        double d = vec3d2.x * argAccessor.getZ() + vec3d3.x * argAccessor.getY() + vec3d4.x * argAccessor.getX();
                                        double e = vec3d2.y * argAccessor.getZ() + vec3d3.y * argAccessor.getY() + vec3d4.y * argAccessor.getX();
                                        double l = vec3d2.z * argAccessor.getZ() + vec3d3.z * argAccessor.getY() + vec3d4.z * argAccessor.getX();
                                        Vec3d modified = new Vec3d(vec3d.x + d, vec3d.y + e, vec3d.z + l);
                                        return CustomClientCommandSource.of(ctx.getSource()).withPosition(modified);
                                    }
                                    return ctx.getSource();
                                })))
                .then(literal("rotated")
                        .then(argument("rot", rotation())
                                .redirect(root, ctx -> {
                                    PosArgument posArgument = ctx.getArgument("pos", PosArgument.class);
                                    if (posArgument instanceof DefaultPosArgument defaultArg) {
                                        DefaultPosArgumentAccessor argAccessor = (DefaultPosArgumentAccessor) defaultArg;
                                        Vec2f source = ctx.getSource().getRotation();
                                        Vec2f modified = new Vec2f((float) argAccessor.getY().toAbsoluteCoordinate(source.x), (float) argAccessor.getY().toAbsoluteCoordinate(source.y));
                                        return CustomClientCommandSource.of(ctx.getSource()).withRotation(modified);
                                    } else if (posArgument instanceof LookingPosArgument) {
                                        return CustomClientCommandSource.of(ctx.getSource()).withRotation(Vec2f.ZERO);
                                    }
                                    return ctx.getSource();
                                })))
                .then(literal("in")
                        .then(argument("dimension", dimension())
                                .redirect(root, ctx -> {
                                    Identifier dimensionArg = ctx.getArgument("dimension", Identifier.class);
                                    RegistryKey<World> registryKey = RegistryKey.of(Registry.WORLD_KEY, dimensionArg);
                                    return CustomClientCommandSource.of(ctx.getSource()).withMeta("dimension", registryKey.getValue());
                                })));
    }

    @Override
    protected String rootLiteral() {
        return "source";
    }
}
