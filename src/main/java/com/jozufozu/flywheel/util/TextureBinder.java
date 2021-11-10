package com.jozufozu.flywheel.util;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;

/**
 * This is a silly hack that's needed because flywheel does things too different from vanilla.
 *
 * <p>
 *     When a {@link RenderType} is setup, the associated textures are passed to the active ShaderInstance, and properly
 *     bound later on when {@link ShaderInstance#apply()} is called.
 *     This interface (and {@link com.jozufozu.flywheel.mixin.ShaderInstanceMixin mixin} binds textures to opengl
 *     without binding the shader.
 * </p>
 */
public interface TextureBinder {

	/**
	 * Call this after calling {@link RenderType#setupRenderState()}.
	 */
	static void bindActiveTextures() {
		TextureBinder shader = (TextureBinder) RenderSystem.getShader();
		if (shader != null)
			shader.bind();
	}

	void bind();
}
