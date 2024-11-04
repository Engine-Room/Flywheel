package dev.engine_room.flywheel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

public class FlywheelTestModClient implements ClientModInitializer {
	public static final String NAME = "Flywheel Test Mod";
	private static final Logger LOGGER = LoggerFactory.getLogger(NAME);

	@Override
	public void onInitializeClient() {
		LOGGER.info("Starting {} on EnvType: {}", NAME, FabricLoader.getInstance()
				.getEnvironmentType());

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			LOGGER.info("Running mixin audit");
			MixinEnvironment.getCurrentEnvironment()
					.audit();

			LOGGER.info("Stopping client");
			client.stop();
		});
	}
}
