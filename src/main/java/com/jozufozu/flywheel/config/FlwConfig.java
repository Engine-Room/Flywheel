package com.jozufozu.flywheel.config;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.backend.BackendType;
import com.jozufozu.flywheel.lib.backend.BackendTypes;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
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

	@Nullable
	public BackendType getBackendType() {
		return BackendTypes.getBackendType(client.backend.get());
	}

	public boolean limitUpdates() {
		return client.limitUpdates.get();
	}

	public static void init() {
	}

	public static class ClientConfig {
		public final ForgeConfigSpec.ConfigValue<String> backend;
		public final BooleanValue limitUpdates;

		public ClientConfig(ForgeConfigSpec.Builder builder) {
			backend = builder.comment("Select the backend to use.")
					.define("backend", BackendTypes.defaultForCurrentPC()
							.getShortName());

			limitUpdates = builder.comment("Enable or disable instance update limiting with distance.")
					.define("limitUpdates", true);
		}
	}
}
