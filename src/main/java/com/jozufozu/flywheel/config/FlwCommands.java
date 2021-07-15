package com.jozufozu.flywheel.config;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

public class FlwCommands {
	@SubscribeEvent
	public static void onServerStarting(FMLServerStartingEvent event) {
		CommandDispatcher<CommandSource> dispatcher = event.getServer()
				.getCommands()
				.getDispatcher();

		dispatcher.register(Commands.literal("flywheel")
									.then(new BooleanConfigCommand("backend", BooleanConfig.ENGINE).register())
									.then(new BooleanConfigCommand("normalOverlay", BooleanConfig.NORMAL_OVERLAY).register()));
	}
}
