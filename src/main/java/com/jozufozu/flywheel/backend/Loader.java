package com.jozufozu.flywheel.backend;

import com.jozufozu.flywheel.backend.compile.FlwCompiler;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.glsl.ShaderSources;
import com.jozufozu.flywheel.glsl.error.ErrorReporter;

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
		if (minecraft == null) {
			return;
		}

		if (minecraft.getResourceManager() instanceof ReloadableResourceManager reloadable) {
			reloadable.registerReloadListener(this);
		}
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		Backend.refresh();

		var errorReporter = new ErrorReporter();
		ShaderSources sources = new ShaderSources(errorReporter, manager);

		if (FlwCompiler.INSTANCE != null) {
			FlwCompiler.INSTANCE.delete();
		}

		FlwCompiler.INSTANCE = new FlwCompiler(sources);

		ClientLevel level = Minecraft.getInstance().level;
		if (Backend.canUseInstancing(level)) {
			InstancedRenderDispatcher.resetInstanceLevel(level);
		}

	}
}
