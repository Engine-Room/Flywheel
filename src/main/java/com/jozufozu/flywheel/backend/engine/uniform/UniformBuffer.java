package com.jozufozu.flywheel.backend.engine.uniform;

import org.lwjgl.opengl.GL32;

import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferUsage;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;

import net.minecraft.util.Mth;

public class UniformBuffer<T extends UniformProvider> {
	private static final int OFFSET_ALIGNMENT = GL32.glGetInteger(GL32.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT);
	private static final boolean PO2_ALIGNMENT = Mth.isPowerOfTwo(OFFSET_ALIGNMENT);

	private final int index;
	private final GlBuffer buffer;
	public final T provider;
	private final MemoryBlock data;

	public UniformBuffer(int index, T provider) {
		this.index = index;
		this.buffer = new GlBuffer(GlBufferUsage.DYNAMIC_DRAW);
		this.provider = provider;

		// renderdoc complains if the size of the buffer does not have the offset alignment
		var actualBytes = align(provider.byteSize());
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

	// https://stackoverflow.com/questions/3407012/rounding-up-to-the-nearest-multiple-of-a-number
	private static int align(int numToRound) {
		if (PO2_ALIGNMENT) {
			return (numToRound + OFFSET_ALIGNMENT - 1) & -OFFSET_ALIGNMENT;
		} else {
			return ((numToRound + OFFSET_ALIGNMENT - 1) / OFFSET_ALIGNMENT) * OFFSET_ALIGNMENT;
		}
	}
}
