package com.jozufozu.flywheel.core.vertex;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.core.layout.CommonItems;

public class PosTexNormalVertex implements VertexType {

	public static final BufferLayout FORMAT = BufferLayout.builder()
			.addItems(CommonItems.VEC3, CommonItems.UV, CommonItems.NORMAL)
			.build();

	@Override
	public BufferLayout getLayout() {
		return FORMAT;
	}

	@Override
	public PosTexNormalWriterUnsafe createWriter(ByteBuffer buffer) {
		return new PosTexNormalWriterUnsafe(this, buffer);
	}

	@Override
	public PosTexNormalVertexListUnsafe createReader(ByteBuffer buffer, int vertexCount) {
		return new PosTexNormalVertexListUnsafe(buffer, vertexCount);
	}

	@Override
	public String getShaderHeader() {
		return """
layout (location = 0) in vec3 _flw_v_pos;
layout (location = 1) in vec2 _flw_v_texCoords;
layout (location = 2) in vec3 _flw_v_normal;

Vertex FLWCreateVertex() {
	Vertex v;
	v.pos = _flw_v_pos;
	v.color = vec4(1.);
	v.texCoords = _flw_v_texCoords;
	v.light = vec2(0.);
	v.normal = _flw_v_normal;
	return v;
}
				""";
	}
}
