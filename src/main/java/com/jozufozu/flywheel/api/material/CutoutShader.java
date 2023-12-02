package com.jozufozu.flywheel.api.material;

import com.jozufozu.flywheel.api.registry.Registry;
import com.jozufozu.flywheel.impl.RegistryImpl;

import net.minecraft.resources.ResourceLocation;

public interface CutoutShader {
	static Registry<CutoutShader> REGISTRY = RegistryImpl.create();

	ResourceLocation source();
}
