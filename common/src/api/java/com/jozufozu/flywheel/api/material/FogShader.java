package com.jozufozu.flywheel.api.material;

import com.jozufozu.flywheel.api.internal.FlwApiLink;
import com.jozufozu.flywheel.api.registry.Registry;

import net.minecraft.resources.ResourceLocation;

public interface FogShader {
	static Registry<FogShader> REGISTRY = FlwApiLink.INSTANCE.createRegistry();

	ResourceLocation source();
}
