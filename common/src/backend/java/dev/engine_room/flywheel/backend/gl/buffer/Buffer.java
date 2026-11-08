package dev.engine_room.flywheel.backend.gl.buffer;

import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GL46C;
import org.lwjgl.system.Checks;

import com.mojang.blaze3d.opengl.GlStateManager;

import dev.engine_room.flywheel.backend.gl.GlCompat;

public interface Buffer {
	Buffer IMPL = new DSA().fallback();

	int create();

	void data(int vbo, long size, long ptr, int glEnum);

	void subData(int vbo, long offset, long size, long ptr);

	class DSA implements Buffer {
		@Override
		public int create() {
			return GL46C.glCreateBuffers();
		}

		@Override
		public void data(int vbo, long size, long ptr, int glEnum) {
			GL46C.nglNamedBufferData(vbo, size, ptr, glEnum);
		}

		@Override
		public void subData(int vbo, long offset, long size, long ptr) {
			GL46C.nglNamedBufferSubData(vbo, offset, size, ptr);
		}

		public Buffer fallback() {
			if (GlCompat.ALLOW_DSA && dsaMethodsAvailable()) {
				return this;
			}
			return new Core();
		}

		private static boolean dsaMethodsAvailable() {
			var c = GlCompat.CAPABILITIES;
			return Checks.checkFunctions(c.glCreateBuffers, c.glNamedBufferData, c.glCopyNamedBufferSubData, c.glMapNamedBufferRange, c.glUnmapNamedBuffer);
		}
	}

	class Core implements Buffer {
		@Override
		public int create() {
			return GlStateManager._glGenBuffers();
		}

		@Override
		public void data(int vbo, long size, long ptr, int glEnum) {
			GlBufferType.COPY_WRITE_BUFFER.bind(vbo);
			GL33C.nglBufferData(GlBufferType.COPY_WRITE_BUFFER.glEnum, size, ptr, glEnum);
		}

		@Override
		public void subData(int vbo, long offset, long size, long ptr) {
			GlBufferType.COPY_WRITE_BUFFER.bind(vbo);
			GL33C.nglBufferSubData(GlBufferType.COPY_WRITE_BUFFER.glEnum, offset, size, ptr);
		}
	}
}
