package dev.engine_room.flywheel.impl;

import java.util.ArrayList;
import java.util.List;

import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.api.backend.Backend;
import dev.engine_room.flywheel.api.backend.BackendVersion;
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
			.version(new BackendVersion(0, 0))
			.register(Flywheel.rl("off"));

	public static final Backend DEFAULT_BACKEND = findDefaultBackend();

	private static Backend backend = OFF_BACKEND;

	private BackendManagerImpl() {
	}

	public static Backend currentBackend() {
		return backend;
	}

	public static boolean isBackendOn() {
		return backend != OFF_BACKEND;
	}

	// Don't store this statically because backends can theoretically change their priorities at runtime.
	private static List<Backend> backendsByPriority() {
		var backends = new ArrayList<>(Backend.REGISTRY.getAll());

		// Sort with keys backwards so that the highest priority is first.
		backends.sort((a, b) -> Integer.compare(b.priority(), a.priority()));
		return backends;
	}

	private static Backend findDefaultBackend() {
		var backendsByPriority = backendsByPriority();
		if (backendsByPriority.isEmpty()) {
			// This probably shouldn't happen, but fail gracefully.
			FlwImpl.LOGGER.warn("No backends registered, defaulting to 'flywheel:off'");
			return OFF_BACKEND;
		}

		return backendsByPriority.get(0);
	}

	private static void chooseBackend() {
		var requirements = FlwImplXplat.INSTANCE.modRequirements();

		var preferred = FlwConfig.INSTANCE.backend();
		if (preferred.isSupported()) {
			if (meetsModRequirements(requirements, preferred)) {
				backend = preferred;
				return;
			} else {
				var minVersion = requirements.minimumBackendVersion();
				FlwImpl.LOGGER.warn("Preferred backend '{}' does not meet minimum version requirement {}.{}", Backend.REGISTRY.getIdOrThrow(preferred), minVersion.major(), minVersion.minor());
			}
		}

		backend = fallback(preferred, requirements);

		FlwImpl.LOGGER.warn("Flywheel backend fell back from '{}' to '{}'", Backend.REGISTRY.getIdOrThrow(preferred), Backend.REGISTRY.getIdOrThrow(backend));
	}

	private static Backend fallback(Backend preferred, ModRequirements requirements) {
		var backendsByPriority = backendsByPriority();

		var startIndex = backendsByPriority.indexOf(preferred) + 1;

		// For safety in case we don't find anything
		var out = OFF_BACKEND;
		for (int i = startIndex; i < backendsByPriority.size(); i++) {
			var candidate = backendsByPriority.get(i);
			if (candidate.isSupported()) {
				if (meetsModRequirements(requirements, candidate)) {
					out = candidate;
					break;
				}
			}
		}
		return out;
	}

	private static boolean meetsModRequirements(ModRequirements requirements, Backend candidate) {
		return candidate.version()
				.compareTo(requirements.minimumBackendVersion()) >= 0;
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

	public static void onReloadLevelRenderer(ClientLevel level) {
		chooseBackend();
		VisualizationManagerImpl.reset(level);
	}
}
