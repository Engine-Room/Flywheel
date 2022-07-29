package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.List;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.core.model.Model;

public class InstancedModel<D extends InstancedPart> {

	private final StructType<D> type;
	private final Model model;
	private final GPUInstancer<D> instancer;
	private List<DrawCall> layers;

	public InstancedModel(StructType<D> type, Model model) {
		this.type = type;
		this.model = model;
		this.instancer = new GPUInstancer<>(this, type);
	}

	public void init(RenderLists renderLists) {
		instancer.init();

		layers = model.getMeshes()
				.entrySet()
				.stream()
				.map(entry -> new DrawCall(instancer, entry.getKey(), entry.getValue()))
				.toList();

		for (DrawCall layer : layers) {
			renderLists.add(new ShaderState(layer.getMaterial(), layer.getVertexType(), type), layer);
		}
	}

	public Model getModel() {
		return model;
	}

	public GPUInstancer<D> getInstancer() {
		return instancer;
	}

	public int getVertexCount() {
		return model.getVertexCount() * instancer.glInstanceCount;
	}

	public void delete() {
		if (instancer.vbo == null) return;

		instancer.delete();

		for (var layer : layers) {
			layer.delete();
		}
	}
}
