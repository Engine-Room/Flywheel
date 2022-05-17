package com.jozufozu.flywheel.backend;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.core.crumbling.CrumblingRenderer;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.ShaderLoadingException;
import com.jozufozu.flywheel.core.source.ShaderSources;
import com.jozufozu.flywheel.core.source.error.ErrorReporter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
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

	Loader() {
		// Can be null when running datagenerators due to the unfortunate time we call this
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft != null) {
			ResourceManager manager = minecraft.getResourceManager();
			if (manager instanceof ReloadableResourceManager) {
				((ReloadableResourceManager) manager).registerReloadListener(this);
			}
		}
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		Backend.refresh();

		var errorReporter = new ErrorReporter();
		ShaderSources sources = new ShaderSources(errorReporter, manager);

		FileResolution.run(errorReporter, sources);

		if (errorReporter.hasErrored()) {
			errorReporter.dump();
			throw new ShaderLoadingException("Failed to resolve all source files, see log for details");
		}

		Backend.LOGGER.info("Loaded all shader sources.");

		ClientLevel world = Minecraft.getInstance().level;
		if (Backend.canUseInstancing(world)) {
			// TODO: looks like it might be good to have another event here
			InstancedRenderDispatcher.resetInstanceWorld(world);
			CrumblingRenderer.reset();
		}

	}
}
