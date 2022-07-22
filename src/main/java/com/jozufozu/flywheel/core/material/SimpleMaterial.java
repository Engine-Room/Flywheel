package com.jozufozu.flywheel.core.material;

import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.core.source.FileResolution;

import net.minecraft.client.renderer.RenderType;

public class SimpleMaterial implements Material {
	protected final RenderStage stage;
	protected final RenderType type;
	protected final FileResolution vertexShader;
	protected final FileResolution fragmentShader;

	public SimpleMaterial(RenderStage stage, RenderType type, FileResolution vertexShader, FileResolution fragmentShader) {
		this.stage = stage;
		this.type = type;
		this.vertexShader = vertexShader;
		this.fragmentShader = fragmentShader;
	}

	@Override
	public RenderStage getRenderStage() {
		return stage;
	}

	@Override
	public RenderType getRenderType() {
		return type;
	}

	@Override
	public FileResolution getVertexShader() {
		return vertexShader;
	}

	@Override
	public FileResolution getFragmentShader() {
		return fragmentShader;
	}
}
