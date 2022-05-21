package com.jozufozu.flywheel.api.material;

import com.jozufozu.flywheel.core.source.FileResolution;

import net.minecraft.client.renderer.RenderType;

public class Material {
	protected final RenderType renderType;
	protected final FileResolution vertexShader;
	protected final FileResolution fragmentShader;

	public Material(RenderType renderType, FileResolution vertexShader, FileResolution fragmentShader) {
		this.renderType = renderType;
		this.vertexShader = vertexShader;
		this.fragmentShader = fragmentShader;
	}

	public RenderType getRenderType() {
		return renderType;
	}

	public FileResolution getVertexShader() {
		return vertexShader;
	}

	public FileResolution getFragmentShader() {
		return fragmentShader;
	}
}
