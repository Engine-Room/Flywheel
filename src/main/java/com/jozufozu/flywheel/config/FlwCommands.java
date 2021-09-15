package com.jozufozu.flywheel.config;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

public class FlwCommands {
	@SubscribeEvent
	public static void onServerStarting(FMLServerStartingEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getServer()
				.getCommands()
				.getDispatcher();

		dispatcher.register(Commands.literal("flywheel")
									.then(new BooleanConfigCommand("backend", BooleanConfig.ENGINE).register())
									.then(new BooleanConfigCommand("debugNormals", BooleanConfig.NORMAL_OVERLAY).register())
									.then(new BooleanConfigCommand("chunkCaching", BooleanConfig.CHUNK_CACHING).register())
		);
	}
}
