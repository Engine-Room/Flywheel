package com.jozufozu.flywheel.config;

import java.util.function.BiConsumer;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.api.backend.BackendType;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.lib.backend.BackendTypes;
import com.jozufozu.flywheel.lib.backend.SimpleBackendType;
import com.jozufozu.flywheel.lib.uniform.FlwShaderUniforms;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class FlwCommands {
	public static void registerClientCommands(RegisterClientCommandsEvent event) {
		FlwConfig config = FlwConfig.get();

		ConfigCommandBuilder commandBuilder = new ConfigCommandBuilder("flywheel");

		commandBuilder.addValue(config.client.backend, "backend", (builder, value) ->
			builder
				.executes(context -> {
					LocalPlayer player = Minecraft.getInstance().player;
					if (player != null) {
						player.displayClientMessage(BackendTypes.getBackendType(value.get())
								.getEngineMessage(), false);
					}
					return Command.SINGLE_SUCCESS;
				})
				.then(Commands.argument("type", BackendTypeArgument.INSTANCE)
					.executes(context -> {
						LocalPlayer player = Minecraft.getInstance().player;
						if (player != null) {
							BackendType type = context.getArgument("type", SimpleBackendType.class);
							value.set(type.getShortName());

							Component message = getEngineMessage(type);
							player.displayClientMessage(message, false);

							Backend.reloadWorldRenderers();
						}
						return Command.SINGLE_SUCCESS;
					})));

		commandBuilder.addValue(config.client.limitUpdates, "limitUpdates", (builder, value) -> booleanValueCommand(builder, value,
				(source, bool) -> {
					LocalPlayer player = Minecraft.getInstance().player;
					if (player == null) return;

					Component text = new TextComponent("Update limiting is currently: ").append(boolToText(bool));
					player.displayClientMessage(text, false);
				},
				(source, bool) -> {
					LocalPlayer player = Minecraft.getInstance().player;
					if (player == null) return;

					Component text = boolToText(bool).append(new TextComponent(" update limiting.").withStyle(ChatFormatting.WHITE));
					player.displayClientMessage(text, false);

					Backend.reloadWorldRenderers();
				}
			));

		// TODO
		commandBuilder.command.then(Commands.literal("debugNormals"))
				.executes(context -> {
					LocalPlayer player = Minecraft.getInstance().player;
					if (player == null) return 0;

					player.displayClientMessage(new TextComponent("This command is not yet implemented."), false);

					return Command.SINGLE_SUCCESS;
				});

		commandBuilder.command.then(Commands.literal("debugCrumbling")
				.then(Commands.argument("pos", BlockPosArgument.blockPos())
						.then(Commands.argument("stage", IntegerArgumentType.integer(0, 9))
								.executes(context -> {
									BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
									int value = IntegerArgumentType.getInteger(context, "stage");

									Entity executor = context.getSource()
											.getEntity();

									if (executor == null) {
										return 0;
									}

									executor.level.destroyBlockProgress(executor.getId(), pos, value);

									return Command.SINGLE_SUCCESS;
								}))));

		commandBuilder.command.then(Commands.literal("debugFrustum")
				.then(Commands.literal("pause")
						.executes(context -> {
							FlwShaderUniforms.FRUSTUM_PAUSED = true;
							return 1;
						}))
				.then(Commands.literal("unpause")
						.executes(context -> {
							FlwShaderUniforms.FRUSTUM_PAUSED = false;
							return 1;
						}))
				.then(Commands.literal("capture")
						.executes(context -> {
							FlwShaderUniforms.FRUSTUM_CAPTURE = true;
							return 1;
						})));

		commandBuilder.build(event.getDispatcher());
	}

	public static void booleanValueCommand(LiteralArgumentBuilder<CommandSourceStack> builder, ConfigValue<Boolean> value, BiConsumer<CommandSourceStack, Boolean> displayAction, BiConsumer<CommandSourceStack, Boolean> setAction) {
		builder
			.executes(context -> {
				displayAction.accept(context.getSource(), value.get());
				return Command.SINGLE_SUCCESS;
			})
			.then(Commands.literal("on")
				.executes(context -> {
					value.set(true);
					setAction.accept(context.getSource(), value.get());
					return Command.SINGLE_SUCCESS;
				}))
			.then(Commands.literal("off")
				.executes(context -> {
					value.set(false);
					setAction.accept(context.getSource(), value.get());
					return Command.SINGLE_SUCCESS;
				}));
	}

	public static MutableComponent boolToText(boolean b) {
		return b ? new TextComponent("enabled").withStyle(ChatFormatting.DARK_GREEN) : new TextComponent("disabled").withStyle(ChatFormatting.RED);
	}

	public static Component getEngineMessage(@NotNull BackendType type) {
		return type.getEngineMessage();
	}

	public static class ConfigCommandBuilder {
		protected LiteralArgumentBuilder<CommandSourceStack> command;

		public ConfigCommandBuilder(String baseLiteral) {
			command = Commands.literal(baseLiteral);
		}

		public <T extends ConfigValue<?>> void addValue(T value, String subcommand, BiConsumer<LiteralArgumentBuilder<CommandSourceStack>, T> consumer) {
			LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(subcommand);
			consumer.accept(builder, value);
			command.then(builder);
		}

		public void build(CommandDispatcher<CommandSourceStack> dispatcher) {
			dispatcher.register(command);
		}
	}
}
