package com.jozufozu.flywheel.api.material;

import com.jozufozu.flywheel.api.vertex.MutableVertexList;

import net.minecraft.client.multiplayer.ClientLevel;

public interface MaterialVertexTransformer {
	void transform(MutableVertexList vertexList, ClientLevel level);
}
