package com.jozufozu.flywheel.backend.instancing.indirect;

import java.util.List;
import java.util.Map;

import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.core.model.Mesh;
import com.jozufozu.flywheel.core.model.Model;

public class InstancedModel<D extends InstancedPart> {

	private final Model model;
	private final StructType<D> type;
	private final IndirectInstancer<D> instancer;

	public InstancedModel(StructType<D> type, Model model) {
		this.model = model;
		this.instancer = new IndirectInstancer<>(this, type);
		this.type = type;
	}

	public void init(RenderLists renderLists) {
		var materialMeshMap = this.model.getMeshes();
		for (var entry : materialMeshMap.entrySet()) {
			var material = entry.getKey();
			var mesh = entry.getValue();
			renderLists.add(material.getRenderStage(), type, mesh, instancer);

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
		return model.getVertexCount() * instancer.maxInstanceCount;
	}

	public void delete() {

	}
}
