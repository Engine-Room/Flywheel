package com.jozufozu.flywheel.api.material;

import java.util.function.Supplier;

import com.jozufozu.flywheel.core.source.FileResolution;

import net.minecraft.client.renderer.RenderType;

public class Material {
	protected final RenderType renderType;
	protected final Supplier<FileResolution> vertexShader;
	protected final Supplier<FileResolution> fragmentShader;

	public Material(RenderType renderType, Supplier<FileResolution> vertexShader, Supplier<FileResolution> fragmentShader) {
		this.renderType = renderType;
		this.vertexShader = vertexShader;
		this.fragmentShader = fragmentShader;
	}

	public RenderType getRenderType() {
		return renderType;
	}

	public FileResolution getVertexShader() {
		return vertexShader.get();
	}

	public FileResolution getFragmentShader() {
		return fragmentShader.get();
	}
}
