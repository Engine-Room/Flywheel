package com.jozufozu.flywheel.config;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;

public class FlwCommands {
	public static void registerClientCommands(RegisterClientCommandsEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

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
					FlwEngine.handle(null);

					return Command.SINGLE_SUCCESS;
				})
				.then(Commands.argument("type", EngineArgument.INSTANCE)
						.executes(context -> {
							FlwEngine type = context.getArgument("type", FlwEngine.class);

							FlwEngine.handle(type);

							return Command.SINGLE_SUCCESS;
						}));
	}
}
