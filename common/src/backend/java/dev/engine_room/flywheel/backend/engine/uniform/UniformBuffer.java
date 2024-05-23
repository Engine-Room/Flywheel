package dev.engine_room.flywheel.backend.engine.uniform;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL32;

import dev.engine_room.flywheel.backend.gl.buffer.GlBuffer;
import dev.engine_room.flywheel.backend.gl.buffer.GlBufferUsage;
import dev.engine_room.flywheel.lib.math.MoreMath;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;

public class UniformBuffer {
	private final int index;
	private final MemoryBlock clientBuffer;
	@Nullable
	private GlBuffer buffer;
	private boolean needsUpload;

	public UniformBuffer(int index, int size) {
		this.index = index;

		// renderdoc complains if the size of the buffer is not 16-byte aligned,
		// though things work fine on my machine if we're short a few bytes
		clientBuffer = MemoryBlock.malloc(MoreMath.align16(size));
		clientBuffer.clear();
	}

	public long ptr() {
		return clientBuffer.ptr();
	}

	public void markDirty() {
		needsUpload = true;
	}

	public void clear() {
		clientBuffer.clear();
		markDirty();
	}

	public void bind() {
		if (buffer == null) {
			buffer = new GlBuffer(GlBufferUsage.DYNAMIC_DRAW);
			needsUpload = true;
		}

		if (needsUpload) {
			buffer.upload(clientBuffer);
			needsUpload = false;
		}

		GL32.glBindBufferRange(GL32.GL_UNIFORM_BUFFER, index, buffer.handle(), 0, clientBuffer.size());
	}

	public void delete() {
		if (buffer != null) {
			buffer.delete();
			buffer = null;
		}
	}
}
