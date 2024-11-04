package dev.engine_room.flywheel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod("flywheel_testmod")
public class FlywheelTestModClient {
	public static final String NAME = "Flywheel Test Mod";
	private static final Logger LOGGER = LoggerFactory.getLogger(NAME);

	public FlywheelTestModClient() {
		LOGGER.info("Starting {} on Dist: {}", NAME, FMLLoader.getDist());

		MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent e) -> {
			if (e.phase == TickEvent.Phase.END) {
				LOGGER.info("Running mixin audit");
				MixinEnvironment.getCurrentEnvironment()
						.audit();

				LOGGER.info("Stopping client");
				Minecraft.getInstance()
						.stop();
			}
		});
	}
}
