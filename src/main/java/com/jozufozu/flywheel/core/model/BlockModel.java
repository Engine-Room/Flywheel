package com.jozufozu.flywheel.core.model;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.Formats;
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

	public BlockModel(ShadeSeparatedBufferBuilder bufferBuilder, String name) {
		this.name = name;
		reader = Formats.BLOCK.createReader(bufferBuilder);
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
	}
}
