package com.jozufozu.flywheel.backend.engine.indirect;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.backend.ShaderIndices;
import com.jozufozu.flywheel.backend.engine.MaterialEncoder;

public class IndirectDraw {
	private final IndirectModel model;
	private final Material material;
	private final IndirectMeshPool.BufferedMesh mesh;
	private final RenderStage stage;

	private final int materialVertexIndex;
	private final int materialFragmentIndex;
	private final int packedFogAndCutout;
	private final int packedMaterialProperties;

	public IndirectDraw(IndirectModel model, Material material, IndirectMeshPool.BufferedMesh mesh, RenderStage stage) {
		this.model = model;
		this.material = material;
		this.mesh = mesh;
		this.stage = stage;

		this.materialVertexIndex = ShaderIndices.getVertexShaderIndex(material.shaders());
		this.materialFragmentIndex = ShaderIndices.getFragmentShaderIndex(material.shaders());
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

	public void write(long ptr) {
		MemoryUtil.memPutInt(ptr, mesh.indexCount()); // count
		MemoryUtil.memPutInt(ptr + 4, 0); // instanceCount - to be set by the apply shader
		MemoryUtil.memPutInt(ptr + 8, mesh.firstIndex()); // firstIndex
		MemoryUtil.memPutInt(ptr + 12, mesh.baseVertex()); // baseVertex
		MemoryUtil.memPutInt(ptr + 16, 0); // baseInstance - to be set by the apply shader

		MemoryUtil.memPutInt(ptr + 20, model.index); // modelIndex - never changes

		MemoryUtil.memPutInt(ptr + 24, materialVertexIndex); // materialVertexIndex
		MemoryUtil.memPutInt(ptr + 28, materialFragmentIndex); // materialFragmentIndex
		MemoryUtil.memPutInt(ptr + 32, packedFogAndCutout); // packedFogAndCutout
		MemoryUtil.memPutInt(ptr + 36, packedMaterialProperties); // packedMaterialProperties
	}
}
