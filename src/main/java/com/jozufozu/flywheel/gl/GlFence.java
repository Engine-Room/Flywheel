package com.jozufozu.flywheel.gl;

import static org.lwjgl.opengl.GL32.GL_SIGNALED;
import static org.lwjgl.opengl.GL32.GL_SYNC_FLUSH_COMMANDS_BIT;
import static org.lwjgl.opengl.GL32.GL_SYNC_GPU_COMMANDS_COMPLETE;
import static org.lwjgl.opengl.GL32.GL_SYNC_STATUS;
import static org.lwjgl.opengl.GL32.GL_TIMEOUT_IGNORED;
import static org.lwjgl.opengl.GL32.glClientWaitSync;
import static org.lwjgl.opengl.GL32.glDeleteSync;
import static org.lwjgl.opengl.GL32.glFenceSync;

import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

// https://github.com/CaffeineMC/sodium-fabric/blob/da17fc8d0cb1a4e82fe6956ac4f07a63d32eca5a/components/gfx-opengl/src/main/java/net/caffeinemc/gfx/opengl/sync/GlFence.java
public class GlFence {

	private long fence;

	public void post() {
		clear();

		fence = glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
	}

	public void clear() {
		if (fence != 0) {
			glDeleteSync(fence);
			fence = 0;
		}
	}

	public boolean poll() {
		if (fence != 0) {
			poll0();
		}

		return fence == 0;
	}

	private void poll0() {
		int result;
		try (var memoryStack = MemoryStack.stackPush()) {
			long checkPtr = memoryStack.ncalloc(Integer.BYTES, 0, Integer.BYTES);
			GL32.nglGetSynciv(fence, GL_SYNC_STATUS, 1, MemoryUtil.NULL, checkPtr);

			result = MemoryUtil.memGetInt(checkPtr);
		}

		if (result == GL_SIGNALED) {
			glDeleteSync(fence);
			fence = 0;
		}
	}

	public void waitSync() {
		if (poll()) {
			return;
		}

		glClientWaitSync(fence, GL_SYNC_FLUSH_COMMANDS_BIT, GL_TIMEOUT_IGNORED);

		glDeleteSync(fence);

		fence = 0;
	}
}
