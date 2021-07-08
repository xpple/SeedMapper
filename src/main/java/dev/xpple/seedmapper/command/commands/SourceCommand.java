package dev.xpple.seedmapper.command.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.*;
import static net.minecraft.command.argument.EntityArgumentType.entity;
import static net.minecraft.command.argument.RotationArgumentType.rotation;
import static net.minecraft.command.argument.Vec3ArgumentType.vec3;

public class SourceCommand extends ClientCommand {
    @Override
    protected void register() {
        LiteralCommandNode<FabricClientCommandSource> root = DISPATCHER.register(literal("source"));
        argumentBuilder
                .then(literal("run")
                        .redirect(DISPATCHER.getRoot(), CommandContext::getSource))
                .then(literal("as")
                        .then(argument("entity", entity())
                                .redirect(root, ctx -> ((CustomClientCommandSource) ctx.getSource()).withEntity(ctx.getArgument("entity", Entity.class)))))
                .then(literal("positioned")
                        .then(argument("pos", vec3())
                                .redirect(root, ctx -> ((CustomClientCommandSource) ctx.getSource()).withPosition(Vec3d.ZERO))))
                .then(literal("rotated")
                        .then(argument("rot", rotation())
                                .redirect(root, ctx -> ((CustomClientCommandSource) ctx.getSource()).withRotation(ctx.getArgument("rot", PosArgument.class).toAbsoluteRotation(null)))));
    }

    @Override
    protected String rootLiteral() {
        return "source";
    }
}
