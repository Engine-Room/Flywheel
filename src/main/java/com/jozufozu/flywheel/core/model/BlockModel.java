package com.jozufozu.flywheel.core.model;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferUsage;
import com.jozufozu.flywheel.backend.gl.buffer.MappedGlBuffer;
import com.jozufozu.flywheel.backend.model.ElementBuffer;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.QuadConverter;
import com.jozufozu.flywheel.util.Pair;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A model of a single block.
 */
public class BlockModel implements Model {
	private final VertexList reader;

	private final String name;

	private final Supplier<ElementBuffer> eboSupplier;

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
			ByteBuffer src = renderedBuffer.indexBuffer();
			ByteBuffer indexBuffer = MemoryTracker.create(src.capacity());
			MemoryUtil.memCopy(src, indexBuffer);
			eboSupplier = () -> {

				MappedGlBuffer vbo = new MappedGlBuffer(GlBufferType.ELEMENT_ARRAY_BUFFER, GlBufferUsage.STATIC_DRAW);

				vbo.upload(indexBuffer);

				return new ElementBuffer(vbo, drawState.indexCount(), drawState.indexType());
			};
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
}
