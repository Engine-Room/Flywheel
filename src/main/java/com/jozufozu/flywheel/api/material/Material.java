package com.jozufozu.flywheel.api.material;

import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.core.source.FileResolution;

import net.minecraft.client.renderer.RenderType;

public interface Material {
	RenderStage getRenderStage();

	RenderType getRenderType();

	FileResolution getVertexShader();

	FileResolution getFragmentShader();
}
