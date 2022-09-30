package com.jozufozu.flywheel.backend;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.backend.instancing.compile.FlwCompiler;
import com.jozufozu.flywheel.core.source.FileResolution;
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

		Backend.LOGGER.info("Loaded all shader sources in " + sources.getLoadTime());

		FileResolution.run(errorReporter, sources);

		if (errorReporter.hasErrored()) {
			throw errorReporter.dump();
		}

		sources.postResolve();

		Backend.LOGGER.info("Successfully resolved all source files.");

		FileResolution.checkAll(errorReporter);

		Backend.LOGGER.info("All shaders passed checks.");

		FlwCompiler.INSTANCE.run();

		ClientLevel level = Minecraft.getInstance().level;
		if (Backend.canUseInstancing(level)) {
			InstancedRenderDispatcher.resetInstanceLevel(level);
		}

	}
}
