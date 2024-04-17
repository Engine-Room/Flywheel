package com.jozufozu.flywheel.backend.engine.uniform;

import com.mojang.blaze3d.systems.RenderSystem;

public final class FogUniforms extends UniformWriter {
	private static final int SIZE = 4 * 7;
	static final UniformBuffer BUFFER = new UniformBuffer(Uniforms.FOG_INDEX, SIZE);

	public static void update() {
		long ptr = BUFFER.ptr();

		var color = RenderSystem.getShaderFogColor();

		ptr = writeFloat(ptr, color[0]);
		ptr = writeFloat(ptr, color[1]);
		ptr = writeFloat(ptr, color[2]);
		ptr = writeFloat(ptr, color[3]);
		ptr = writeFloat(ptr, RenderSystem.getShaderFogStart());
		ptr = writeFloat(ptr, RenderSystem.getShaderFogEnd());
		ptr = writeInt(ptr, RenderSystem.getShaderFogShape()
				.getIndex());

		BUFFER.markDirty();
	}
}
