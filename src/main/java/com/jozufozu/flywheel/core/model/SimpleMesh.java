package com.jozufozu.flywheel.core.model;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.api.vertex.VertexType;

public class SimpleMesh implements Mesh {
	private final VertexList reader;
	private final VertexType vertexType;
	private final String name;

	public SimpleMesh(VertexList reader, VertexType vertexType, String name) {
		this.reader = reader;
		this.vertexType = vertexType;
		this.name = name;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public VertexType getVertexType() {
		return vertexType;
	}

	@Override
	public VertexList getReader() {
		return reader;
	}

	@Override
	public String toString() {
		return "SimpleMesh{" + "name='" + name + "',vertexType='" + vertexType + "}";
	}
}
