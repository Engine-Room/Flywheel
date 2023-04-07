package com.jozufozu.flywheel.api.struct;

import com.jozufozu.flywheel.api.vertex.MutableVertexList;

import net.minecraft.client.multiplayer.ClientLevel;

public interface StructVertexTransformer<P extends InstancePart> {
	void transform(MutableVertexList vertexList, P struct, ClientLevel level);
}
