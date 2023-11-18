package com.jozufozu.flywheel.backend;

import com.jozufozu.flywheel.backend.compile.FlwPrograms;
import com.jozufozu.flywheel.impl.BackendManagerImpl;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

/**
 * The main entity for loading shaders.
 *
 * <p>
 * This class is responsible for invoking the loading, parsing, and compilation stages.
 * </p>
 */
public class Loader implements ResourceManagerReloadListener {
	public static final Loader INSTANCE = new Loader();

	private Loader() {
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		FlwPrograms.reload(manager);

		// TODO: Move this to the impl package
		// TODO: To ensure this runs after all backends are ready, inject into Minecraft after the reload and before levelRenderer.allChanged()
		// Alternatively, consider adding API 
		// TODO: This should reset all VisualizationManagerImpls, not just the one for the static client level
		BackendManagerImpl.refresh(Minecraft.getInstance().level);
	}

	public static void init() {
		// Can be null when running data generators due to the unfortunate time we call this
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft == null) {
			return;
		}

		if (minecraft.getResourceManager() instanceof ReloadableResourceManager reloadable) {
			reloadable.registerReloadListener(INSTANCE);
		}
	}
}
