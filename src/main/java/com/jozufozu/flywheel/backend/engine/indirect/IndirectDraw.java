package com.jozufozu.flywheel.backend.engine.indirect;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.backend.MaterialShaderIndices;
import com.jozufozu.flywheel.backend.MaterialUtil;

public class IndirectDraw<I extends Instance> {
	private final IndirectInstancer<I> instancer;
	private final IndirectMeshPool.BufferedMesh mesh;
	private final Material material;
	private final RenderStage stage;

	private final int vertexMaterialID;
	private final int fragmentMaterialID;
	private final int packedMaterialProperties;

	private int baseInstance = -1;
	private boolean needsFullWrite = true;

	public IndirectDraw(IndirectInstancer<I> instancer, Material material, IndirectMeshPool.BufferedMesh mesh, RenderStage stage) {
		this.instancer = instancer;
		this.material = material;
		this.mesh = mesh;
		this.stage = stage;

		this.vertexMaterialID = MaterialShaderIndices.getVertexShaderIndex(material.shaders());
		this.fragmentMaterialID = MaterialShaderIndices.getFragmentShaderIndex(material.shaders());
		this.packedMaterialProperties = MaterialUtil.packProperties(material);
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

	public void writeObjects(long objectPtr, int batchID) {
		if (needsFullWrite) {
			instancer.writeFull(objectPtr, batchID);
		} else {
			instancer.writeSparse(objectPtr, batchID);
		}
	}

	public void writeIndirectCommand(long ptr) {
		var boundingSphere = mesh.boundingSphere();

		MemoryUtil.memPutInt(ptr, mesh.indexCount()); // count
		MemoryUtil.memPutInt(ptr + 4, 0); // instanceCount - to be incremented by the compute shader
		MemoryUtil.memPutInt(ptr + 8, mesh.firstIndex); // firstIndex
		MemoryUtil.memPutInt(ptr + 12, mesh.baseVertex); // baseVertex
		MemoryUtil.memPutInt(ptr + 16, baseInstance); // baseInstance

		boundingSphere.getToAddress(ptr + 20); // boundingSphere
		MemoryUtil.memPutInt(ptr + 36, vertexMaterialID); // vertexMaterialID
		MemoryUtil.memPutInt(ptr + 40, fragmentMaterialID); // fragmentMaterialID
		MemoryUtil.memPutInt(ptr + 44, packedMaterialProperties); // packedMaterialProperties
	}
}
