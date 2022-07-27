package com.jozufozu.flywheel.core;

import org.lwjgl.opengl.GL46;
import org.lwjgl.system.MemoryStack;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.core.compile.DebugCompiler;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.util.Lazy;
import com.jozufozu.flywheel.util.joml.FrustumIntersection;
import com.mojang.blaze3d.systems.RenderSystem;

public class DebugRender {

	private static final Lazy<GlProgram> SHADER = Lazy.of(() -> DebugCompiler.INSTANCE.get(new DebugCompiler.Context(Files.VERTEX, Files.FRAGMENT)));

	private static final Lazy<Frustum> FRUSTUM_VBO = Lazy.of(Frustum::new);

	public static void init() {
		Files.init();
	}

	public static void updateFrustum(FrustumIntersection culler) {
		FRUSTUM_VBO.get()
				.upload(culler);
	}

	public static void drawFrustum() {
		if (!FRUSTUM_VBO.isInitialized()) {
			return;
		}

		RenderSystem.disableCull();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		try (var ignored = GlStateTracker.getRestoreState()) {
			SHADER.get()
					.bind();
			FRUSTUM_VBO.get()
					.draw();
		}
	}

	public static class Files {
		public static final FileResolution VERTEX = FileResolution.get(Flywheel.rl("debug/debug.vert"));
		public static final FileResolution FRAGMENT = FileResolution.get(Flywheel.rl("debug/debug.frag"));

		public static void init() {

		}
	}

	// FIXME: This never worked (and the thing it was meant to debug is already fixed),
	//  but it should be a quick turnaround
	private static class Frustum {
		private static final int[] indices = new int[]{
				0, 2, 3, 0, 3, 1,
				2, 6, 7, 2, 7, 3,
				6, 4, 5, 6, 5, 7,
				4, 0, 1, 4, 1, 5,
				0, 4, 6, 0, 6, 2,
				1, 5, 7, 1, 7, 3,
		};

		private static final int elementCount = indices.length;
		private static final int indicesSize = elementCount * 4;
		private static final int verticesSize = 3 * 8 * 4;
		private final int buffer;
		private final int vao;

		public Frustum() {
			// holy moly DSA is nice
			buffer = GL46.glCreateBuffers();
			GL46.glNamedBufferStorage(buffer, verticesSize + indicesSize, GL46.GL_DYNAMIC_STORAGE_BIT);
			GL46.glNamedBufferSubData(buffer, 0, indices);

			vao = GL46.glCreateVertexArrays();
			GL46.glEnableVertexArrayAttrib(vao, 0);
			GL46.glVertexArrayElementBuffer(vao, buffer);
			GL46.glVertexArrayVertexBuffer(vao, 0, buffer, indicesSize, 3 * 4);
			GL46.glVertexArrayAttribFormat(vao, 0, 3, GL46.GL_FLOAT, false, 0);
		}

		public void upload(FrustumIntersection culler) {
			try (var stack = MemoryStack.stackPush()) {
				var buf = stack.malloc(3 * 8 * 4);

				culler.bufferPlanes(buf);

				GL46.glNamedBufferSubData(buffer, indicesSize, buf);
			}
		}

		public void draw() {
			GL46.glEnableVertexArrayAttrib(vao, 0);
			GL46.glVertexArrayElementBuffer(vao, buffer);
			GL46.glBindVertexArray(vao);
			GL46.glDrawElements(GL46.GL_TRIANGLES, elementCount, GL46.GL_UNSIGNED_INT, 0);
		}
	}
}
