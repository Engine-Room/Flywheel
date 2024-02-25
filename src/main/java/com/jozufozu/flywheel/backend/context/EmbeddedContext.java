package com.jozufozu.flywheel.backend.context;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.visualization.EmbeddedLevel;
import com.jozufozu.flywheel.backend.engine.textures.TextureSource;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.mojang.blaze3d.vertex.PoseStack;

public class EmbeddedContext implements Context {
	private final EmbeddedLevel world;

	public EmbeddedContext(EmbeddedLevel world) {
		this.world = world;
	}

	@Override
	public ContextShader contextShader() {
		return ContextShaders.EMBEDDED;
	}

	@Override
	public void prepare(Material material, GlProgram shader, TextureSource textureSource) {
		var stack = new PoseStack();
		world.transform(stack);

		//		shader.setVec3("create_oneOverLightBoxSize");
		//		shader.setVec3("create_lightVolumeMin");
		shader.setMat4("_flw_model", stack.last()
				.pose());
		shader.setMat3("_flw_normal", stack.last()
				.normal());
	}
}
