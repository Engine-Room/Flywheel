package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.backend.gl.GlVertexArray;
import com.jozufozu.flywheel.backend.model.BufferedModel;
import com.jozufozu.flywheel.backend.model.MeshAllocator;
import com.jozufozu.flywheel.core.model.ModelSupplier;

import net.minecraft.client.renderer.RenderType;

public class InstancedModel<D extends InstanceData> {

	GPUInstancer<D> instancer;
	ModelSupplier model;

	@Nullable
	private BufferedModel bufferedMesh;
	@Nullable
	private GlVertexArray vao;

	public InstancedModel(GPUInstancer<D> instancer, ModelSupplier model) {
		this.instancer = instancer;
		this.model = model;
	}

	public Map<RenderType, Renderable> init(MeshAllocator allocator) {
		instancer.init();

		vao = new GlVertexArray();

		bufferedMesh = allocator.alloc(model.get(), vao);
		instancer.attributeBaseIndex = bufferedMesh.getAttributeCount();
		vao.enableArrays(bufferedMesh.getAttributeCount() + instancer.instanceFormat.getAttributeCount());

		return ImmutableMap.of(RenderType.solid(), this::render);
	}

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

	private boolean invalid() {
		return instancer.vbo == null || bufferedMesh == null || vao == null;
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

	public boolean isEmpty() {
		return instancer.isEmpty();
	}

	public void delete() {
		if (invalid()) return;

		vao.delete();
		bufferedMesh.delete();
		instancer.vbo.delete();

		vao = null;
		bufferedMesh = null;
		instancer.vbo = null;
	}
}
