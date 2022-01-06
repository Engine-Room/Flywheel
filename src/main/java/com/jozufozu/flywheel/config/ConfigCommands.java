package com.jozufozu.flywheel.config;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.config.Option.EnumOption;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public final class ConfigCommands {
	public static void init(FlwConfig config) {
		ConfigCommandBuilder commandBuilder = new ConfigCommandBuilder("flywheel");

		commandBuilder.addOption(config.engine, "backend", (builder, option) -> enumOptionCommand(builder, config, option,
				FlwEngine::getShortName,
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
					Component text = new TextComponent("Normal debug mode is currently: ").append(boolToText(value));
					source.sendFeedback(text);
				},
				(source, value) -> {
					Component text = boolToText(value).append(new TextComponent(" normal debug mode").withStyle(ChatFormatting.WHITE));
					source.sendFeedback(text);
				}
			));

		commandBuilder.build();
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
		return b ? new TextComponent("enabled").withStyle(ChatFormatting.DARK_GREEN) : new TextComponent("disabled").withStyle(ChatFormatting.RED);
	}

	public static Component getEngineMessage(@NotNull FlwEngine type) {
		return switch (type) {
			case OFF -> new TextComponent("Disabled Flywheel").withStyle(ChatFormatting.RED);
			case INSTANCING -> new TextComponent("Using Instancing Engine").withStyle(ChatFormatting.GREEN);
			case BATCHING -> {
				MutableComponent msg = new TextComponent("Using Batching Engine").withStyle(ChatFormatting.GREEN);

				if (FabricLoader.getInstance()
						.isModLoaded("create")) {
					// FIXME: batching engine contraption lighting issues
					msg.append(new TextComponent("\nWARNING: May cause issues with Create Contraptions").withStyle(ChatFormatting.RED));
				}

				yield msg;
			}
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

		public void build() {
			ClientCommandManager.DISPATCHER.register(command);
		}
	}
}
