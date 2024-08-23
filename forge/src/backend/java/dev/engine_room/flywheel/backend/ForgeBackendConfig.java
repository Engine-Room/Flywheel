package dev.engine_room.flywheel.backend;

import org.apache.commons.lang3.tuple.Pair;

import dev.engine_room.flywheel.backend.compile.LightSmoothness;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ForgeBackendConfig implements BackendConfig {
	public static final ForgeBackendConfig INSTANCE = new ForgeBackendConfig();

	public final ClientConfig client;
	private final ForgeConfigSpec clientSpec;

	private ForgeBackendConfig() {
		Pair<ClientConfig, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
		this.client = clientPair.getLeft();
		clientSpec = clientPair.getRight();
	}

	@Override
	public LightSmoothness lightSmoothness() {
		return client.lightSmoothness.get();
	}

	public void registerSpecs(ModLoadingContext context) {
		context.registerConfig(ModConfig.Type.CLIENT, clientSpec, "flywheel-backend.toml");
	}

	public static class ClientConfig {
		public final ForgeConfigSpec.EnumValue<LightSmoothness> lightSmoothness;

		private ClientConfig(ForgeConfigSpec.Builder builder) {
			lightSmoothness = builder.comment("How smooth flywheel's shader-based lighting should be. May have a large performance impact.")
					.defineEnum("lightSmoothness", LightSmoothness.SMOOTH);
		}
	}
}
