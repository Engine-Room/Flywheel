package com.jozufozu.flywheel.config;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FlwCommands {
	@SubscribeEvent
	public static void onServerStarting(ServerStartingEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getServer()
				.getCommands()
				.getDispatcher();

		dispatcher.register(Commands.literal("flywheel")
									.then(new BooleanConfigCommand("debugNormals", BooleanConfig.NORMAL_OVERLAY).register())
									.then(EngineConfigCommand.register())
		);
	}
}
