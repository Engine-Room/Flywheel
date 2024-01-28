package com.jozufozu.flywheel.api.material;

import com.jozufozu.flywheel.api.internal.InternalFlywheelApi;
import com.jozufozu.flywheel.api.registry.Registry;

import net.minecraft.resources.ResourceLocation;

public interface FogShader {
	static Registry<FogShader> REGISTRY = InternalFlywheelApi.INSTANCE.createRegistry();

	ResourceLocation source();
}
