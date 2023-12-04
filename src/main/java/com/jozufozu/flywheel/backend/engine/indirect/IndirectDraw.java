package com.jozufozu.flywheel.backend.engine.indirect;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.backend.MaterialEncoder;
import com.jozufozu.flywheel.backend.ShaderIndices;

public class IndirectDraw {
	private final IndirectModel model;
	private final IndirectMeshPool.BufferedMesh mesh;
	private final Material material;
	private final RenderStage stage;

	private final int vertexMaterialID;
	private final int fragmentMaterialID;
	private final int packedFogAndCutout;
	private final int packedMaterialProperties;

	public IndirectDraw(IndirectModel model, Material material, IndirectMeshPool.BufferedMesh mesh, RenderStage stage) {
		this.model = model;
		this.material = material;
		this.mesh = mesh;
		this.stage = stage;

		this.vertexMaterialID = ShaderIndices.getVertexShaderIndex(material.shaders());
		this.fragmentMaterialID = ShaderIndices.getFragmentShaderIndex(material.shaders());
		this.packedFogAndCutout = MaterialEncoder.packFogAndCutout(material);
		this.packedMaterialProperties = MaterialEncoder.packProperties(material);
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

	public void writeIndirectCommand(long ptr) {
		MemoryUtil.memPutInt(ptr, mesh.indexCount()); // count
		MemoryUtil.memPutInt(ptr + 4, 0); // instanceCount
		MemoryUtil.memPutInt(ptr + 8, mesh.firstIndex); // firstIndex
		MemoryUtil.memPutInt(ptr + 12, mesh.baseVertex); // baseVertex
		MemoryUtil.memPutInt(ptr + 16, model.baseInstance); // baseInstance

		MemoryUtil.memPutInt(ptr + 20, model.id); // modelID
		MemoryUtil.memPutInt(ptr + 24, vertexMaterialID); // vertexMaterialID
		MemoryUtil.memPutInt(ptr + 28, fragmentMaterialID); // fragmentMaterialID
		MemoryUtil.memPutInt(ptr + 32, packedFogAndCutout); // packedFogAndCutout
		MemoryUtil.memPutInt(ptr + 36, packedMaterialProperties); // packedMaterialProperties
	}
}
