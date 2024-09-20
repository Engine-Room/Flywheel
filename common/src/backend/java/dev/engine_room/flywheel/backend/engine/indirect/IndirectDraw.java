package dev.engine_room.flywheel.backend.engine.indirect;

import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.visualization.VisualType;
import dev.engine_room.flywheel.backend.MaterialShaderIndices;
import dev.engine_room.flywheel.backend.engine.MaterialEncoder;
import dev.engine_room.flywheel.backend.engine.MeshPool;
import dev.engine_room.flywheel.backend.engine.embed.EmbeddedEnvironment;

public class IndirectDraw {
	private final IndirectInstancer<?> instancer;
	private final Material material;
	private final MeshPool.PooledMesh mesh;
	private final VisualType visualType;
	private final int bias;
	private final int indexOfMeshInModel;

	private final int materialVertexIndex;
	private final int materialFragmentIndex;
	private final int packedFogAndCutout;
	private final int packedMaterialProperties;
	private boolean deleted;

	public IndirectDraw(IndirectInstancer<?> instancer, Material material, MeshPool.PooledMesh mesh, VisualType visualType, int bias, int indexOfMeshInModel) {
		this.instancer = instancer;
		this.material = material;
		this.mesh = mesh;
		this.visualType = visualType;
		this.bias = bias;
		this.indexOfMeshInModel = indexOfMeshInModel;

		mesh.acquire();

		this.materialVertexIndex = MaterialShaderIndices.vertexIndex(material.shaders());
		this.materialFragmentIndex = MaterialShaderIndices.fragmentIndex(material.shaders());
		this.packedFogAndCutout = MaterialEncoder.packUberShader(material);
		this.packedMaterialProperties = MaterialEncoder.packProperties(material);
	}

	public boolean deleted() {
		return deleted;
	}

	public Material material() {
		return material;
	}

	public boolean isEmbedded() {
		return instancer.environment instanceof EmbeddedEnvironment;
	}

	public MeshPool.PooledMesh mesh() {
		return mesh;
	}

	public VisualType visualType() {
		return visualType;
	}

	public int bias() {
		return bias;
	}

	public int indexOfMeshInModel() {
		return indexOfMeshInModel;
	}

	public void write(long ptr) {
		MemoryUtil.memPutInt(ptr, mesh.indexCount()); // count
		MemoryUtil.memPutInt(ptr + 4, 0); // instanceCount - to be set by the apply shader
		MemoryUtil.memPutInt(ptr + 8, mesh.firstIndex()); // firstIndex
		MemoryUtil.memPutInt(ptr + 12, mesh.baseVertex()); // baseVertex
		MemoryUtil.memPutInt(ptr + 16, instancer.baseInstance()); // baseInstance

		MemoryUtil.memPutInt(ptr + 20, instancer.modelIndex()); // modelIndex

		MemoryUtil.memPutInt(ptr + 24, instancer.environment.matrixIndex()); // matrixIndex

		MemoryUtil.memPutInt(ptr + 28, materialVertexIndex); // materialVertexIndex
		MemoryUtil.memPutInt(ptr + 32, materialFragmentIndex); // materialFragmentIndex
		MemoryUtil.memPutInt(ptr + 36, packedFogAndCutout); // packedFogAndCutout
		MemoryUtil.memPutInt(ptr + 40, packedMaterialProperties); // packedMaterialProperties
	}

	public void writeWithOverrides(long ptr, int instanceIndex, Material materialOverride) {
		MemoryUtil.memPutInt(ptr, mesh.indexCount()); // count
		MemoryUtil.memPutInt(ptr + 4, 1); // instanceCount - only drawing one instance
		MemoryUtil.memPutInt(ptr + 8, mesh.firstIndex()); // firstIndex
		MemoryUtil.memPutInt(ptr + 12, mesh.baseVertex()); // baseVertex
		MemoryUtil.memPutInt(ptr + 16, instancer.baseInstance() + instanceIndex); // baseInstance

		MemoryUtil.memPutInt(ptr + 20, instancer.modelIndex()); // modelIndex

		MemoryUtil.memPutInt(ptr + 24, instancer.environment.matrixIndex()); // matrixIndex

		MemoryUtil.memPutInt(ptr + 28, MaterialShaderIndices.vertexIndex(materialOverride.shaders())); // materialVertexIndex
		MemoryUtil.memPutInt(ptr + 32, MaterialShaderIndices.fragmentIndex(materialOverride.shaders())); // materialFragmentIndex
		MemoryUtil.memPutInt(ptr + 36, MaterialEncoder.packUberShader(materialOverride)); // packedFogAndCutout
		MemoryUtil.memPutInt(ptr + 40, MaterialEncoder.packProperties(materialOverride)); // packedMaterialProperties
	}

	public void delete() {
		if (deleted) {
			return;
		}

		mesh.release();

		deleted = true;
	}
}
