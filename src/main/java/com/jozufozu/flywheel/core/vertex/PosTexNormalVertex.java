package com.jozufozu.flywheel.core.vertex;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.core.layout.CommonItems;
import com.jozufozu.flywheel.core.source.FileResolution;

public class PosTexNormalVertex implements VertexType {

	public static final BufferLayout FORMAT = BufferLayout.builder()
			.addItems(CommonItems.VEC3, CommonItems.UV, CommonItems.NORMAL)
			.build();

	@Override
	public BufferLayout getLayout() {
		return FORMAT;
	}

	@Override
	public PosTexNormalWriterUnsafe createWriter(ByteBuffer buffer) {
		return new PosTexNormalWriterUnsafe(this, buffer);
	}

	@Override
	public PosTexNormalVertexListUnsafe createReader(ByteBuffer buffer, int vertexCount) {
		return new PosTexNormalVertexListUnsafe(buffer, vertexCount);
	}

	@Override
	public FileResolution getLayoutShader() {
		return Components.Files.POS_TEX_NORMAL_LAYOUT;
	}
}
