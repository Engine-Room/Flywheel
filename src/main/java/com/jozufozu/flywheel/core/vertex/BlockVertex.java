package com.jozufozu.flywheel.core.vertex;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.layout.CommonItems;
import com.jozufozu.flywheel.fabric.helper.BufferBuilderHelper;
import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.datafixers.util.Pair;

public class BlockVertex implements VertexType {

	public static final BufferLayout FORMAT = BufferLayout.builder()
			.addItems(CommonItems.VEC3,
					CommonItems.RGBA,
					CommonItems.UV,
					CommonItems.LIGHT_SHORT,
					CommonItems.NORMAL,
					CommonItems.PADDING_BYTE)
			.build();

	@Override
	public BufferLayout getLayout() {
		return FORMAT;
	}

	@Override
	public BlockWriterUnsafe createWriter(ByteBuffer buffer) {
		return new BlockWriterUnsafe(this, buffer);
	}

	@Override
	public BlockVertexListUnsafe createReader(ByteBuffer buffer, int vertexCount) {
		return new BlockVertexListUnsafe(buffer, vertexCount);
	}

	public VertexList createReader(BufferBuilder bufferBuilder) {
		// TODO: try to avoid virtual model rendering
		Pair<BufferBuilder.DrawState, ByteBuffer> pair = bufferBuilder.popNextBuffer();
		BufferBuilder.DrawState drawState = pair.getFirst();

		if (drawState.format() != DefaultVertexFormat.BLOCK) {
			throw new RuntimeException("Cannot use BufferBuilder with " + drawState.format());
		}

		ByteBuffer buffer = pair.getSecond();
		BufferBuilderHelper.fixByteOrder(bufferBuilder, buffer);
		return new BlockVertexListUnsafe(buffer, drawState.vertexCount());
	}
}
