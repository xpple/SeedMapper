package dev.xpple.seedmapper.command.commands;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.xpple.seedmapper.command.ClientCommand;
import dev.xpple.seedmapper.command.CustomClientCommandSource;
import dev.xpple.seedmapper.command.arguments.BlockArgumentType;
import dev.xpple.seedmapper.util.chat.Chat;
import dev.xpple.seedmapper.util.config.Configs;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ConfigCommand extends ClientCommand {

    private static final Map<Class<?>, Pair<Supplier<? extends ArgumentType<?>>, BiFunction<CommandContext<FabricClientCommandSource>, String, Object>>> arguments = ImmutableMap.<Class<?>, Pair<Supplier<? extends ArgumentType<?>>, BiFunction<CommandContext<FabricClientCommandSource>, String, Object>>>builder()
            .put(boolean.class, new Pair<>(BoolArgumentType::bool, BoolArgumentType::getBool))
            .put(Boolean.class, new Pair<>(BoolArgumentType::bool, BoolArgumentType::getBool))
            .put(double.class, new Pair<>(DoubleArgumentType::doubleArg, DoubleArgumentType::getDouble))
            .put(Double.class, new Pair<>(DoubleArgumentType::doubleArg, DoubleArgumentType::getDouble))
            .put(float.class, new Pair<>(FloatArgumentType::floatArg, FloatArgumentType::getFloat))
            .put(Float.class, new Pair<>(FloatArgumentType::floatArg, FloatArgumentType::getFloat))
            .put(int.class, new Pair<>(IntegerArgumentType::integer, IntegerArgumentType::getInteger))
            .put(Integer.class, new Pair<>(IntegerArgumentType::integer, IntegerArgumentType::getInteger))
            .put(long.class, new Pair<>(LongArgumentType::longArg, LongArgumentType::getLong))
            .put(Long.class, new Pair<>(LongArgumentType::longArg, LongArgumentType::getLong))
            .put(String.class, new Pair<>(StringArgumentType::string, StringArgumentType::getString))
            .put(Block.class, new Pair<>(BlockArgumentType::block, BlockArgumentType::getBlock))
            .build();

    @Override
    protected void build(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        Map<String, LiteralArgumentBuilder<FabricClientCommandSource>> literals = new HashMap<>();
        for (String config : Configs.getConfigs()) {
            LiteralArgumentBuilder<FabricClientCommandSource> literal = literal(config);
            literals.put(config, literal);

            literal.then(literal("get").executes(ctx -> get(CustomClientCommandSource.of(ctx.getSource()), config)));
        }
        Configs.getSetters().forEach(config -> {
            Class<?> type = Configs.getType(config);
            var pair = arguments.get(type);
            if (pair == null) {
                return;
            }
            RequiredArgumentBuilder<FabricClientCommandSource, ?> subCommand = argument("value", pair.getLeft().get()).executes(ctx -> set(CustomClientCommandSource.of(ctx.getSource()), config, pair.getRight().apply(ctx, "value")));
            literals.get(config).then(literal("set").then(subCommand));
        });
        Configs.getAdders().forEach(config -> {
            Type[] types = Configs.getParameterTypes(config);
            Type type;
            if (types.length == 1) {
                type = types[0];
            } else if (types.length == 2) {
                type = types[1];
            } else {
                return;
            }
            var pair = arguments.get((Class<?>) type);
            if (pair == null) {
                return;
            }
            RequiredArgumentBuilder<FabricClientCommandSource, ?> subCommand = argument("value", pair.getLeft().get()).executes(ctx -> add(CustomClientCommandSource.of(ctx.getSource()), config, pair.getRight().apply(ctx, "value")));
            literals.get(config).then(literal("add").then(subCommand));
        });
        Configs.getPutters().forEach(config -> {
            Type[] types = Configs.getParameterTypes(config);
            if (types.length != 2) {
                return;
            }
            Type keyType = types[0];
            var keyPair = arguments.get((Class<?>) keyType);
            if (keyPair == null) {
                return;
            }
            RequiredArgumentBuilder<FabricClientCommandSource, ?> subCommand = argument("key", keyPair.getLeft().get());
            Function<CommandContext<FabricClientCommandSource>, Object> getKey = ctx -> keyPair.getRight().apply(ctx, "key");
            Type valueType = types[1];
            var valuePair = arguments.get((Class<?>) valueType);
            if (valuePair == null) {
                return;
            }
            RequiredArgumentBuilder<FabricClientCommandSource, ?> subSubCommand = argument("value", valuePair.getLeft().get()).executes(ctx -> put(CustomClientCommandSource.of(ctx.getSource()), config, getKey.apply(ctx), valuePair.getRight().apply(ctx, "value")));
            literals.get(config).then(literal("put").then(subCommand.then(subSubCommand)));
        });
        Configs.getRemovers().forEach(config -> {
            Type[] types = Configs.getParameterTypes(config);
            Type type;
            if (types.length == 1) {
                type = types[0];
            } else if (types.length == 2) {
                type = types[1];
            } else {
                return;
            }
            var pair = arguments.get((Class<?>) type);
            if (pair == null) {
                return;
            }
            RequiredArgumentBuilder<FabricClientCommandSource, ?> subCommand = argument("value", pair.getLeft().get()).executes(ctx -> remove(CustomClientCommandSource.of(ctx.getSource()), config, pair.getRight().apply(ctx, "value")));
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
