package com.jozufozu.flywheel.backend;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.layout.BufferLayout;
import com.jozufozu.flywheel.api.vertex.VertexView;
import com.jozufozu.flywheel.lib.layout.CommonItems;
import com.jozufozu.flywheel.lib.vertex.FullVertexView;

import net.minecraft.resources.ResourceLocation;

public final class InternalVertex {
	public static final BufferLayout LAYOUT = BufferLayout.builder()
			.addItem(CommonItems.VEC3, "position")
			.addItem(CommonItems.UNORM_4x8, "color")
			.addItem(CommonItems.VEC2, "tex")
			.addItem(CommonItems.LIGHT_COORD, "overlay")
			.addItem(CommonItems.LIGHT_COORD, "light")
			.addItem(CommonItems.NORM_3x8, "normal")
			.withPadding(1)
			.build();

	public static final ResourceLocation LAYOUT_SHADER = Flywheel.rl("internal/vertex_input.vert");

	private InternalVertex() {
	}

	public static VertexView createVertexView() {
		return new FullVertexView();
	}
}
