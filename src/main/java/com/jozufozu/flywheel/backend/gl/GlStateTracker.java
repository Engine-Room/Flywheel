package com.jozufozu.flywheel.backend.gl;

import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.mojang.blaze3d.platform.GlStateManager;

/**
 * Tracks bound buffers/vbos because GlStateManager doesn't do that for us.
 */
public class GlStateTracker {

	private static final int[] buffers = new int[GlBufferType.values().length];
	private static int vao;
	private static int program;

	public static int getBuffer(GlBufferType type) {
		return buffers[type.ordinal()];
	}

	public static int getVertexArray() {
		return vao;
	}

	public static int getProgram() {
		return program;
	}

	public static void _setBuffer(GlBufferType type, int buffer) {
		buffers[type.ordinal()] = buffer;
	}

	public static void _setProgram(int id) {
		program = id;
	}

	public static void _setVertexArray(int id) {
		vao = id;
	}

	public static State getRestoreState() {
		return new State(buffers.clone(), vao, program);
	}

	public static record State(int[] buffers, int vao, int program) {
		public void restore() {
			GlBufferType[] values = GlBufferType.values();

			for (int i = 0; i < values.length; i++) {
				GlStateManager._glBindBuffer(values[i].glEnum, buffers[i]);
			}

			GlStateManager._glBindVertexArray(vao);
			GlStateManager._glUseProgram(program);
		}
	}
}
