package com.jozufozu.flywheel.backend.vertex;

import com.jozufozu.flywheel.api.vertex.ReusableVertexList;
import com.jozufozu.flywheel.api.vertex.VertexListProvider;
import com.mojang.blaze3d.vertex.VertexFormat;

public class InferredVertexListProviderImpl implements VertexListProvider {
	private final VertexFormat format;
	private final InferredVertexFormatInfo formatInfo;

	public InferredVertexListProviderImpl(VertexFormat format) {
		this.format = format;
		formatInfo = new InferredVertexFormatInfo(format);
	}

	@Override
	public ReusableVertexList createVertexList() {
		return new InferredVertexListImpl(formatInfo);
	}
}
