package com.jozufozu.flywheel.lib.vertex;

import com.jozufozu.flywheel.api.layout.BufferLayout;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.lib.layout.CommonItems;

public class BlockVertex implements VertexType {
	public static final BufferLayout FORMAT = BufferLayout.builder()
			.addItem(CommonItems.VEC3, "position")
			.addItem(CommonItems.UNORM_4x8, "color")
			.addItem(CommonItems.VEC2, "tex")
			.addItem(CommonItems.LIGHT_COORD, "light")
			.addItem(CommonItems.NORM_3x8, "normal")
			.withPadding(1)
			.build();

	@Override
	public BufferLayout getLayout() {
		return FORMAT;
	}

	@Override
	public BlockVertexList createVertexList() {
		return new BlockVertexList();
	}
}
