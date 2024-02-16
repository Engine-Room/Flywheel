package com.jozufozu.flywheel.backend;

import java.util.List;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.layout.FloatRepr;
import com.jozufozu.flywheel.api.layout.Layout;
import com.jozufozu.flywheel.api.layout.LayoutBuilder;
import com.jozufozu.flywheel.api.layout.UnsignedIntegerRepr;
import com.jozufozu.flywheel.api.vertex.VertexView;
import com.jozufozu.flywheel.backend.engine.LayoutAttributes;
import com.jozufozu.flywheel.backend.gl.array.VertexAttribute;
import com.jozufozu.flywheel.lib.vertex.FullVertexView;

import net.minecraft.resources.ResourceLocation;

public final class InternalVertex {
	public static final Layout LAYOUT = LayoutBuilder.create()
			.vector("position", FloatRepr.FLOAT, 3)
			.vector("color", FloatRepr.NORMALIZED_UNSIGNED_BYTE, 4)
			.vector("tex", FloatRepr.FLOAT, 2)
			.vector("overlay_light", UnsignedIntegerRepr.UNSIGNED_SHORT, 4)
			.vector("normal", FloatRepr.NORMALIZED_BYTE, 3)
			.build();

	public static final List<VertexAttribute> ATTRIBUTES = LayoutAttributes.attributes(LAYOUT);
	public static final int ATTRIBUTE_COUNT = ATTRIBUTES.size();
	public static final int STRIDE = LAYOUT.byteSize();

	public static final ResourceLocation LAYOUT_SHADER = Flywheel.rl("internal/vertex_input.vert");

	private InternalVertex() {
	}

	public static VertexView createVertexView() {
		return new FullVertexView();
	}
}
