package dev.engine_room.flywheel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;

public class FlywheelTestModClient implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("Flywheel Test Mod");

	@Override
	public void onInitializeClient() {
		LOGGER.info("Starting Test Mod, on Env: {}", FabricLoader.getInstance().getEnvironmentType());

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			LOGGER.info("Running mixin audit");
			MixinEnvironment.getCurrentEnvironment()
					.audit();

			LOGGER.info("Ran mixin audit, stopping client.");
			client.stop();
		});
	}
}
