package com.jozufozu.flywheel.backend.engine.uniform;

import org.lwjgl.opengl.GL32;

import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferUsage;
import com.jozufozu.flywheel.lib.math.MoreMath;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;

public class UniformBuffer<T extends UniformProvider> {
	private final int index;
	private final GlBuffer buffer;
	public final T provider;
	private final MemoryBlock data;

	public UniformBuffer(int index, T provider) {
		this.index = index;
		this.buffer = new GlBuffer(GlBufferUsage.DYNAMIC_DRAW);
		this.provider = provider;

		// renderdoc complains if the size of the buffer is not 16-byte aligned,
		// though things work fine on my machine if we're short a few bytes
		var actualBytes = MoreMath.align16(provider.byteSize());
		this.data = MemoryBlock.mallocTracked(actualBytes);
		this.data.clear();
	}

	public void update() {
		provider.write(data.ptr());
		buffer.upload(data);
	}

	public void bind() {
		GL32.glBindBufferRange(GL32.GL_UNIFORM_BUFFER, index, buffer.handle(), 0, data.size());
	}

	public void delete() {
		data.free();
		buffer.delete();
	}
}
