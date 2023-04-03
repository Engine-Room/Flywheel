package com.jozufozu.flywheel.backend;

import com.jozufozu.flywheel.api.backend.BackendManager;
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
	public static final Loader INSTANCE = new Loader();

	private Loader() {
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		BackendManager.refresh();

		var errorReporter = new ErrorReporter();
		ShaderSources sources = new ShaderSources(errorReporter, manager);

		if (FlwCompiler.INSTANCE != null) {
			FlwCompiler.INSTANCE.delete();
		}

		FlwCompiler.INSTANCE = new FlwCompiler(sources);

		ClientLevel level = Minecraft.getInstance().level;
		if (BackendUtil.canUseInstancing(level)) {
			InstancedRenderDispatcher.resetInstanceLevel(level);
		}
	}

	public static void init() {
		// Can be null when running datagenerators due to the unfortunate time we call this
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft == null) {
			return;
		}

		if (minecraft.getResourceManager() instanceof ReloadableResourceManager reloadable) {
			reloadable.registerReloadListener(INSTANCE);
		}
	}
}
