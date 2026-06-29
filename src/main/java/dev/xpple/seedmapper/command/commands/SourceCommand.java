package dev.xpple.seedmapper.command.commands;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.datafixers.util.Pair;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.Map;

import static com.mojang.brigadier.arguments.LongArgumentType.*;
import static dev.xpple.clientarguments.arguments.CEntityArgument.*;
import static dev.xpple.clientarguments.arguments.CRotationArgument.*;
import static dev.xpple.clientarguments.arguments.CVec3Argument.*;
import static dev.xpple.seedmapper.command.arguments.DimensionArgument.*;
import static dev.xpple.seedmapper.command.arguments.GeneratorFlagArgument.*;
import static dev.xpple.seedmapper.command.arguments.StructureSaltArgument.*;
import static dev.xpple.seedmapper.command.arguments.VersionArgument.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.*;

public class SourceCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralArgumentBuilder<FabricClientCommandSource> rootLiteral = literal("sm:source");
        LiteralCommandNode<FabricClientCommandSource> root = dispatcher.register(rootLiteral);
        dispatcher.register(rootLiteral
            .then(literal("run")
                .redirect(dispatcher.getRoot(), CommandContext::getSource))
            .then(literal("as")
                .then(argument("entity", entity())
                    .redirect(root, ctx -> CustomClientCommandSource.of(ctx.getSource()).withEntity(getEntity(ctx, "entity")))))
            .then(literal("positioned")
                .then(argument("pos", vec3())
                    .redirect(root, ctx -> CustomClientCommandSource.of(ctx.getSource()).withPosition(getVec3(ctx, "pos")))))
            .then(literal("rotated")
                .then(argument("rot", rotation())
                    .redirect(root, ctx -> CustomClientCommandSource.of(ctx.getSource()).withRotation(getRotation(ctx, "rot").getRotation(ctx.getSource())))))
            .then(literal("in")
                .then(argument("dimension", dimension())
                    .redirect(root, ctx -> CustomClientCommandSource.of(ctx.getSource()).withMeta("dimension", getDimension(ctx, "dimension")))))
            .then(literal("versioned")
                .then(argument("version", version())
                    .redirect(root, ctx -> CustomClientCommandSource.of(ctx.getSource()).withMeta("version", getVersion(ctx, "version")))))
            .then(literal("seeded")
                .then(argument("seed", longArg())
                    .redirect(root, ctx -> CustomClientCommandSource.of(ctx.getSource()).withMeta("seed", getLong(ctx, "seed")))))
            .then(literal("flagged")
                .then(argument("generatorFlag", generatorFlag())
                    .redirect(root, ctx -> {
                        CustomClientCommandSource source = CustomClientCommandSource.of(ctx.getSource());
                        Object existingGeneratorFlag = source.getMeta("generatorFlags");
                        int generatorFlag = getGeneratorFlag(ctx, "generatorFlag");
                        if (existingGeneratorFlag == null) {
                            return source.withMeta("generatorFlags", generatorFlag);
                        }
                        return source.withMeta("generatorFlags", ((int) existingGeneratorFlag) | generatorFlag);
                    })))
            .then(literal("salted")
                .then(argument("structureSalt", structureSalt())
                    .redirect(root, ctx -> {
                        CustomClientCommandSource source = CustomClientCommandSource.of(ctx.getSource());
                        Object existingCustomStructureSalts = source.getMeta("customStructureSalts");
                        Pair<Integer, Integer> structureSalt = getStructureSalt(ctx, "structureSalt");
                        if (existingCustomStructureSalts == null) {
                            return source.withMeta("customStructureSalts", Map.of(structureSalt.getFirst(), structureSalt.getSecond()));
                        }
                        //noinspection unchecked
                        Map<Integer, Integer> customStructureSalts = ImmutableMap.<Integer, Integer>builder()
                            .putAll((Map<Integer, Integer>) existingCustomStructureSalts)
                            .put(structureSalt.getFirst(), structureSalt.getSecond())
                            .buildKeepingLast();
                        return source.withMeta("customStructureSalts", customStructureSalts);
                    }))));
    }
}
