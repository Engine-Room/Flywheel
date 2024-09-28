package dev.engine_room.flywheel.backend.engine.indirect;

import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.visualization.VisualType;
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

		MemoryUtil.memPutInt(ptr + 28, packedFogAndCutout); // packedFogAndCutout
		MemoryUtil.memPutInt(ptr + 32, packedMaterialProperties); // packedMaterialProperties
	}

	public void writeWithOverrides(long ptr, int instanceIndex, Material materialOverride) {
		MemoryUtil.memPutInt(ptr, mesh.indexCount()); // count
		MemoryUtil.memPutInt(ptr + 4, 1); // instanceCount - only drawing one instance
		MemoryUtil.memPutInt(ptr + 8, mesh.firstIndex()); // firstIndex
		MemoryUtil.memPutInt(ptr + 12, mesh.baseVertex()); // baseVertex
		MemoryUtil.memPutInt(ptr + 16, instancer.local2GlobalInstanceIndex(instanceIndex)); // baseInstance

		MemoryUtil.memPutInt(ptr + 20, instancer.modelIndex()); // modelIndex

		MemoryUtil.memPutInt(ptr + 24, instancer.environment.matrixIndex()); // matrixIndex

		MemoryUtil.memPutInt(ptr + 28, MaterialEncoder.packUberShader(materialOverride)); // packedFogAndCutout
		MemoryUtil.memPutInt(ptr + 32, MaterialEncoder.packProperties(materialOverride)); // packedMaterialProperties
	}

	public void delete() {
		if (deleted) {
			return;
		}

		mesh.release();

		deleted = true;
	}
}
