package com.jozufozu.flywheel.config;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.jozufozu.flywheel.api.backend.Backend;
import com.jozufozu.flywheel.api.backend.BackendManager;
import com.mojang.logging.LogUtils;

import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class FlwConfig {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final FlwConfig INSTANCE = new FlwConfig();

	public final ClientConfig client;
	private final ForgeConfigSpec clientSpec;

	private FlwConfig() {
		Pair<ClientConfig, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
		this.client = clientPair.getLeft();
		clientSpec = clientPair.getRight();
	}

	public static FlwConfig get() {
		return INSTANCE;
	}

	public Backend getBackend() {
		Backend backend = parseBackend(client.backend.get());
		if (backend == null) {
			backend = BackendManager.getDefaultBackend();
			client.backend.set(Backend.REGISTRY.getIdOrThrow(backend).toString());
		}

		return backend;
	}

	@Nullable
	private static Backend parseBackend(String idStr) {
		ResourceLocation backendId;
		try {
			backendId = new ResourceLocation(idStr);
		} catch (ResourceLocationException e) {
			LOGGER.warn("Config contains invalid backend ID '" + idStr + "'!");
			return null;
		}

		Backend backend = Backend.REGISTRY.get(backendId);
		if (backend == null) {
			LOGGER.warn("Config contains non-existent backend with ID '" + backendId + "'!");
			return null;
		}

		return backend;
	}

	public boolean limitUpdates() {
		return client.limitUpdates.get();
	}

	public void registerSpecs(ModLoadingContext context) {
		context.registerConfig(ModConfig.Type.CLIENT, clientSpec);
	}

	public static class ClientConfig {
		public final ConfigValue<String> backend;
		public final BooleanValue limitUpdates;

		private ClientConfig(ForgeConfigSpec.Builder builder) {
			backend = builder.comment("Select the backend to use.")
					.define("backend", Backend.REGISTRY.getIdOrThrow(BackendManager.getDefaultBackend()).toString());

			limitUpdates = builder.comment("Enable or disable instance update limiting with distance.")
					.define("limitUpdates", true);
		}
	}
}
