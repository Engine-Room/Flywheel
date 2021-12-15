package com.jozufozu.flywheel.config;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.command.EnumArgument;

public class EngineConfigCommand {
	public ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("engine")
				.executes(context -> {
					ServerPlayer player = context.getSource()
							.getPlayerOrException();
					FlwPackets.channel.send(PacketDistributor.PLAYER.with(() -> player), new SConfigureEnginePacket());
					return Command.SINGLE_SUCCESS;
				})
				.then(Commands.argument("type", EnumArgument.enumArgument(FlwEngine.class))
						.executes(context -> {
							FlwEngine type = context.getArgument("type", FlwEngine.class);

							ServerPlayer player = context.getSource()
									.getPlayerOrException();
							FlwPackets.channel.send(PacketDistributor.PLAYER.with(() -> player), new SConfigureEnginePacket(type));

							return Command.SINGLE_SUCCESS;
						}));
	}
}
