package com.jozufozu.flywheel.core.model;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.Formats;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
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

	public BlockModel(ByteBuffer vertexBuffer, BufferBuilder.DrawState drawState, int unshadedStartVertex, String name) {
		if (drawState.format() != DefaultVertexFormat.BLOCK) {
			throw new RuntimeException("Cannot use buffered data with non-block format '" + drawState.format() + "'");
		}

		reader = Formats.BLOCK.createReader(vertexBuffer, drawState.vertexCount(), unshadedStartVertex);

		this.name = name;
	}

	public BlockModel(ByteBuffer vertexBuffer, BufferBuilder.DrawState drawState, String name) {
		if (drawState.format() != DefaultVertexFormat.BLOCK) {
			throw new RuntimeException("Cannot use buffered data with non-block format '" + drawState.format() + "'");
		}

		reader = Formats.BLOCK.createReader(vertexBuffer, drawState.vertexCount());

		this.name = name;
	}

	public BlockModel(ShadeSeparatedBufferedData data, String name) {
		this(data.vertexBuffer(), data.drawState(), data.unshadedStartVertex(), name);
	}

	public static BlockModel of(Bufferable bufferable, String name) {
		ShadeSeparatedBufferedData data = bufferable.build();
		BlockModel model = new BlockModel(data, name);
		data.release();
		return model;
	}

	public static BlockModel of(BakedModel model, BlockState referenceState) {
		ShadeSeparatedBufferedData data = ModelUtil.getBufferedData(model, referenceState);
		BlockModel blockModel = new BlockModel(data, referenceState.toString());
		data.release();
		return blockModel;
	}

	public static BlockModel of(BlockState state) {
		return of(Minecraft.getInstance()
				.getBlockRenderer()
				.getBlockModel(state), state);
	}

	public static BlockModel of(BakedModel model, BlockState referenceState, PoseStack ms) {
		ShadeSeparatedBufferedData data = ModelUtil.getBufferedData(model, referenceState, ms);
		BlockModel blockModel = new BlockModel(data, referenceState.toString());
		data.release();
		return blockModel;
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
		reader.delete();
	}
}
