package com.jozufozu.flywheel.core.vertex;

import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.core.layout.CommonItems;

import net.minecraft.resources.ResourceLocation;

public class PosTexNormalVertex implements VertexType {
	public static final BufferLayout FORMAT = BufferLayout.builder()
			.addItem(CommonItems.VEC3, "position")
			.addItem(CommonItems.VEC2, "tex")
			.addItem(CommonItems.NORM_3x8, "normal")
			.build();

	@Override
	public BufferLayout getLayout() {
		return FORMAT;
	}

	@Override
	public ResourceLocation layoutShader() {
		return Components.Files.POS_TEX_NORMAL_LAYOUT;
	}

	@Override
	public PosTexNormalVertexList createVertexList() {
		return new PosTexNormalVertexList();
	}
}
