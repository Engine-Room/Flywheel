package com.jozufozu.flywheel.backend.model;

public interface DirectBufferBuilder {

	DirectVertexConsumer intoDirectConsumer(int vertexCount);
}
