package dev.engine_room.flywheel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;

import net.minecraft.client.Minecraft;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod("flywheel_testmod")
public class FlywheelTestModClient {
	public static final String NAME = "Flywheel Test Mod";
	private static final Logger LOGGER = LoggerFactory.getLogger(NAME);

	public FlywheelTestModClient() {
		LOGGER.info("Starting {} on Dist: {}", NAME, FMLLoader.getDist());

		NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e) -> {
			LOGGER.info("Running mixin audit");
			MixinEnvironment.getCurrentEnvironment()
					.audit();

			LOGGER.info("Stopping client");
			Minecraft.getInstance()
					.stop();
		});
	}
}
