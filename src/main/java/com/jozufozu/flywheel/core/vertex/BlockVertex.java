package com.jozufozu.flywheel.core.vertex;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.core.layout.CommonItems;

public class BlockVertex implements VertexType {

	public static final BufferLayout FORMAT = BufferLayout.builder()
			.addItems(CommonItems.VEC3,
					CommonItems.RGBA,
					CommonItems.UV,
					CommonItems.LIGHT_SHORT,
					CommonItems.NORMAL,
					CommonItems.PADDING_BYTE)
			.build();

	@Override
	public BufferLayout getLayout() {
		return FORMAT;
	}

	@Override
	public BlockWriterUnsafe createWriter(ByteBuffer buffer) {
		return new BlockWriterUnsafe(this, buffer);
	}

	@Override
	public BlockVertexListUnsafe createReader(ByteBuffer buffer, int vertexCount) {
		return new BlockVertexListUnsafe(buffer, vertexCount);
	}

	public BlockVertexListUnsafe.Shaded createReader(ByteBuffer buffer, int vertexCount, int unshadedStartVertex) {
		return new BlockVertexListUnsafe.Shaded(buffer, vertexCount, unshadedStartVertex);
	}

	@Override
	public String getShaderHeader() {
		return """
layout (location = 0) in vec3 _flw_v_pos;
layout (location = 1) in vec4 _flw_v_color;
layout (location = 2) in vec2 _flw_v_texCoords;
layout (location = 3) in vec2 _flw_v_light;
layout (location = 4) in vec3 _flw_v_normal;

Vertex FLWCreateVertex() {
	Vertex v;
	v.pos = _flw_v_pos;
	v.color = _flw_v_color;
	v.texCoords = _flw_v_texCoords;
	v.light = _flw_v_light;
	v.normal = _flw_v_normal;
	return v;
}
				""";
	}
}
