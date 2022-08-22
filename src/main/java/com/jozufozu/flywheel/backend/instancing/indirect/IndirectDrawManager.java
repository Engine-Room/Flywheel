package com.jozufozu.flywheel.backend.instancing.indirect;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.model.Mesh;
import com.jozufozu.flywheel.util.Pair;

public class IndirectDrawManager {

	public final Map<Pair<StructType<?>, VertexType>, IndirectCullingGroup<?>> lists = new HashMap<>();

	@SuppressWarnings("unchecked")
	public <D extends InstancedPart> void add(IndirectInstancer<D> instancer, Material material, Mesh mesh) {
		var indirectList = (IndirectCullingGroup<D>) lists.computeIfAbsent(Pair.of(instancer.type, mesh.getVertexType()), p -> new IndirectCullingGroup<>(p.first(), p.second()));

		indirectList.drawSet.add(instancer, material, indirectList.meshPool.alloc(mesh));
	}
}
