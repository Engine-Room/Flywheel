package com.jozufozu.flywheel.impl;

import com.jozufozu.flywheel.backend.Backends;
import com.jozufozu.flywheel.backend.ShaderIndices;
import com.jozufozu.flywheel.impl.registry.IdRegistryImpl;
import com.jozufozu.flywheel.impl.registry.RegistryImpl;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.material.CutoutShaders;
import com.jozufozu.flywheel.lib.material.FogShaders;
import com.jozufozu.flywheel.lib.material.StandardMaterialShaders;
import com.jozufozu.flywheel.lib.util.ShadersModHandler;
import com.jozufozu.flywheel.vanilla.VanillaVisuals;

public final class FlywheelInit {
	private FlywheelInit() {
	}

	public static void init() {
		// impl
		BackendManagerImpl.init();

		// lib
		ShadersModHandler.init();
		InstanceTypes.init();
		CutoutShaders.init();
		FogShaders.init();
		StandardMaterialShaders.init();

		// backend
		ShaderIndices.init();
		Backends.init();

		// vanilla
		VanillaVisuals.init();
	}

	public static void freezeRegistries() {
		RegistryImpl.freezeAll();
		IdRegistryImpl.freezeAll();
	}
}
