package com.jozufozu.flywheel.config;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class FlwConfig {

	private static final FlwConfig INSTANCE = new FlwConfig();

	public final ClientConfig client;

	public FlwConfig() {
		Pair<ClientConfig, ForgeConfigSpec> client = new ForgeConfigSpec.Builder().configure(ClientConfig::new);

		this.client = client.getLeft();

		ModLoadingContext.get()
				.registerConfig(ModConfig.Type.CLIENT, client.getRight());
	}

	public static FlwConfig get() {
		return INSTANCE;
	}

	public BackendType getBackendType() {
		return client.backend.get();
	}

	public boolean limitUpdates() {
		return client.limitUpdates.get();
	}

	public static void init() {
	}

	public static class ClientConfig {
		public final EnumValue<BackendType> backend;
		public final BooleanValue limitUpdates;

		public ClientConfig(ForgeConfigSpec.Builder builder) {
			backend = builder.comment("Select the backend to use.")
					.defineEnum("backend", BackendType.INSTANCING);

			limitUpdates = builder.comment("Enable or disable instance update limiting with distance.")
					.define("limitUpdates", true);
		}
	}
}
