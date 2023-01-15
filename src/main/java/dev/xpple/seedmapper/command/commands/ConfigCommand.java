package dev.xpple.seedmapper.command.commands;

import com.google.common.base.Joiner;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.util.chat.Chat;
import dev.xpple.seedmapper.util.config.Configs;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.text.Text;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg;
import static com.mojang.brigadier.arguments.DoubleArgumentType.getDouble;
import static com.mojang.brigadier.arguments.FloatArgumentType.floatArg;
import static com.mojang.brigadier.arguments.FloatArgumentType.getFloat;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.LongArgumentType.getLong;
import static com.mojang.brigadier.arguments.LongArgumentType.longArg;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static dev.xpple.seedmapper.command.arguments.BlockArgumentType.block;
import static dev.xpple.seedmapper.command.arguments.BlockArgumentType.getBlock;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ConfigCommand extends ClientCommand {

    @Override
    protected void build(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        Map<String, LiteralArgumentBuilder<FabricClientCommandSource>> literals = new HashMap<>();
        for (String config : Configs.getConfigs()) {
            LiteralArgumentBuilder<FabricClientCommandSource> literal = literal(config);
            literals.put(config, literal);

            literal.then(literal("get").executes(ctx -> get(CustomClientCommandSource.of(ctx.getSource()), config)));
        }
        Configs.getSetters().forEach(config -> {
            RequiredArgumentBuilder<FabricClientCommandSource, ?> subCommand;
            Class<?> type = Configs.getType(config);
            if (type == boolean.class || type == Boolean.class) {
                subCommand = argument("value", bool()).executes(ctx -> set(CustomClientCommandSource.of(ctx.getSource()), config, getBool(ctx, "value")));
            } else if (type == double.class || type == Double.class) {
                subCommand = argument("value", doubleArg()).executes(ctx -> set(CustomClientCommandSource.of(ctx.getSource()), config, getDouble(ctx, "value")));
            } else if (type == float.class || type == Float.class) {
                subCommand = argument("value", floatArg()).executes(ctx -> set(CustomClientCommandSource.of(ctx.getSource()), config, getFloat(ctx, "value")));
            } else if (type == int.class || type == Integer.class) {
                subCommand = argument("value", integer()).executes(ctx -> set(CustomClientCommandSource.of(ctx.getSource()), config, getInteger(ctx, "value")));
            } else if (type == long.class || type == Long.class) {
                subCommand = argument("value", longArg()).executes(ctx -> set(CustomClientCommandSource.of(ctx.getSource()), config, getLong(ctx, "value")));
            } else if (type == String.class) {
                subCommand = argument("value", greedyString()).executes(ctx -> set(CustomClientCommandSource.of(ctx.getSource()), config, getString(ctx, "value")));
            } else if (type == Block.class) {
                subCommand = argument("value", block()).executes(ctx -> set(CustomClientCommandSource.of(ctx.getSource()), config, getBlock(ctx, "value")));
            } else {
                return;
            }
            literals.get(config).then(literal("set").then(subCommand));
        });
        Configs.getAdders().forEach(config -> {
            RequiredArgumentBuilder<FabricClientCommandSource, ?> subCommand;
            Type[] types = Configs.getParameterTypes(config);
            Type type;
            if (types.length == 1) {
                type = types[0];
            } else if (types.length == 2) {
                type = types[1];
            } else {
                return;
            }
            if (type == boolean.class || type == Boolean.class) {
                subCommand = argument("value", bool()).executes(ctx -> add(CustomClientCommandSource.of(ctx.getSource()), config, getBool(ctx, "value")));
            } else if (type == double.class || type == Double.class) {
                subCommand = argument("value", doubleArg()).executes(ctx -> add(CustomClientCommandSource.of(ctx.getSource()), config, getDouble(ctx, "value")));
            } else if (type == float.class || type == Float.class) {
                subCommand = argument("value", floatArg()).executes(ctx -> add(CustomClientCommandSource.of(ctx.getSource()), config, getFloat(ctx, "value")));
            } else if (type == int.class || type == Integer.class) {
                subCommand = argument("value", integer()).executes(ctx -> add(CustomClientCommandSource.of(ctx.getSource()), config, getInteger(ctx, "value")));
            } else if (type == long.class || type == Long.class) {
                subCommand = argument("value", longArg()).executes(ctx -> add(CustomClientCommandSource.of(ctx.getSource()), config, getLong(ctx, "value")));
            } else if (type == String.class) {
                subCommand = argument("value", greedyString()).executes(ctx -> add(CustomClientCommandSource.of(ctx.getSource()), config, getString(ctx, "value")));
            } else if (type == Block.class) {
                subCommand = argument("value", block()).executes(ctx -> add(CustomClientCommandSource.of(ctx.getSource()), config, getBlock(ctx, "value")));
            } else {
                return;
            }
            literals.get(config).then(literal("add").then(subCommand));
        });
        Configs.getPutters().forEach(config -> {
            RequiredArgumentBuilder<FabricClientCommandSource, ?> subCommand;
            Type[] types = Configs.getParameterTypes(config);
            if (types.length != 2) {
                return;
            }
            Type keyType = types[0];
            Function<CommandContext<FabricClientCommandSource>, Object> getKey;
            if (keyType == boolean.class || keyType == Boolean.class) {
                subCommand = argument("key", bool());
                getKey = ctx -> getBool(ctx, "key");
            } else if (keyType == double.class || keyType == Double.class) {
                subCommand = argument("key", doubleArg());
                getKey = ctx -> getDouble(ctx, "key");
            } else if (keyType == float.class || keyType == Float.class) {
                subCommand = argument("key", floatArg());
                getKey = ctx -> getFloat(ctx, "key");
            } else if (keyType == int.class || keyType == Integer.class) {
                subCommand = argument("key", integer());
                getKey = ctx -> getInteger(ctx, "key");
            } else if (keyType == long.class || keyType == Long.class) {
                subCommand = argument("key", longArg());
                getKey = ctx -> getLong(ctx, "key");
            } else if (keyType == String.class) {
                subCommand = argument("key", string());
                getKey = ctx -> getString(ctx, "key");
            } else if (keyType == Block.class) {
                subCommand = argument("key", block());
                getKey = ctx -> getBlock(ctx, "key");
            } else {
                return;
            }
            RequiredArgumentBuilder<FabricClientCommandSource, ?> subSubCommand;
            Type valueType = types[1];
            if (valueType == boolean.class || valueType == Boolean.class) {
                subSubCommand = argument("value", bool()).executes(ctx -> put(CustomClientCommandSource.of(ctx.getSource()), config, getKey.apply(ctx), getBool(ctx, "value")));
            } else if (valueType == double.class || valueType == Double.class) {
                subSubCommand = argument("value", doubleArg()).executes(ctx -> put(CustomClientCommandSource.of(ctx.getSource()), config, getKey.apply(ctx), getDouble(ctx, "value")));
            } else if (valueType == float.class || valueType == Float.class) {
                subSubCommand = argument("value", floatArg()).executes(ctx -> put(CustomClientCommandSource.of(ctx.getSource()), config, getKey.apply(ctx), getFloat(ctx, "value")));
            } else if (valueType == int.class || valueType == Integer.class) {
                subSubCommand = argument("value", integer()).executes(ctx -> put(CustomClientCommandSource.of(ctx.getSource()), config, getKey.apply(ctx), getInteger(ctx, "value")));
            } else if (valueType == long.class || valueType == Long.class) {
                subSubCommand = argument("value", longArg()).executes(ctx -> put(CustomClientCommandSource.of(ctx.getSource()), config, getKey.apply(ctx), getLong(ctx, "value")));
            } else if (valueType == String.class) {
                subSubCommand = argument("value", greedyString()).executes(ctx -> put(CustomClientCommandSource.of(ctx.getSource()), config, getKey.apply(ctx), getString(ctx, "value")));
            } else if (valueType == Block.class) {
                subSubCommand = argument("value", block()).executes(ctx -> put(CustomClientCommandSource.of(ctx.getSource()), config, getKey.apply(ctx), getBlock(ctx, "value")));
            } else {
                return;
            }
            literals.get(config).then(literal("put").then(subCommand.then(subSubCommand)));
        });
        Configs.getRemovers().forEach(config -> {
            RequiredArgumentBuilder<FabricClientCommandSource, ?> subCommand;
            Type[] types = Configs.getParameterTypes(config);
            Type type;
            if (types.length == 1) {
                type = types[0];
            } else if (types.length == 2) {
                type = types[1];
            } else {
                return;
            }
            if (type == boolean.class || type == Boolean.class) {
                subCommand = argument("value", bool()).executes(ctx -> remove(CustomClientCommandSource.of(ctx.getSource()), config, getBool(ctx, "value")));
            } else if (type == double.class || type == Double.class) {
                subCommand = argument("value", doubleArg()).executes(ctx -> remove(CustomClientCommandSource.of(ctx.getSource()), config, getDouble(ctx, "value")));
            } else if (type == float.class || type == Float.class) {
                subCommand = argument("value", floatArg()).executes(ctx -> remove(CustomClientCommandSource.of(ctx.getSource()), config, getFloat(ctx, "value")));
            } else if (type == int.class || type == Integer.class) {
                subCommand = argument("value", integer()).executes(ctx -> remove(CustomClientCommandSource.of(ctx.getSource()), config, getInteger(ctx, "value")));
            } else if (type == long.class || type == Long.class) {
                subCommand = argument("value", longArg()).executes(ctx -> remove(CustomClientCommandSource.of(ctx.getSource()), config, getLong(ctx, "value")));
            } else if (type == String.class) {
                subCommand = argument("value", greedyString()).executes(ctx -> remove(CustomClientCommandSource.of(ctx.getSource()), config, getString(ctx, "value")));
            } else if (type == Block.class) {
                subCommand = argument("value", block()).executes(ctx -> remove(CustomClientCommandSource.of(ctx.getSource()), config, getBlock(ctx, "value")));
            } else {
                return;
            }
            literals.get(config).then(literal("remove").then(subCommand));
        });
        literals.forEach((config, literal) -> argumentBuilder.then(literal));
    }

    @Override
    protected String rootLiteral() {
        return "config";
    }

    private static int get(CustomClientCommandSource source, String config) {
        String value = Configs.toString(config);
        Chat.print("", Text.translatable("command.config.get", config, value));
        return Command.SINGLE_SUCCESS;
    }

    private static int set(CustomClientCommandSource source, String config, Object value) {
        Configs.set(config, value);
        Chat.print("", Text.translatable("command.config.set", config, value));
        return Command.SINGLE_SUCCESS;
    }

    private static int add(CustomClientCommandSource source, String config, Object value) {
        Configs.add(config, value);
        Chat.print("", Text.translatable("command.config.add", value, config));
        return Command.SINGLE_SUCCESS;
    }

    private static int put(CustomClientCommandSource source, String config, Object key, Object value) {
        Configs.put(config, key, value);
        Chat.print("", Text.translatable("command.config.put", key, value, config));
        return Command.SINGLE_SUCCESS;
    }

    private static int remove(CustomClientCommandSource source, String config, Object value) {
        Configs.remove(config, value);
        Chat.print("", Text.translatable("command.config.remove", value, config));
        return Command.SINGLE_SUCCESS;
    }
}
