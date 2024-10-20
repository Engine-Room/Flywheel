package dev.engine_room.flywheel.impl;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.backend.BackendVersion;
import dev.engine_room.flywheel.api.event.ReloadLevelRendererCallback;
import dev.engine_room.flywheel.impl.compat.CompatMod;
import dev.engine_room.flywheel.impl.compat.FabricSodiumCompat;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.minecraft.client.multiplayer.ClientLevel;

public class FlwImplXplatImpl implements FlwImplXplat {
	@Override
	public boolean isModLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	@Override
	public void dispatchReloadLevelRendererEvent(ClientLevel level) {
		ReloadLevelRendererCallback.EVENT.invoker().onReloadLevelRenderer(level);
	}

	@Override
	public String getVersionStr() {
		return FlywheelFabric.version().getFriendlyString();
	}

	@Override
	public FlwConfig getConfig() {
		return FabricFlwConfig.INSTANCE;
	}

	@Override
	public boolean useSodium0_6Compat() {
		return FabricSodiumCompat.USE_0_6_COMPAT;
	}

	@Override
	public boolean useIrisCompat() {
		return CompatMod.IRIS.isLoaded;
	}

	@Override
	public ModRequirements modRequirements() {
		List<ModRequirements.Entry> entries = new ArrayList<>();

		for (ModContainer mod : FabricLoader.getInstance()
				.getAllMods()) {
			var metadata = mod.getMetadata();

			var custom = metadata.getCustomValue("flywheel");

			if (custom != null && custom.getType() == CustomValue.CvType.OBJECT) {
				var object = custom.getAsObject();

				var major = getCustomValueAsInt(object.get("backend_major_version"));
				var minor = getCustomValueAsInt(object.get("backend_minor_version"));

				// Major version is required
				if (major != null) {
					// Minor version defaults to 0
					var version = new BackendVersion(major, minor == null ? 0 : minor);

					entries.add(new ModRequirements.Entry(metadata.getId(), version));
				} else {
					FlwImpl.LOGGER.warn("Mod {} has invalid required backend version", metadata.getId());
				}
			}
		}

		if (!entries.isEmpty()) {
			var minVersion = entries.stream()
					.map(ModRequirements.Entry::version)
					.min(BackendVersion::compareTo)
					.get();

			return new ModRequirements(minVersion, entries);
		} else {
			return new ModRequirements(new BackendVersion(0, 0), List.of());
		}
	}

	@Nullable
	private static Integer getCustomValueAsInt(@Nullable CustomValue major) {
		if (major != null && major.getType() == CustomValue.CvType.NUMBER) {
			return major.getAsNumber()
					.intValue();
		}

		return null;
	}
}
