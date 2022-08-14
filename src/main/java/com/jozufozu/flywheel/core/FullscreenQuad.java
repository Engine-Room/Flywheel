package com.jozufozu.flywheel.core;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.backend.gl.array.GlVertexArray;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.core.layout.CommonItems;
import com.jozufozu.flywheel.util.Lazy;

public class FullscreenQuad {

	public static final Lazy<FullscreenQuad> INSTANCE = Lazy.of(FullscreenQuad::new);
	private static final BufferLayout LAYOUT = BufferLayout.builder()
			.addItems(CommonItems.VEC4)
			.build();

	private static final float[] vertices = {
			// pos          // tex
			-1.0f, -1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f,

			-1.0f, -1.0f, 0.0f, 0.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f};

	private static final int bufferSize = vertices.length * 4;

	private final GlVertexArray vao;
	private final GlBuffer vbo;

	private FullscreenQuad() {
		try (var restoreState = GlStateTracker.getRestoreState()) {
			vbo = new GlBuffer(GlBufferType.ARRAY_BUFFER);
			vbo.ensureCapacity(bufferSize);
			try (MappedBuffer buffer = vbo.map()) {
				var ptr = buffer.getPtr();

				for (var i = 0; i < vertices.length; i++) {
					MemoryUtil.memPutFloat(ptr + i * Float.BYTES, vertices[i]);
				}

			} catch (Exception e) {
				Flywheel.LOGGER.error("Could not create fullscreen quad.", e);
			}

			vao = new GlVertexArray();

			vao.enableArrays(1);

            vao.bindAttributes(vbo, 0, LAYOUT, 0L);
        }
	}

	public void draw() {
		vao.bind();
		glDrawArrays(GL_TRIANGLES, 0, 6);
		GlVertexArray.unbind();
	}

	public void delete() {
		vao.delete();
		vbo.delete();
	}
}
