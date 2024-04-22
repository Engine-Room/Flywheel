package com.jozufozu.flywheel.impl;

import java.util.function.BiConsumer;

import com.jozufozu.flywheel.api.backend.Backend;
import com.jozufozu.flywheel.api.backend.BackendManager;
import com.jozufozu.flywheel.backend.engine.uniform.DebugMode;
import com.jozufozu.flywheel.backend.engine.uniform.FrameUniforms;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.server.command.EnumArgument;

public final class FlwCommands {
	private FlwCommands() {
	}

	public static void registerClientCommands(RegisterClientCommandsEvent event) {
		var config = ForgeFlwConfig.INSTANCE;

		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("flywheel");

		ConfigValue<String> backendValue = config.client.backend;
		command.then(Commands.literal("backend")
				.executes(context -> {
					Backend backend = BackendManager.getBackend();
					String idStr = Backend.REGISTRY.getIdOrThrow(backend)
							.toString();
					sendMessage(context.getSource(), Component.translatable("command.flywheel.backend.get", idStr));
					return Command.SINGLE_SUCCESS;
				})
				.then(Commands.argument("id", BackendArgument.INSTANCE)
					.executes(context -> {
						Backend requestedBackend = context.getArgument("id", Backend.class);
						String requestedIdStr = Backend.REGISTRY.getIdOrThrow(requestedBackend)
								.toString();
						backendValue.set(requestedIdStr);

						// Reload renderers so we can report the actual backend.
						Minecraft.getInstance().levelRenderer.allChanged();

						Backend actualBackend = BackendManager.getBackend();
						if (actualBackend != requestedBackend) {
							sendFailure(context.getSource(), Component.translatable("command.flywheel.backend.set.unavailable", requestedIdStr));
						}

						String actualIdStr = Backend.REGISTRY.getIdOrThrow(actualBackend)
								.toString();
						sendMessage(context.getSource(), Component.translatable("command.flywheel.backend.set", actualIdStr));
						return Command.SINGLE_SUCCESS;
					})));

		command.then(booleanValueCommand(Commands.literal("limitUpdates"), config.client.limitUpdates,
				(source, bool) -> {
					if (bool) {
						sendMessage(source, Component.translatable("command.flywheel.limit_updates.get.on"));
					} else {
						sendMessage(source, Component.translatable("command.flywheel.limit_updates.get.off"));
					}
				},
				(source, bool) -> {
					if (bool) {
						sendMessage(source, Component.translatable("command.flywheel.limit_updates.set.on"));
					} else {
						sendMessage(source, Component.translatable("command.flywheel.limit_updates.set.off"));
					}

					Minecraft.getInstance().levelRenderer.allChanged();
				}
			));

		command.then(Commands.literal("crumbling")
				.then(Commands.argument("pos", BlockPosArgument.blockPos())
						.then(Commands.argument("stage", IntegerArgumentType.integer(0, 9))
								.executes(context -> {
									Entity executor = context.getSource()
											.getEntity();

									if (executor == null) {
										return 0;
									}

									BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
									int value = IntegerArgumentType.getInteger(context, "stage");

									executor.level()
											.destroyBlockProgress(executor.getId(), pos, value);

									return Command.SINGLE_SUCCESS;
								}))));

		command.then(Commands.literal("debug")
				.then(Commands.argument("mode", EnumArgument.enumArgument(DebugMode.class))
						.executes(context -> {
							DebugMode mode = context.getArgument("mode", DebugMode.class);
							FrameUniforms.debugMode(mode);
							return Command.SINGLE_SUCCESS;
						})));

		command.then(Commands.literal("frustum")
				.then(Commands.literal("capture")
						.executes(context -> {
							FrameUniforms.captureFrustum();
							return Command.SINGLE_SUCCESS;
						}))
				.then(Commands.literal("unpause")
						.executes(context -> {
							FrameUniforms.unpauseFrustum();
							return Command.SINGLE_SUCCESS;
						})));

		event.getDispatcher().register(command);
	}

	private static LiteralArgumentBuilder<CommandSourceStack> booleanValueCommand(LiteralArgumentBuilder<CommandSourceStack> builder, ConfigValue<Boolean> value, BiConsumer<CommandSourceStack, Boolean> displayAction, BiConsumer<CommandSourceStack, Boolean> setAction) {
		return builder
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

	private static void sendMessage(CommandSourceStack source, Component message) {
		source.sendSuccess(() -> message, true);
	}

	private static void sendFailure(CommandSourceStack source, Component message) {
		source.sendFailure(message);
	}
}
