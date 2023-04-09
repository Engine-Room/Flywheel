package com.jozufozu.flywheel.backend.engine.indirect;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.lib.material.MaterialIndices;

public class IndirectDraw<I extends Instance> {
	private final IndirectInstancer<I> instancer;
	private final IndirectMeshPool.BufferedMesh mesh;
	private final Material material;
	private final RenderStage stage;

	private final int vertexMaterialID;
	private final int fragmentMaterialID;

	private int baseInstance = -1;
	private boolean needsFullWrite = true;

	public IndirectDraw(IndirectInstancer<I> instancer, Material material, IndirectMeshPool.BufferedMesh mesh, RenderStage stage) {
		this.instancer = instancer;
		this.material = material;
		this.mesh = mesh;
		this.stage = stage;

		this.vertexMaterialID = MaterialIndices.getVertexShaderIndex(material);
		this.fragmentMaterialID = MaterialIndices.getFragmentShaderIndex(material);
	}

	public IndirectInstancer<I> instancer() {
		return instancer;
	}

	public Material material() {
		return material;
	}

	public IndirectMeshPool.BufferedMesh mesh() {
		return mesh;
	}

	public RenderStage stage() {
		return stage;
	}

	public void prepare(int baseInstance) {
		instancer.update();
		if (baseInstance == this.baseInstance) {
			needsFullWrite = false;
			return;
		}
		this.baseInstance = baseInstance;
		needsFullWrite = true;
	}

	public void writeObjects(long objectPtr, long batchIDPtr, int batchID) {
		if (needsFullWrite) {
			instancer.writeFull(objectPtr, batchIDPtr, batchID);
		} else {
			instancer.writeSparse(objectPtr, batchIDPtr, batchID);
		}
	}

	public void writeIndirectCommand(long ptr) {
		var boundingSphere = mesh.getMesh().getBoundingSphere();

		MemoryUtil.memPutInt(ptr, mesh.getIndexCount()); // count
		MemoryUtil.memPutInt(ptr + 4, 0); // instanceCount - to be incremented by the compute shader
		MemoryUtil.memPutInt(ptr + 8, 0); // firstIndex - all models share the same index buffer
		MemoryUtil.memPutInt(ptr + 12, mesh.getBaseVertex()); // baseVertex
		MemoryUtil.memPutInt(ptr + 16, baseInstance); // baseInstance

		boundingSphere.getToAddress(ptr + 20); // boundingSphere
		MemoryUtil.memPutInt(ptr + 36, vertexMaterialID); // vertexMaterialID
		MemoryUtil.memPutInt(ptr + 40, fragmentMaterialID); // fragmentMaterialID
	}
}
