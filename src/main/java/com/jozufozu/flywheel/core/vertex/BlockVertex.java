package com.jozufozu.flywheel.core.vertex;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.core.layout.CommonItems;
import com.jozufozu.flywheel.core.source.FileResolution;

public class BlockVertex implements VertexType {

	public static final BufferLayout FORMAT = BufferLayout.builder()
			.addItems(CommonItems.VEC3,
					CommonItems.RGBA,
					CommonItems.UV,
					CommonItems.LIGHT_SHORT,
					CommonItems.NORMAL)
			.withPadding(1)
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

	@Override
	public FileResolution getLayoutShader() {
		return Components.Files.BLOCK_LAYOUT;
	}
}
