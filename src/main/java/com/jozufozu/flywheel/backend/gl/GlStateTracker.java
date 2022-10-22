package com.jozufozu.flywheel.backend.gl;

import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.mojang.blaze3d.platform.GlStateManager;

/**
 * Tracks bound buffers/vbos because GlStateManager doesn't do that for us.
 */
public class GlStateTracker {
	private static final int[] BUFFERS = new int[GlBufferType.values().length];
	private static int vao;
	private static int program;

	public static int getBuffer(GlBufferType type) {
		return BUFFERS[type.ordinal()];
	}

	public static int getVertexArray() {
		return vao;
	}

	public static int getProgram() {
		return program;
	}

	public static void _setBuffer(GlBufferType type, int id) {
		BUFFERS[type.ordinal()] = id;
	}

	public static void _setVertexArray(int id) {
		vao = id;
	}

	public static void _setProgram(int id) {
		program = id;
	}

	public static State getRestoreState() {
		return new State(BUFFERS.clone(), vao, program);
	}

	public record State(int[] buffers, int vao, int program) {
		public void restore() {
			if (vao != GlStateTracker.vao) {
				GlStateManager._glBindVertexArray(vao);
			}

			GlBufferType[] values = GlBufferType.values();

			for (int i = 0; i < values.length; i++) {
				if (buffers[i] != GlStateTracker.BUFFERS[i] && values[i] != GlBufferType.ELEMENT_ARRAY_BUFFER) {
					GlStateManager._glBindBuffer(values[i].glEnum, buffers[i]);
				}
			}

			if (program != GlStateTracker.program) {
				GlStateManager._glUseProgram(program);
			}
		}
	}
}
