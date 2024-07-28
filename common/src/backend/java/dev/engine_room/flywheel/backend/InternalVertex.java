package dev.engine_room.flywheel.backend;

import java.util.List;

import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.api.layout.FloatRepr;
import dev.engine_room.flywheel.api.layout.Layout;
import dev.engine_room.flywheel.api.layout.LayoutBuilder;
import dev.engine_room.flywheel.backend.gl.array.VertexAttribute;
import dev.engine_room.flywheel.lib.vertex.FullVertexView;
import dev.engine_room.flywheel.lib.vertex.VertexView;
import net.minecraft.resources.ResourceLocation;

public final class InternalVertex {
	public static final Layout LAYOUT = LayoutBuilder.create()
			.vector("position", FloatRepr.FLOAT, 3)
			.vector("color", FloatRepr.NORMALIZED_UNSIGNED_BYTE, 4)
			.vector("tex", FloatRepr.FLOAT, 2)
			.vector("overlay", FloatRepr.SHORT, 2)
			.vector("light", FloatRepr.UNSIGNED_SHORT, 2)
			.vector("normal", FloatRepr.NORMALIZED_BYTE, 3)
			.build();

	public static final List<VertexAttribute> ATTRIBUTES = LayoutAttributes.attributes(LAYOUT);
	public static final int STRIDE = LAYOUT.byteSize();

	public static final ResourceLocation LAYOUT_SHADER = Flywheel.rl("internal/vertex_input.vert");

	private InternalVertex() {
	}

	public static VertexView createVertexView() {
		return new FullVertexView();
	}
}
