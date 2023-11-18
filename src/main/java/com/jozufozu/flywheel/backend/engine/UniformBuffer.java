package com.jozufozu.flywheel.backend.engine;

import java.util.List;
import java.util.Set;

import org.lwjgl.opengl.GL32;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.api.uniform.ShaderUniforms;
import com.jozufozu.flywheel.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.gl.shader.GlProgram;
import com.jozufozu.flywheel.lib.math.MoreMath;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;

import net.minecraft.util.Mth;

public class UniformBuffer {

	private static final int OFFSET_ALIGNMENT = GL32.glGetInteger(GL32.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT);
	private static final int MAX_SIZE = GL32.glGetInteger(GL32.GL_MAX_UNIFORM_BLOCK_SIZE);
	private static final int MAX_BINDINGS = GL32.glGetInteger(GL32.GL_MAX_UNIFORM_BUFFER_BINDINGS);
	private static final boolean PO2_ALIGNMENT = Mth.isPowerOfTwo(OFFSET_ALIGNMENT);

	private static UniformBuffer instance;
	private final ProviderSet providerSet;

	public static UniformBuffer getInstance() {
		if (instance == null) {
			instance = new UniformBuffer();
		}
		return instance;
	}

	private final GlBuffer buffer;

	private UniformBuffer() {
		buffer = new GlBuffer();
		providerSet = new ProviderSet(ShaderUniforms.REGISTRY.getAll());
	}

	public static void syncAndBind(GlProgram program) {
		getInstance().sync();
		program.bind();
	}

	public void sync() {
		if (providerSet.pollUpdates()) {
			buffer.upload(providerSet.data);
		}

		GL32.glBindBufferRange(GL32.GL_UNIFORM_BUFFER, 0, buffer.handle(), 0, providerSet.data.size());
	}

	// https://stackoverflow.com/questions/3407012/rounding-up-to-the-nearest-multiple-of-a-number
	private static int alignUniformBuffer(int numToRound) {
		if (PO2_ALIGNMENT) {
			return (numToRound + OFFSET_ALIGNMENT - 1) & -OFFSET_ALIGNMENT;
		} else {
			return ((numToRound + OFFSET_ALIGNMENT - 1) / OFFSET_ALIGNMENT) * OFFSET_ALIGNMENT;
		}
	}

	private void delete() {
		providerSet.delete();
		buffer.delete();
	}

	public static void onReloadRenderers(ReloadRenderersEvent event) {
		if (instance != null) {
			instance.delete();
			instance = null;
		}
	}

	private static class LiveProvider {
		private final ShaderUniforms shaderUniforms;
		private final int offset;
		private final int size;
		private ShaderUniforms.Provider provider;

		private LiveProvider(ShaderUniforms shaderUniforms, int offset, int size) {
			this.shaderUniforms = shaderUniforms;
			this.offset = offset;
			this.size = size;
		}

		private void updatePtr(MemoryBlock bufferBase) {
			if (provider != null) {
				provider.delete();
			}
			provider = shaderUniforms.activate(bufferBase.ptr() + offset);
		}

		public boolean maybePoll() {
			return provider != null && provider.poll();
		}
	}

	private static class ProviderSet {
		private final List<LiveProvider> allocatedProviders;

		private final MemoryBlock data;

		private ProviderSet(final Set<ShaderUniforms> providers) {
			var builder = ImmutableList.<LiveProvider>builder();
			int totalBytes = 0;
			for (ShaderUniforms provider : providers) {
				int size = MoreMath.align16(provider.byteSize());

				builder.add(new LiveProvider(provider, totalBytes, size));

				totalBytes += size;
			}

			allocatedProviders = builder.build();

			data = MemoryBlock.mallocTracked(totalBytes);

			for (LiveProvider p : allocatedProviders) {
				p.updatePtr(data);
			}
		}

		public boolean pollUpdates() {
			boolean changed = false;
			for (LiveProvider p : allocatedProviders) {
				changed |= p.maybePoll();
			}
			return changed;
		}

		public void delete() {
			allocatedProviders.forEach(p -> p.provider.delete());
			data.free();
		}
	}
}
