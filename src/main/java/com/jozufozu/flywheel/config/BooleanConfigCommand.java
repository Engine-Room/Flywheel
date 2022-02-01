package com.jozufozu.flywheel.config;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

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
					value.receiver.accept(BooleanDirective.DISPLAY);
					return Command.SINGLE_SUCCESS;
				})
				.then(Commands.literal("on")
							  .executes(context -> {
								  value.receiver.accept(BooleanDirective.TRUE);
								  return Command.SINGLE_SUCCESS;
							  }))
				.then(Commands.literal("off")
							  .executes(context -> {
								  value.receiver.accept(BooleanDirective.FALSE);
								  return Command.SINGLE_SUCCESS;
							  }));
	}
}
