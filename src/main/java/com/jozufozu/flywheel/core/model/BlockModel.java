package com.jozufozu.flywheel.core.model;

import com.jozufozu.flywheel.util.UnsafeBlockFormatReader;
import com.jozufozu.flywheel.util.ModelReader;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A model of a single block.
 */
public class BlockModel implements Model {
	private static final PoseStack IDENTITY = new PoseStack();

	private final ModelReader reader;

	private final String name;

	public BlockModel(BlockState state) {
		this(Minecraft.getInstance()
				.getBlockRenderer()
				.getBlockModel(state), state);
	}

	public BlockModel(BakedModel model, BlockState referenceState) {
		this(model, referenceState, IDENTITY);
	}

	public BlockModel(BakedModel model, BlockState referenceState, PoseStack ms) {
		reader = new UnsafeBlockFormatReader(ModelUtil.getBufferBuilder(model, referenceState, ms));
		name = referenceState.toString();
	}

	@Override
	public void configure(ModelTransformer.Context ctx) {
		ctx.inputHasDiffuse = true;
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
	public ModelReader getReader() {
		return reader;
	}
}
