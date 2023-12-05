package com.jozufozu.flywheel.lib.vertex;

import com.jozufozu.flywheel.api.layout.BufferLayout;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.lib.layout.CommonItems;

public class FullVertex implements VertexType {
	public static final BufferLayout FORMAT = BufferLayout.builder()
			.addItem(CommonItems.VEC3, "position")
			.addItem(CommonItems.UNORM_4x8, "color")
			.addItem(CommonItems.VEC2, "tex")
			.addItem(CommonItems.LIGHT_COORD, "overlay")
			.addItem(CommonItems.LIGHT_COORD, "light")
			.addItem(CommonItems.NORM_3x8, "normal")
			.withPadding(1)
			.build();

	@Override
	public int getStride() {
		return FORMAT.getStride();
	}

	@Override
	public FullVertexList createVertexList() {
		return new FullVertexList();
	}
}
