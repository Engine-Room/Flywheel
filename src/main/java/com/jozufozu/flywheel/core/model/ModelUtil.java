package com.jozufozu.flywheel.core.model;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.backend.model.IndexedModel;

public class ModelUtil {
	public static IndexedModel getIndexedModel(IModel blockModel) {
		ByteBuffer vertices = ByteBuffer.allocate(blockModel.size());
		vertices.order(ByteOrder.nativeOrder());

		blockModel.buffer(new VecBuffer(vertices));

		((Buffer) vertices).rewind();

		return new IndexedModel(blockModel.format(), vertices, blockModel.vertexCount(), blockModel.createEBO());
	}
}
