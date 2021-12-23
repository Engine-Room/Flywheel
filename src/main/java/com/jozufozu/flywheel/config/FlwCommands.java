package com.jozufozu.flywheel.config;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

public class FlwCommands {
	@SubscribeEvent
	public static void onServerStarting(ServerStartingEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getServer()
				.getCommands()
				.getDispatcher();

		dispatcher.register(Commands.literal("flywheel")
									.then(debugCommand())
									.then(backendCommand())
		);
	}

	private static ArgumentBuilder<CommandSourceStack, ?> debugCommand() {
		return new BooleanConfigCommand("debugNormals", BooleanConfig.NORMAL_OVERLAY).register();
	}

	private static ArgumentBuilder<CommandSourceStack, ?> backendCommand() {
		return Commands.literal("backend")
				.executes(context -> {
					ServerPlayer player = context.getSource()
							.getPlayerOrException();
					FlwPackets.channel.send(PacketDistributor.PLAYER.with(() -> player), new SConfigureEnginePacket());
					return Command.SINGLE_SUCCESS;
				})
				.then(Commands.argument("type", EngineArgument.getInstance())
						.executes(context -> {
							FlwEngine type = context.getArgument("type", FlwEngine.class);

							ServerPlayer player = context.getSource()
									.getPlayerOrException();
							FlwPackets.channel.send(PacketDistributor.PLAYER.with(() -> player), new SConfigureEnginePacket(type));

							return Command.SINGLE_SUCCESS;
						}));
	}
}
