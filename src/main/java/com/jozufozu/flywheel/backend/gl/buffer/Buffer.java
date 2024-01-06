package com.jozufozu.flywheel.backend.gl.buffer;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.Checks;

import com.jozufozu.flywheel.backend.gl.GlCompat;

public interface Buffer {
	Buffer IMPL = new DSA().fallback();

	int create();

	void data(int vbo, long size, long ptr, int glEnum);

	void copyData(int src, int dst, long srcOffset, long dstOffset, long size);

	long mapRange(int handle, int offset, long size, int access);

	void unmap(int handle);

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
		public void copyData(int src, int dst, long srcOffset, long dstOffset, long size) {
			GL45C.glCopyNamedBufferSubData(src, dst, srcOffset, dstOffset, size);
		}

		@Override
		public long mapRange(int handle, int offset, long size, int access) {
			return GL45C.nglMapNamedBufferRange(handle, offset, size, access);
		}

		@Override
		public void unmap(int handle) {
			GL45C.glUnmapNamedBuffer(handle);
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
		public void copyData(int src, int dst, long size, long srcOffset, long dstOffset) {
			GlBufferType.COPY_READ_BUFFER.bind(src);
			GlBufferType.COPY_WRITE_BUFFER.bind(dst);

			GL31.glCopyBufferSubData(GlBufferType.COPY_READ_BUFFER.glEnum, GlBufferType.COPY_WRITE_BUFFER.glEnum, srcOffset, dstOffset, size);
		}

		@Override
		public long mapRange(int handle, int offset, long size, int access) {
			GlBufferType.COPY_READ_BUFFER.bind(handle);
			return GL30.nglMapBufferRange(GlBufferType.COPY_READ_BUFFER.glEnum, 0, size, access);
		}

		@Override
		public void unmap(int handle) {
			GlBufferType.COPY_READ_BUFFER.bind(handle);
			GL15.glUnmapBuffer(GlBufferType.COPY_READ_BUFFER.glEnum);
		}
	}
}
