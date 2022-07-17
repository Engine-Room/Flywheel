package com.jozufozu.flywheel.core.model;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.Formats;
import com.mojang.blaze3d.vertex.BufferBuilder;

public class WorldModel implements Model {

	private final VertexList reader;
	private final String name;

	public WorldModel(BufferBuilder bufferBuilder, String name) {
		this.reader = Formats.BLOCK.createReader(bufferBuilder);
		this.name = name;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public VertexType getType() {
		return Formats.BLOCK;
	}

	@Override
	public int vertexCount() {
		return reader.getVertexCount();
	}

	@Override
	public VertexList getReader() {
		return reader;
	}
}
