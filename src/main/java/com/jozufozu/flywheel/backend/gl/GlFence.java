package com.jozufozu.flywheel.backend.gl;

import static org.lwjgl.opengl.GL32.GL_ALREADY_SIGNALED;
import static org.lwjgl.opengl.GL32.GL_CONDITION_SATISFIED;
import static org.lwjgl.opengl.GL32.GL_SYNC_FLUSH_COMMANDS_BIT;
import static org.lwjgl.opengl.GL32.GL_SYNC_GPU_COMMANDS_COMPLETE;
import static org.lwjgl.opengl.GL32.GL_UNSIGNALED;
import static org.lwjgl.opengl.GL32.glClientWaitSync;
import static org.lwjgl.opengl.GL32.glDeleteSync;
import static org.lwjgl.opengl.GL32.glFenceSync;

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

	public void waitSync() {
		if (fence != 0) {
			int waitReturn = GL_UNSIGNALED;
			while (waitReturn != GL_ALREADY_SIGNALED && waitReturn != GL_CONDITION_SATISFIED) {
				waitReturn = glClientWaitSync(fence, GL_SYNC_FLUSH_COMMANDS_BIT, 1);
			}

			glDeleteSync(fence);
		}

		fence = 0;
	}
}
