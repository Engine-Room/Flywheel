package com.jozufozu.flywheel.config;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.config.Option.EnumOption;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class FlwCommands {
	public static void registerClientCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext context) {
		FlwConfig config = FlwConfig.get();

		ConfigCommandBuilder commandBuilder = new ConfigCommandBuilder("flywheel");

		commandBuilder.addOption(config.backend, (builder, option) -> enumOptionCommand(builder, config, option,
				BackendType::getShortName,
				(source, value) -> {
					source.sendFeedback(getEngineMessage(value));
				},
				(source, value) -> {
					source.sendFeedback(getEngineMessage(value));
					Backend.reloadWorldRenderers();
				}
			));

		commandBuilder.addOption(config.debugNormals, (builder, option) -> booleanOptionCommand(builder, config, option,
				(source, value) -> {
					Component text = Component.literal("Normal debug mode is currently: ").append(boolToText(value));
					source.sendFeedback(text);
				},
				(source, value) -> {
					Component text = boolToText(value).append(Component.literal(" normal debug mode").withStyle(ChatFormatting.WHITE));
					source.sendFeedback(text);
				}
			));

		commandBuilder.addOption(config.limitUpdates, (builder, option) -> booleanOptionCommand(builder, config, option,
				(source, value) -> {
					Component text = Component.literal("Update limiting is currently: ").append(boolToText(value));
					source.sendFeedback(text);
				},
				(source, value) -> {
					Component text = boolToText(value).append(Component.literal(" update limiting.").withStyle(ChatFormatting.WHITE));
					source.sendFeedback(text);
				}
			));

		commandBuilder.build(dispatcher);
	}

	public static void booleanOptionCommand(LiteralArgumentBuilder<FabricClientCommandSource> builder, FlwConfig config, Option<Boolean> option, BiConsumer<FabricClientCommandSource, Boolean> displayAction, BiConsumer<FabricClientCommandSource, Boolean> setAction) {
		builder
			.executes(context -> {
				displayAction.accept(context.getSource(), option.get());
				return Command.SINGLE_SUCCESS;
			})
			.then(ClientCommandManager.literal("on")
				.executes(context -> {
					option.set(true);
					setAction.accept(context.getSource(), option.get());
					config.save();
					return Command.SINGLE_SUCCESS;
				}))
			.then(ClientCommandManager.literal("off")
				.executes(context -> {
					option.set(false);
					setAction.accept(context.getSource(), option.get());
					config.save();
					return Command.SINGLE_SUCCESS;
				}));
	}

	public static <E extends Enum<E>> void enumOptionCommand(LiteralArgumentBuilder<FabricClientCommandSource> builder, FlwConfig config, EnumOption<E> option, Function<E, String> nameFunc, BiConsumer<FabricClientCommandSource, E> displayAction, BiConsumer<FabricClientCommandSource, E> setAction) {
		builder.executes(context -> {
			displayAction.accept(context.getSource(), option.get());
			return Command.SINGLE_SUCCESS;
		});
		for (E constant : option.getEnumType().getEnumConstants()) {
			builder.then(ClientCommandManager.literal(nameFunc.apply(constant))
				.executes(context -> {
					option.set(constant);
					setAction.accept(context.getSource(), option.get());
					config.save();
					return Command.SINGLE_SUCCESS;
				}));
		}
	}

	public static MutableComponent boolToText(boolean b) {
		return b ? Component.literal("enabled").withStyle(ChatFormatting.DARK_GREEN) : Component.literal("disabled").withStyle(ChatFormatting.RED);
	}

	public static Component getEngineMessage(@NotNull BackendType type) {
		return switch (type) {
			case OFF -> Component.literal("Disabled Flywheel").withStyle(ChatFormatting.RED);
			case INSTANCING -> Component.literal("Using Instancing Engine").withStyle(ChatFormatting.GREEN);
			case BATCHING ->  Component.literal("Using Batching Engine").withStyle(ChatFormatting.GREEN);
		};
	}

	public static class ConfigCommandBuilder {
		protected LiteralArgumentBuilder<FabricClientCommandSource> command;

		public ConfigCommandBuilder(String baseLiteral) {
			command = ClientCommandManager.literal(baseLiteral);
		}

		public <T extends Option<?>> void addOption(T option, BiConsumer<LiteralArgumentBuilder<FabricClientCommandSource>, T> consumer) {
			addOption(option, option.getKey(), consumer);
		}

		public <T extends Option<?>> void addOption(T option, String subcommand, BiConsumer<LiteralArgumentBuilder<FabricClientCommandSource>, T> consumer) {
			LiteralArgumentBuilder<FabricClientCommandSource> builder = ClientCommandManager.literal(subcommand);
			consumer.accept(builder, option);
			command.then(builder);
		}

		public void build(CommandDispatcher<FabricClientCommandSource> dispatcher) {
			dispatcher.register(command);
		}
	}
}
