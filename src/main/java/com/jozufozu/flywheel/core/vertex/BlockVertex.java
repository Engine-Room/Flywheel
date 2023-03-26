package com.jozufozu.flywheel.core.vertex;

import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.core.layout.CommonItems;

import net.minecraft.resources.ResourceLocation;

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
	public ResourceLocation layoutShader() {
		return Components.Files.BLOCK_LAYOUT;
	}

	@Override
	public BlockVertexList createVertexList() {
		return new BlockVertexList();
	}
}
