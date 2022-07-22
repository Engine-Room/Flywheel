package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.List;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.core.model.ModelSupplier;

public class InstancedModel<D extends InstancedPart> {

	private final ModelSupplier model;
	private final StructType<D> type;
	private final GPUInstancer<D> instancer;
	private List<DrawCall> layers;

	public InstancedModel(StructType<D> type, ModelSupplier model) {
		this.model = model;
		this.instancer = new GPUInstancer<>(this, type);
		this.type = type;
	}

	public void init(RenderLists renderLists) {
		instancer.init();

		layers = model.get()
				.entrySet()
				.stream()
				.map(entry -> new DrawCall(instancer, entry.getKey(), entry.getValue()))
				.toList();

		for (DrawCall layer : layers) {
			renderLists.add(new ShaderState(layer.material, layer.getVertexType(), type), layer);
		}
	}

	public GPUInstancer<D> getInstancer() {
		return instancer;
	}

	public ModelSupplier getModel() {
		return model;
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
