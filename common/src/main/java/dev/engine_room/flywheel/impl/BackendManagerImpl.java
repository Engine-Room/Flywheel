package dev.engine_room.flywheel.impl;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.api.backend.Backend;
import dev.engine_room.flywheel.backend.Backends;
import dev.engine_room.flywheel.impl.visualization.VisualizationManagerImpl;
import dev.engine_room.flywheel.lib.backend.SimpleBackend;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;

public final class BackendManagerImpl {
	public static final Backend OFF_BACKEND = SimpleBackend.builder()
			.engineFactory(level -> {
				throw new UnsupportedOperationException("Cannot create engine when backend is off.");
			})
			.supported(() -> true)
			.register(Flywheel.rl("off"));

	public static final Backend DEFAULT_BACKEND = findDefaultBackend();

	private static Backend backend = OFF_BACKEND;

	private BackendManagerImpl() {
	}

	public static Backend getBackend() {
		return backend;
	}

	public static boolean isBackendOn() {
		return backend != OFF_BACKEND;
	}

	private static Backend findDefaultBackend() {
		// TODO: Automatically select the best default config based on the user's driver
		// TODO: Figure out how this will work if custom backends are registered and without hardcoding the default backends
		return Backends.INDIRECT;
	}

	private static void chooseBackend() {
		var preferred = FlwConfig.INSTANCE.backend();
		var actual = preferred.findFallback();

		if (preferred != actual) {
			FlwImpl.LOGGER.warn("Flywheel backend fell back from '{}' to '{}'", Backend.REGISTRY.getIdOrThrow(preferred), Backend.REGISTRY.getIdOrThrow(actual));
		}

		backend = actual;
	}

	public static String getBackendString() {
		ResourceLocation backendId = Backend.REGISTRY.getId(backend);
		if (backendId == null) {
			return "[unregistered]";
		}
		return backendId.toString();
	}

	public static void init() {
	}

	public static void onEndClientResourceReload(boolean didError) {
		if (didError) {
			return;
		}

		chooseBackend();
		VisualizationManagerImpl.resetAll();
	}

	public static void onReloadLevelRenderer(@Nullable ClientLevel level) {
		chooseBackend();

		if (level != null) {
			VisualizationManagerImpl.reset(level);
		}
	}
}
