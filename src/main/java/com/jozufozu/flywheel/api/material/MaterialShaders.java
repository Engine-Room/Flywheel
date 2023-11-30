package com.jozufozu.flywheel.api.material;

import com.jozufozu.flywheel.api.registry.Registry;
import com.jozufozu.flywheel.impl.RegistryImpl;

import net.minecraft.resources.ResourceLocation;

public interface MaterialShaders {
	static Registry<MaterialShaders> REGISTRY = RegistryImpl.create();

	ResourceLocation vertexShader();

	ResourceLocation fragmentShader();
}
