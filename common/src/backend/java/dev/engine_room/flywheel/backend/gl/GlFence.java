package dev.engine_room.flywheel.backend.gl;

import static org.lwjgl.opengl.GL32.GL_SIGNALED;
import static org.lwjgl.opengl.GL32.GL_SYNC_GPU_COMMANDS_COMPLETE;
import static org.lwjgl.opengl.GL32.GL_SYNC_STATUS;
import static org.lwjgl.opengl.GL32.glDeleteSync;
import static org.lwjgl.opengl.GL32.glFenceSync;
import static org.lwjgl.opengl.GL32.nglGetSynciv;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class GlFence {
	private final long fence;

	public GlFence() {
		fence = glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
	}

	public boolean isSignaled() {
		int result;
		try (var memoryStack = MemoryStack.stackPush()) {
			long checkPtr = memoryStack.ncalloc(Integer.BYTES, 0, Integer.BYTES);
			nglGetSynciv(fence, GL_SYNC_STATUS, 1, MemoryUtil.NULL, checkPtr);

			result = MemoryUtil.memGetInt(checkPtr);
		}
		return result == GL_SIGNALED;
	}

	public void delete() {
		glDeleteSync(fence);
	}
}
