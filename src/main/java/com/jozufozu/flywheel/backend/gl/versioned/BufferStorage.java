package com.jozufozu.flywheel.backend.gl.versioned;

import org.lwjgl.opengl.ARBBufferStorage;
import org.lwjgl.opengl.GL44;
import org.lwjgl.opengl.GLCapabilities;

public enum BufferStorage implements GlVersioned {

	GL44CORE {
		@Override
		public boolean supported(GLCapabilities caps) {
			return caps.OpenGL44;
		}

		@Override
		public void bufferStorage(int target, long size, int flags) {
			GL44.glBufferStorage(target, size, flags);
		}
	},
	ARB {
		@Override
		public boolean supported(GLCapabilities caps) {
			return caps.GL_ARB_buffer_storage;
		}

		@Override
		public void bufferStorage(int target, long size, int flags) {
			ARBBufferStorage.glBufferStorage(target, size, flags);
		}
	},
	UNSUPPORTED {
		@Override
		public boolean supported(GLCapabilities caps) {
			return true;
		}

		@Override
		public void bufferStorage(int target, long size, int flags) {
			throw new UnsupportedOperationException();
		}
	};

	public abstract void bufferStorage(int target, long size, int flags);
}
