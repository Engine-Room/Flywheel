package dev.engine_room.flywheel.backend.gl.buffer;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.Checks;

import dev.engine_room.flywheel.backend.gl.GlCompat;

public interface Buffer {
	Buffer IMPL = new DSA().fallback();

	int create();

	void data(int vbo, long size, long ptr, int glEnum);

	void subData(int vbo, long offset, long size, long ptr);

	class DSA implements Buffer {
		@Override
		public int create() {
			return GL45C.glCreateBuffers();
		}

		@Override
		public void data(int vbo, long size, long ptr, int glEnum) {
			GL45C.nglNamedBufferData(vbo, size, ptr, glEnum);
		}

		@Override
		public void subData(int vbo, long offset, long size, long ptr) {
			GL45C.nglNamedBufferSubData(vbo, offset, size, ptr);
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
			return GL15.glGenBuffers();
		}

		@Override
		public void data(int vbo, long size, long ptr, int glEnum) {
			GlBufferType.COPY_WRITE_BUFFER.bind(vbo);
			GL15.nglBufferData(GlBufferType.COPY_WRITE_BUFFER.glEnum, size, ptr, glEnum);
		}

		@Override
		public void subData(int vbo, long offset, long size, long ptr) {
			GlBufferType.COPY_WRITE_BUFFER.bind(vbo);
			GL15.nglBufferSubData(GlBufferType.COPY_WRITE_BUFFER.glEnum, offset, size, ptr);
		}
	}
}
