package com.jozufozu.flywheel.config;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class BooleanConfigCommand {

	private final String name;

	private final BooleanConfig value;

	public BooleanConfigCommand(String name, BooleanConfig value) {
		this.name = name;
		this.value = value;
	}

	public ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal(name)
				.executes(context -> {
					ServerPlayer player = context.getSource()
							.getPlayerOrException();
					FlwPackets.channel.send(PacketDistributor.PLAYER.with(() -> player), new SConfigureBooleanPacket(value, BooleanDirective.DISPLAY));
					return Command.SINGLE_SUCCESS;
				})
				.then(Commands.literal("on")
							  .executes(context -> {
								  ServerPlayer player = context.getSource()
										  .getPlayerOrException();
								  FlwPackets.channel.send(PacketDistributor.PLAYER.with(() -> player), new SConfigureBooleanPacket(value, BooleanDirective.TRUE));
								  return Command.SINGLE_SUCCESS;
							  }))
				.then(Commands.literal("off")
							  .executes(context -> {
								  ServerPlayer player = context.getSource()
										  .getPlayerOrException();
								  FlwPackets.channel.send(PacketDistributor.PLAYER.with(() -> player), new SConfigureBooleanPacket(value, BooleanDirective.FALSE));
								  return Command.SINGLE_SUCCESS;
							  }));
	}
}
