package com.jozufozu.flywheel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jozufozu.flywheel.backend.Backends;
import com.jozufozu.flywheel.backend.ShaderIndices;
import com.jozufozu.flywheel.impl.BackendManagerImpl;
import com.jozufozu.flywheel.impl.registry.IdRegistryImpl;
import com.jozufozu.flywheel.impl.registry.RegistryImpl;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.material.CutoutShaders;
import com.jozufozu.flywheel.lib.material.FogShaders;
import com.jozufozu.flywheel.lib.material.StandardMaterialShaders;
import com.jozufozu.flywheel.lib.util.ShadersModHandler;

import net.minecraft.resources.ResourceLocation;

public class Flywheel {
	public static final String ID = "flywheel";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	public static ResourceLocation rl(String path) {
		return new ResourceLocation(ID, path);
	}

	public static void earlyInit() {
		BackendManagerImpl.init();

		ShadersModHandler.init();

		Backends.init();
	}

	public static void init() {
		InstanceTypes.init();
		CutoutShaders.init();
		FogShaders.init();
		StandardMaterialShaders.init();

		ShaderIndices.init();
	}

	public static void freeze() {
		RegistryImpl.freezeAll();
		IdRegistryImpl.freezeAll();
	}
}
