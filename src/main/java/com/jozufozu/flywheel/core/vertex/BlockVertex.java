package com.jozufozu.flywheel.core.vertex;

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
	public FileResolution getLayoutShader() {
		return Components.Files.BLOCK_LAYOUT;
	}

	@Override
	public BlockVertexList createVertexList() {
		return new BlockVertexList();
	}
}
