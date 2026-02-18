package dev.engine_room.flywheel.backend.gl;

import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;

public class GlFence {
	private final long fence;

	public GlFence() {
		fence = GlStateManager._glFenceSync(GlConst.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
	}

	public boolean isSignaled() {
		int result;
		try (var memoryStack = MemoryStack.stackPush()) {
			long checkPtr = memoryStack.ncalloc(Integer.BYTES, 0, Integer.BYTES);
			GL32.nglGetSynciv(fence, GL32.GL_SYNC_STATUS, 1, MemoryUtil.NULL, checkPtr);

			result = MemoryUtil.memGetInt(checkPtr);
		}
		return result == GL32.GL_SIGNALED;
	}

	public void delete() {
		GlStateManager._glDeleteSync(fence);
	}
}
