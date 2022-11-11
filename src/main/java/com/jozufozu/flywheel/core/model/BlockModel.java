package com.jozufozu.flywheel.core.model;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferUsage;
import com.jozufozu.flywheel.backend.model.ElementBuffer;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.QuadConverter;
import com.jozufozu.flywheel.util.Pair;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat.IndexType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A model of a single block.
 */
public class BlockModel implements Model {

	private final VertexList reader;
	private final String name;
	private final EBOSupplier eboSupplier;

	public BlockModel(BlockState state) {
		this(Minecraft.getInstance()
				.getBlockRenderer()
				.getBlockModel(state), state);
	}

	public BlockModel(BakedModel model, BlockState referenceState) {
		this(new BakedModelBuilder(model).withReferenceState(referenceState), referenceState.toString());
	}

	public BlockModel(BakedModel model, BlockState referenceState, PoseStack ms) {
		this(new BakedModelBuilder(model).withReferenceState(referenceState)
				.withPoseStack(ms), referenceState.toString());
	}

	public BlockModel(Bufferable bufferable, String name) {
		this(bufferable.build(), name);
	}

	public BlockModel(Pair<RenderedBuffer, Integer> pair, String name) {
		this.name = name;

		RenderedBuffer renderedBuffer = pair.first();
		BufferBuilder.DrawState drawState = renderedBuffer.drawState();
		reader = Formats.BLOCK.createReader(renderedBuffer, pair.second());

		if (!drawState.sequentialIndex()) {
			eboSupplier = new BufferEBOSupplier(renderedBuffer.indexBuffer(), drawState.indexCount(), drawState.indexType());
		} else {
			eboSupplier = () -> QuadConverter.getInstance()
					.quads2Tris(vertexCount() / 4);
		}
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public int vertexCount() {
		return reader.getVertexCount();
	}

	@Override
	public VertexList getReader() {
		return reader;
	}

	@Override
	public ElementBuffer createEBO() {
		return eboSupplier.get();
	}

	@Override
	public VertexType getType() {
		return Formats.BLOCK;
	}

	@Override
	public void delete() {
		if (reader instanceof AutoCloseable closeable) {
			try {
				closeable.close();
			} catch (Exception e) {
				//
			}
		}
		eboSupplier.delete();
	}

	private interface EBOSupplier extends Supplier<ElementBuffer> {
		default void delete() {
		}
	}

	private static class BufferEBOSupplier implements EBOSupplier {
		private final ByteBuffer indexBuffer;
		private final int indexCount;
		private final IndexType indexType;

		private int eboName = -1;
		private ElementBuffer ebo;

		public BufferEBOSupplier(ByteBuffer indexBufferSrc, int indexCount, IndexType indexType) {
			indexBuffer = MemoryTracker.create(indexBufferSrc.capacity());
			MemoryUtil.memCopy(indexBufferSrc, indexBuffer);
			this.indexCount = indexCount;
			this.indexType = indexType;
		}

		@Override
		public ElementBuffer get() {
			if (eboName == -1) {
				eboName = createEBO();
				ebo = new ElementBuffer(eboName, indexCount, indexType);
				MemoryUtil.memFree(indexBuffer);
			}

			return ebo;
		}

		private int createEBO() {
			int vbo = GL32.glGenBuffers();

			// XXX ARRAY_BUFFER is bound and restored
			var bufferType = GlBufferType.ARRAY_BUFFER;
			var oldBuffer = bufferType.getBoundBuffer();
			bufferType.bind(vbo);
			GL15.glBufferData(bufferType.glEnum, indexBuffer, GlBufferUsage.STATIC_DRAW.glEnum);
			bufferType.bind(oldBuffer);
			return vbo;
		}

		@Override
		public void delete() {
			GL32.glDeleteBuffers(eboName);
		}
	}
}
