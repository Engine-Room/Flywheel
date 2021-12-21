package com.jozufozu.flywheel.core.model;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.util.BufferBuilderReader;
import com.jozufozu.flywheel.util.ModelReader;
import com.jozufozu.flywheel.util.RenderMath;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

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
		reader = new BufferBuilderReader(ModelUtil.getBufferBuilder(model, referenceState, ms));
		name = referenceState.toString();
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public VertexFormat format() {
		return Formats.UNLIT_MODEL;
	}

	@Override
	public int vertexCount() {
		return reader.getVertexCount();
	}

	@Override
	public void buffer(VertexConsumer buffer) {

		int vertexCount = vertexCount();

		for (int i = 0; i < vertexCount; i++) {
			buffer.vertex(reader.getX(i), reader.getY(i), reader.getZ(i));

			buffer.normal(reader.getNX(i), reader.getNY(i), reader.getNZ(i));

			buffer.uv(reader.getU(i), reader.getV(i));

			buffer.endVertex();
		}
	}
}
