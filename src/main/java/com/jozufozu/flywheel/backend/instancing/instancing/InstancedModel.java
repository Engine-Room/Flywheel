package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.backend.gl.GlVertexArray;
import com.jozufozu.flywheel.backend.model.MeshPool;
import com.jozufozu.flywheel.core.model.Mesh;
import com.jozufozu.flywheel.core.model.ModelSupplier;
import com.jozufozu.flywheel.util.Pair;

public class InstancedModel<D extends InstanceData> {

	final GPUInstancer<D> instancer;
	final ModelSupplier model;
	private Map<Material, Layer> layers;

	public InstancedModel(GPUInstancer<D> instancer, ModelSupplier model) {
		this.instancer = instancer;
		this.model = model;
	}

	public Map<Material, ? extends Renderable> init(MeshPool allocator) {
		instancer.init();

		layers = model.get()
				.entrySet()
				.stream()
				.map(entry -> Pair.of(entry.getKey(), new Layer(allocator, entry.getKey(), entry.getValue())))
				.collect(ImmutableMap.toImmutableMap(Pair::first, Pair::second));


		return layers;
	}

	private class Layer implements Renderable {

		final Material material;
		MeshPool.BufferedMesh bufferedMesh;
		GlVertexArray vao;

		private Layer(MeshPool allocator, Material material, Mesh mesh) {
			this.material = material;
			vao = new GlVertexArray();
			bufferedMesh = allocator.alloc(mesh, vao);
			instancer.attributeBaseIndex = bufferedMesh.getAttributeCount();
			vao.enableArrays(bufferedMesh.getAttributeCount() + instancer.instanceFormat.getAttributeCount());
		}

		@Override
		public void render() {
			if (invalid()) return;

			vao.bind();

			instancer.renderSetup(vao);

			if (instancer.glInstanceCount > 0) {
				bufferedMesh.drawInstances(instancer.glInstanceCount);
			}

			// persistent mapping sync point
			instancer.vbo.doneForThisFrame();
		}

		@Override
		public boolean shouldRemove() {
			return invalid();
		}

		private boolean invalid() {
			return instancer.vbo == null || bufferedMesh == null || vao == null;
		}

		public void delete() {
			if (invalid()) return;

			vao.delete();
			bufferedMesh.delete();

			vao = null;
			bufferedMesh = null;
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

		instancer.vbo.delete();
		instancer.vbo = null;

		for (var layer : layers.values()) {
			layer.delete();
		}
	}
}
