package com.jozufozu.flywheel.backend.engine.indirect;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instancer.InstancePart;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.lib.material.MaterialIndices;

public final class IndirectDraw<P extends InstancePart> {
	private final IndirectInstancer<P> instancer;
	private final IndirectMeshPool.BufferedMesh mesh;
	private final Material material;
	private final RenderStage stage;
	int baseInstance = -1;

	final int vertexMaterialID;
	final int fragmentMaterialID;

	boolean needsFullWrite = true;

	IndirectDraw(IndirectInstancer<P> instancer, Material material, RenderStage stage, IndirectMeshPool.BufferedMesh mesh) {
		this.instancer = instancer;
		this.material = material;
		this.stage = stage;
		this.mesh = mesh;

		this.vertexMaterialID = MaterialIndices.getVertexShaderIndex(material);
		this.fragmentMaterialID = MaterialIndices.getFragmentShaderIndex(material);
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

	void writeObjects(long objectPtr, long batchIDPtr, int batchID) {
		if (needsFullWrite) {
			instancer.writeFull(objectPtr, batchIDPtr, batchID);
		} else {
			instancer.writeSparse(objectPtr, batchIDPtr, batchID);
		}
	}

	public void writeIndirectCommand(long ptr) {
		var boundingSphere = mesh.mesh.getBoundingSphere();

		MemoryUtil.memPutInt(ptr, mesh.getIndexCount()); // count
		MemoryUtil.memPutInt(ptr + 4, 0); // instanceCount - to be incremented by the compute shader
		MemoryUtil.memPutInt(ptr + 8, 0); // firstIndex - all models share the same index buffer
		MemoryUtil.memPutInt(ptr + 12, mesh.getBaseVertex()); // baseVertex
		MemoryUtil.memPutInt(ptr + 16, baseInstance); // baseInstance

		boundingSphere.getToAddress(ptr + 20); // boundingSphere
		MemoryUtil.memPutInt(ptr + 36, vertexMaterialID); // vertexMaterialID
		MemoryUtil.memPutInt(ptr + 40, fragmentMaterialID); // fragmentMaterialID

	}

	public IndirectInstancer<P> instancer() {
		return instancer;
	}

	public IndirectMeshPool.BufferedMesh mesh() {
		return mesh;
	}

	public Material material() {
		return material;
	}

	public RenderStage stage() {
		return stage;
	}
}
