package dev.engine_room.flywheel.impl.vertex;

import com.mojang.blaze3d.vertex.VertexFormat;

import dev.engine_room.flywheel.api.vertex.VertexView;
import dev.engine_room.flywheel.api.vertex.VertexViewProvider;

public class InferredVertexViewProvider implements VertexViewProvider {
	private final InferredVertexFormatInfo formatInfo;

	public InferredVertexViewProvider(VertexFormat format) {
		formatInfo = new InferredVertexFormatInfo(format);
	}

	@Override
	public VertexView createVertexView() {
		return new InferredVertexView(formatInfo);
	}
}
