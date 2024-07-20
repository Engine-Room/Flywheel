package dev.engine_room.flywheel.backend.engine.indirect;

import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.visualization.VisualType;
import dev.engine_room.flywheel.backend.ShaderIndices;
import dev.engine_room.flywheel.backend.engine.MaterialEncoder;
import dev.engine_room.flywheel.backend.engine.MeshPool;

public class IndirectDraw {
	private final IndirectInstancer<?> instancer;
	private final Material material;
	private final MeshPool.PooledMesh mesh;
	private final VisualType visualType;
	private final int indexOfMeshInModel;

	private final int materialVertexIndex;
	private final int materialFragmentIndex;
	private final int packedFogAndCutout;
	private final int packedMaterialProperties;
	private boolean deleted;

	public IndirectDraw(IndirectInstancer<?> instancer, Material material, MeshPool.PooledMesh mesh, VisualType visualType, int indexOfMeshInModel) {
		this.instancer = instancer;
		this.material = material;
		this.mesh = mesh;
		this.visualType = visualType;
		this.indexOfMeshInModel = indexOfMeshInModel;

		mesh.acquire();

		this.materialVertexIndex = ShaderIndices.getVertexShaderIndex(material.shaders());
		this.materialFragmentIndex = ShaderIndices.getFragmentShaderIndex(material.shaders());
		this.packedFogAndCutout = MaterialEncoder.packFogAndCutout(material);
		this.packedMaterialProperties = MaterialEncoder.packProperties(material);
	}

	public boolean deleted() {
		return deleted;
	}

	public Material material() {
		return material;
	}

	public MeshPool.PooledMesh mesh() {
		return mesh;
	}

	public VisualType visualType() {
		return visualType;
	}

	public int indexOfMeshInModel() {
		return indexOfMeshInModel;
	}

	public void write(long ptr) {
		MemoryUtil.memPutInt(ptr, mesh.indexCount()); // count
		MemoryUtil.memPutInt(ptr + 4, 0); // instanceCount - to be set by the apply shader
		MemoryUtil.memPutInt(ptr + 8, mesh.firstIndex()); // firstIndex
		MemoryUtil.memPutInt(ptr + 12, mesh.baseVertex()); // baseVertex
		MemoryUtil.memPutInt(ptr + 16, instancer.baseInstance); // baseInstance

		MemoryUtil.memPutInt(ptr + 20, instancer.modelIndex); // modelIndex

		MemoryUtil.memPutInt(ptr + 24, materialVertexIndex); // materialVertexIndex
		MemoryUtil.memPutInt(ptr + 28, materialFragmentIndex); // materialFragmentIndex
		MemoryUtil.memPutInt(ptr + 32, packedFogAndCutout); // packedFogAndCutout
		MemoryUtil.memPutInt(ptr + 36, packedMaterialProperties); // packedMaterialProperties
	}

	public void writeWithOverrides(long ptr, int instanceIndex, Material materialOverride) {
		MemoryUtil.memPutInt(ptr, mesh.indexCount()); // count
		MemoryUtil.memPutInt(ptr + 4, 1); // instanceCount - only drawing one instance
		MemoryUtil.memPutInt(ptr + 8, mesh.firstIndex()); // firstIndex
		MemoryUtil.memPutInt(ptr + 12, mesh.baseVertex()); // baseVertex
		MemoryUtil.memPutInt(ptr + 16, instancer.baseInstance + instanceIndex); // baseInstance

		MemoryUtil.memPutInt(ptr + 20, instancer.modelIndex); // modelIndex

		MemoryUtil.memPutInt(ptr + 24, ShaderIndices.getVertexShaderIndex(materialOverride.shaders())); // materialVertexIndex
		MemoryUtil.memPutInt(ptr + 28, ShaderIndices.getFragmentShaderIndex(materialOverride.shaders())); // materialFragmentIndex
		MemoryUtil.memPutInt(ptr + 32, MaterialEncoder.packFogAndCutout(materialOverride)); // packedFogAndCutout
		MemoryUtil.memPutInt(ptr + 36, MaterialEncoder.packProperties(materialOverride)); // packedMaterialProperties
	}

	public void delete() {
		if (deleted) {
			return;
		}

		mesh.release();

		deleted = true;
	}
}
