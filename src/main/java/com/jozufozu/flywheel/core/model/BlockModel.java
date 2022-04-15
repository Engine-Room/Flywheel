package com.jozufozu.flywheel.core.model;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A model of a single block.
 */
public class BlockModel implements Model {
	private static final PoseStack IDENTITY = new PoseStack();

	private final VertexList reader;

	private final String name;

	public BlockModel(BlockState state) {
		this(Minecraft.getInstance()
				.getBlockRenderer()
				.getBlockModel(state), state);
	}

	public BlockModel(BakedModel model, BlockState referenceState) {
		this(model, referenceState, IDENTITY);
	}

	public BlockModel(PartialModel model) {
		this(model, IDENTITY);
	}

	public BlockModel(PartialModel model, PoseStack ms) {
		this(Formats.BLOCK.createReader(ModelUtil.getBufferBuilder(model.get(), Blocks.AIR.defaultBlockState(), ms)), model.getLocation().toString());
	}

	public BlockModel(BakedModel model, BlockState referenceState, PoseStack ms) {
		this(Formats.BLOCK.createReader(ModelUtil.getBufferBuilder(model, referenceState, ms)), referenceState.toString());
	}

	public BlockModel(VertexList reader, String name) {
		this.reader = reader;
		this.name = name;
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
}
