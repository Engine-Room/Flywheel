package com.jozufozu.flywheel.api.material;

import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.core.source.FileResolution;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public interface Material {
	RenderStage getRenderStage();

	RenderType getBatchingRenderType();

	FileResolution getVertexShader();

	FileResolution getFragmentShader();

	void setup();

	void clear();
}
