package dev.engine_room.flywheel.impl;

import java.util.ArrayList;
import java.util.List;

import com.electronwill.nightconfig.core.Config;

import dev.engine_room.flywheel.api.backend.BackendVersion;
import dev.engine_room.flywheel.api.event.ReloadLevelRendererEvent;
import dev.engine_room.flywheel.impl.compat.CompatMod;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.forgespi.language.IModInfo;

public class FlwImplXplatImpl implements FlwImplXplat {
	@Override
	public boolean isModLoaded(String modId) {
		return LoadingModList.get().getModFileById(modId) != null;
	}

	@Override
	public void dispatchReloadLevelRendererEvent(ClientLevel level) {
		MinecraftForge.EVENT_BUS.post(new ReloadLevelRendererEvent(level));
	}

	@Override
	public String getVersionStr() {
		return FlywheelForge.version().toString();
	}

	@Override
	public FlwConfig getConfig() {
		return ForgeFlwConfig.INSTANCE;
	}

	@Override
	public boolean useSodium0_6Compat() {
		return CompatMod.SODIUM.isLoaded && !CompatMod.EMBEDDIUM.isLoaded;
	}

	@Override
	public boolean useIrisCompat() {
		return CompatMod.IRIS.isLoaded || CompatMod.OCULUS.isLoaded;
	}

	@Override
	public ModRequirements modRequirements() {
		List<ModRequirements.Entry> entries = new ArrayList<>();

		ModList.get()
				.forEachModFile(file -> {
					var info = file.getModFileInfo();

					for (IModInfo mod : info.getMods()) {
						var modProperties = mod.getModProperties()
								.get("flywheel");

						// There's no well-defined API for custom properties like in fabric.
						// It just returns an object, but internally it's represented with nightconfig.
						if (modProperties instanceof Config config) {
							// Minor version defaults to 0, major is required
							int major = config.getIntOrElse("backend_major_version", Integer.MIN_VALUE);
							int minor = config.getIntOrElse("backend_minor_version", 0);

							if (major != Integer.MIN_VALUE) {
								entries.add(new ModRequirements.Entry(mod.getModId(), new BackendVersion(major, minor)));
							} else {
								FlwImpl.LOGGER.warn("Mod {} has invalid required backend version", mod.getModId());
							}
						} else {
							FlwImpl.LOGGER.warn("Mod {} has invalid flywheel properties", mod.getModId());
						}
					}
				});

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
}
