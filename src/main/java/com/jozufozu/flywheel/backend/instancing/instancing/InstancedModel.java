package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.api.InstancedPart;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.backend.gl.GlVertexArray;
import com.jozufozu.flywheel.backend.model.MeshPool;
import com.jozufozu.flywheel.core.model.Mesh;
import com.jozufozu.flywheel.core.model.ModelSupplier;
import com.jozufozu.flywheel.util.Pair;

public class InstancedModel<D extends InstancedPart> {

	public final GPUInstancer<D> instancer;
	public final ModelSupplier model;
	private List<Layer> layers;

	public InstancedModel(StructType<D> type, ModelSupplier model) {
		this.model = model;
		this.instancer = new GPUInstancer<>(this, type);
	}

	public void init() {
		instancer.init();

		buildLayers();
	}

	public List<? extends Renderable> getLayers() {
		return layers;
	}

	private void buildLayers() {
		layers = model.get()
				.entrySet()
				.stream()
				.map(entry -> new Layer(entry.getKey(), entry.getValue()))
				.toList();
	}

	private class Layer implements Renderable {

		final Material material;
		MeshPool.BufferedMesh bufferedMesh;
		GlVertexArray vao;

		private Layer(Material material, Mesh mesh) {
			this.material = material;
			vao = new GlVertexArray();
			bufferedMesh = MeshPool.getInstance()
					.alloc(mesh);
			instancer.attributeBaseIndex = bufferedMesh.getAttributeCount();
			vao.enableArrays(bufferedMesh.getAttributeCount() + instancer.instanceFormat.getAttributeCount());
		}

		@Override
		public Material getMaterial() {
			return material;
		}

		@Override
		public VertexType getVertexType() {
			return bufferedMesh.getVertexType();
		}

		@Override
		public void render() {
			if (invalid()) return;

			try (var ignored = GlStateTracker.getRestoreState()) {

				instancer.renderSetup(vao);

				if (instancer.glInstanceCount > 0) {
					bufferedMesh.drawInstances(vao, instancer.glInstanceCount);
				}
			}
		}

		@Override
		public boolean shouldRemove() {
			return invalid();
		}

		/**
		 * Only {@code true} if the InstancedModel has been destroyed.
		 */
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

		for (var layer : layers) {
			layer.delete();
		}
	}
}
