package com.jozufozu.flywheel.backend.instancing.indirect;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.core.model.Model;

public class IndirectModel<D extends InstancedPart> {

	private final Model model;
	private final IndirectInstancer<D> instancer;

	public IndirectModel(StructType<D> type, Model model) {
		this.model = model;
		this.instancer = new IndirectInstancer<>(this, type);
	}

	public void init(IndirectDrawManager indirectDrawManager) {
		var materialMeshMap = this.model.getMeshes();
		for (var entry : materialMeshMap.entrySet()) {
			var material = entry.getKey();
			var mesh = entry.getValue();
			indirectDrawManager.add(instancer, material, mesh);

			return; // TODO: support multiple meshes per model
		}
	}

	public IndirectInstancer<D> getInstancer() {
		return instancer;
	}

	public Model getModel() {
		return model;
	}

	public int getVertexCount() {
		return model.getVertexCount() * instancer.instanceCount;
	}

	public void delete() {

	}
}
