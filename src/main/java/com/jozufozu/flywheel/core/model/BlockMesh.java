package com.jozufozu.flywheel.core.model;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.vertex.Formats;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A model of a single block.
 */
public class BlockMesh implements Mesh {
	private static final PoseStack IDENTITY = new PoseStack();

	private final VertexList reader;

	private final String name;

	public BlockMesh(BlockState state) {
		this(Minecraft.getInstance()
				.getBlockRenderer()
				.getBlockModel(state), state);
	}

	public BlockMesh(BakedModel model, BlockState referenceState) {
		this(model, referenceState, IDENTITY);
	}

	public BlockMesh(PartialModel model) {
		this(model, IDENTITY);
	}

	public BlockMesh(PartialModel model, PoseStack ms) {
		this(ModelUtil.bakedModel(model.get())
				.withPoseStack(ms), model.getName());
	}

	public BlockMesh(BakedModel model, BlockState referenceState, PoseStack ms) {
		this(ModelUtil.bakedModel(model)
				.withReferenceState(referenceState)
				.withPoseStack(ms), referenceState.toString());
	}

	public BlockMesh(Bufferable builder, String name) {
		this(Formats.BLOCK.createReader(builder.build()), name);
	}

	public BlockMesh(VertexList reader, String name) {
		this.reader = reader;
		this.name = name;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public VertexList getReader() {
		return reader;
	}

	@Override
	public String toString() {
		return "BlockMesh{" + "name='" + name + "',type='" + reader.getVertexType() + "}";
	}
}
