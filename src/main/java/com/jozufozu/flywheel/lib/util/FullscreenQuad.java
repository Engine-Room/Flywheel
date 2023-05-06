package com.jozufozu.flywheel.lib.util;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.layout.BufferLayout;
import com.jozufozu.flywheel.gl.GlStateTracker;
import com.jozufozu.flywheel.gl.array.GlVertexArray;
import com.jozufozu.flywheel.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.gl.buffer.MappedBuffer;
import com.jozufozu.flywheel.lib.layout.CommonItems;
import com.jozufozu.flywheel.util.Lazy;

public class FullscreenQuad {

	public static final Lazy<FullscreenQuad> INSTANCE = Lazy.of(FullscreenQuad::new);
	private static final BufferLayout LAYOUT = BufferLayout.builder()
			.addItem(CommonItems.VEC4, "posTex")
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
			vbo = new GlBuffer();
			vbo.ensureCapacity(bufferSize);
			try (MappedBuffer buffer = vbo.map()) {
				var ptr = buffer.ptr();

				for (var i = 0; i < vertices.length; i++) {
					MemoryUtil.memPutFloat(ptr + i * Float.BYTES, vertices[i]);
				}

			} catch (Exception e) {
				Flywheel.LOGGER.error("Could not create fullscreen quad.", e);
			}

			vao = GlVertexArray.create();

			vao.bindVertexBuffer(0, vbo.handle(), 0L, LAYOUT.getStride());
			vao.bindAttributes(0, 0, LAYOUT.attributes());
		}
	}

	/**
	 * Draw the fullscreen quad.<br>
	 * note: may bind a VAO, but will not restore prior state.
	 */
	public void draw() {
		vao.bindForDraw();
		glDrawArrays(GL_TRIANGLES, 0, 6);
	}

	public void delete() {
		vao.delete();
		vbo.delete();
	}
}
