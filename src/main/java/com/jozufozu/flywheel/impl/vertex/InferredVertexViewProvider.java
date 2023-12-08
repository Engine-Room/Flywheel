package com.jozufozu.flywheel.impl.vertex;

import com.jozufozu.flywheel.api.vertex.VertexView;
import com.jozufozu.flywheel.api.vertex.VertexViewProvider;
import com.mojang.blaze3d.vertex.VertexFormat;

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
