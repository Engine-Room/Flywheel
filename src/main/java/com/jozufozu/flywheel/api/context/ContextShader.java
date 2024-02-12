package com.jozufozu.flywheel.api.context;

import com.jozufozu.flywheel.api.internal.InternalFlywheelApi;
import com.jozufozu.flywheel.api.registry.Registry;

import net.minecraft.resources.ResourceLocation;

public interface ContextShader {
	static Registry<ContextShader> REGISTRY = InternalFlywheelApi.INSTANCE.createRegistry();

	ResourceLocation vertexShader();

	ResourceLocation fragmentShader();
}
