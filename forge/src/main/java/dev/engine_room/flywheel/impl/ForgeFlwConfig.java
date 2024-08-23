package dev.engine_room.flywheel.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.backend.Backend;
import dev.engine_room.flywheel.api.backend.BackendManager;
import dev.engine_room.flywheel.backend.BackendConfig;
import dev.engine_room.flywheel.backend.FlwBackendXplatImpl;
import dev.engine_room.flywheel.backend.compile.LightSmoothness;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ForgeFlwConfig implements FlwConfig {
	public static final ForgeFlwConfig INSTANCE = new ForgeFlwConfig();

	public final ClientConfig client;
	private final ModConfigSpec clientSpec;

	private ForgeFlwConfig() {
		Pair<ClientConfig, ModConfigSpec> clientPair = new ModConfigSpec.Builder().configure(ClientConfig::new);
		this.client = clientPair.getLeft();
		clientSpec = clientPair.getRight();

		FlwBackendXplatImpl.CONFIG = client.backendConfig;
	}

	@Override
	public Backend backend() {
		Backend backend = parseBackend(client.backend.get());
		if (backend == null) {
			backend = BackendManager.defaultBackend();
			client.backend.set(Backend.REGISTRY.getIdOrThrow(backend).toString());
		}

		return backend;
	}

	@Nullable
	private static Backend parseBackend(String idStr) {
		ResourceLocation backendId;
		try {
			backendId = ResourceLocation.parse(idStr);
		} catch (ResourceLocationException e) {
			FlwImpl.CONFIG_LOGGER.warn("'backend' value '{}' is not a valid resource location", idStr);
			return null;
		}

		Backend backend = Backend.REGISTRY.get(backendId);
		if (backend == null) {
			FlwImpl.CONFIG_LOGGER.warn("Backend with ID '{}' is not registered", backendId);
			return null;
		}

		return backend;
	}

	@Override
	public boolean limitUpdates() {
		return client.limitUpdates.get();
	}

	@Override
	public int workerThreads() {
		return client.workerThreads.get();
	}

	public void registerSpecs(ModContainer context) {
		context.registerConfig(ModConfig.Type.CLIENT, clientSpec);
	}

	public static class ClientConfig {
		public final ModConfigSpec.ConfigValue<String> backend;
		public final ModConfigSpec.BooleanValue limitUpdates;
		public final ModConfigSpec.IntValue workerThreads;

		public final ForgeBackendConfig backendConfig;

		private ClientConfig(ModConfigSpec.Builder builder) {
			backend = builder.comment("Select the backend to use.")
					.define("backend", Backend.REGISTRY.getIdOrThrow(BackendManager.defaultBackend()).toString());

			limitUpdates = builder.comment("Enable or disable instance update limiting with distance.")
					.define("limitUpdates", true);

			workerThreads = builder.comment("The number of worker threads to use. Set to -1 to let Flywheel decide. Set to 0 to disable parallelism. Requires a game restart to take effect.")
					.defineInRange("workerThreads", -1, -1, Runtime.getRuntime()
							.availableProcessors());

			builder.comment("Config options for flywheel's build-in backends.")
					.push("flw_backends");

			backendConfig = new ForgeBackendConfig(builder);
		}
	}

	public static class ForgeBackendConfig implements BackendConfig {
		public final ForgeConfigSpec.EnumValue<LightSmoothness> lightSmoothness;

		public ForgeBackendConfig(ForgeConfigSpec.Builder builder) {
			lightSmoothness = builder.comment("How smooth flywheel's shader-based lighting should be. May have a large performance impact.")
					.defineEnum("lightSmoothness", LightSmoothness.SMOOTH);
		}

		@Override
		public LightSmoothness lightSmoothness() {
			return lightSmoothness.get();
		}
	}
}
