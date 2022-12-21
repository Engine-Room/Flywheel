package com.jozufozu.flywheel.core.uniform;

import java.util.BitSet;
import java.util.Collection;
import java.util.List;

import org.lwjgl.opengl.GL32;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.uniform.UniformProvider;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.backend.memory.MemoryBlock;
import com.jozufozu.flywheel.core.ComponentRegistry;
import com.jozufozu.flywheel.util.RenderMath;

public class UniformBuffer {

	private static final int OFFSET_ALIGNMENT = GL32.glGetInteger(GL32.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT);
	private static final int MAX_SIZE = GL32.glGetInteger(GL32.GL_MAX_UNIFORM_BLOCK_SIZE);
	private static final int MAX_BINDINGS = GL32.glGetInteger(GL32.GL_MAX_UNIFORM_BUFFER_BINDINGS);
	private static final boolean PO2_ALIGNMENT = RenderMath.isPowerOf2(OFFSET_ALIGNMENT);

	private static UniformBuffer instance;
	private final AllocatedProviderSet providerSet;

	public static UniformBuffer getInstance() {
		if (instance == null) {
			instance = new UniformBuffer();
		}
		return instance;
	}

	private final GlBuffer buffer;

	private UniformBuffer() {
		buffer = new GlBuffer(GlBufferType.UNIFORM_BUFFER);
		providerSet = new AllocatedProviderSet(ComponentRegistry.getAllUniformProviders());
	}

	public void sync() {
		providerSet.sync();

		buffer.upload(providerSet.data);

		int handle = buffer.handle();
		for (Allocated p : providerSet.allocatedProviders) {
			GL32.glBindBufferRange(GL32.GL_UNIFORM_BUFFER, p.index, handle, p.offset, p.size);
		}
	}

	// https://stackoverflow.com/questions/3407012/rounding-up-to-the-nearest-multiple-of-a-number
	private static int alignUniformBuffer(int numToRound) {
		if (PO2_ALIGNMENT) {
			return (numToRound + OFFSET_ALIGNMENT - 1) & -OFFSET_ALIGNMENT;
		} else {
			return ((numToRound + OFFSET_ALIGNMENT - 1) / OFFSET_ALIGNMENT) * OFFSET_ALIGNMENT;
		}
	}

	private static int align16(int numToRound) {
		return (numToRound + 16 - 1) & -16;
	}

	private static class Allocated {
		private final UniformProvider provider;
		private final int offset;
		private final int size;
		private final int index;
		private UniformProvider.ActiveUniformProvider activeProvider;

		private Allocated(UniformProvider provider, int offset, int size, int index) {
			this.provider = provider;
			this.offset = offset;
			this.size = size;
			this.index = index;
		}

		private void updatePtr(MemoryBlock bufferBase) {
			if (activeProvider != null) {
				activeProvider.delete();
			}
			activeProvider = provider.activate(bufferBase.ptr() + offset);
		}

		public int offset() {
			return offset;
		}

		public int size() {
			return size;
		}

		public int index() {
			return index;
		}

		public boolean maybePoll() {
			return activeProvider != null && activeProvider.poll();
		}
	}

	private static class AllocatedProviderSet {
		private final List<Allocated> allocatedProviders;

		private final MemoryBlock data;

		private final BitSet changedBytes;

		private AllocatedProviderSet(final Collection<UniformProvider> providers) {
			var builder = ImmutableList.<Allocated>builder();
			int totalBytes = 0;
			int index = 0;
			for (UniformProvider provider : providers) {
				int size = align16(provider.byteSize());

				builder.add(new Allocated(provider, totalBytes, size, index));

				totalBytes = alignUniformBuffer(totalBytes + size);
				index++;
			}

			allocatedProviders = builder.build();

			data = MemoryBlock.mallocTracked(totalBytes);
			changedBytes = new BitSet(totalBytes);

			for (Allocated p : allocatedProviders) {
				p.updatePtr(data);
			}
		}

		public void sync() {
			for (Allocated p : allocatedProviders) {
				if (p.maybePoll()) {
					changedBytes.set(p.offset(), p.offset() + p.size());
				}
			}
		}
	}
}
