package com.jozufozu.flywheel.api.instance;

import com.jozufozu.flywheel.api.vertex.MutableVertexList;

public interface InstanceVertexTransformer<I extends Instance> {
	void transform(MutableVertexList vertexList, I instance);
}
