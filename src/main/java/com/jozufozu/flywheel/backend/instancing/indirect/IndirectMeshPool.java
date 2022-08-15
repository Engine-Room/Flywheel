package com.jozufozu.flywheel.backend.instancing.indirect;

import static org.lwjgl.opengl.GL46.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.instancing.instancing.ElementBuffer;
import com.jozufozu.flywheel.backend.memory.MemoryBlock;
import com.jozufozu.flywheel.core.model.Mesh;

public class IndirectMeshPool {

	private final Map<Mesh, BufferedMesh> meshes = new HashMap<>();
	private final List<BufferedMesh> meshList = new ArrayList<>();

	final VertexType vertexType;

	final int vbo;
	private final MemoryBlock clientStorage;

	private boolean dirty;

	/**
	 * Create a new mesh pool.
	 */
	public IndirectMeshPool(VertexType type, int vertexCapacity) {
		vertexType = type;
		vbo = glCreateBuffers();
		var byteCapacity = type.byteOffset(vertexCapacity);
		glNamedBufferStorage(vbo, byteCapacity, GL_DYNAMIC_STORAGE_BIT);
		clientStorage = MemoryBlock.malloc(byteCapacity);
	}

	/**
	 * Allocate a model in the arena.
	 *
	 * @param mesh The model to allocate.
	 * @return A handle to the allocated model.
	 */
	public BufferedMesh alloc(Mesh mesh) {
		return meshes.computeIfAbsent(mesh, m -> {
			BufferedMesh bufferedModel = new BufferedMesh(m);
			meshList.add(bufferedModel);

			dirty = true;
			return bufferedModel;
		});
	}

	@Nullable
	public BufferedMesh get(Mesh mesh) {
		return meshes.get(mesh);
	}

	void uploadAll() {
		if (!dirty) {
			return;
		}
		dirty = false;

		final long ptr = clientStorage.ptr();

		int byteIndex = 0;
		int baseVertex = 0;
		for (BufferedMesh model : meshList) {
			model.byteIndex = byteIndex;
			model.baseVertex = baseVertex;

			model.buffer(ptr);

			byteIndex += model.getByteSize();
			baseVertex += model.mesh.getVertexCount();
		}

		nglNamedBufferSubData(vbo, 0, byteIndex, ptr);
	}

	public void delete() {
		clientStorage.free();
		glDeleteBuffers(vbo);
		meshes.clear();
		meshList.clear();
	}

	public class BufferedMesh {

		public final Mesh mesh;
		private final int vertexCount;
		private long byteIndex;
		private int baseVertex;

		public BufferedMesh(Mesh mesh) {
			this.mesh = mesh;

			vertexCount = mesh.getVertexCount();
		}

		private void buffer(long ptr) {
			this.mesh.write(ptr + byteIndex);
		}

		public int getByteSize() {
			return IndirectMeshPool.this.vertexType.getLayout().getStride() * this.vertexCount;
		}

		public int getBaseVertex() {
			return baseVertex;
		}

		public int getVertexCount() {
			return this.vertexCount;
		}

		public int getIndexCount() {
			return this.vertexCount * 6 / 4;
		}
	}

}
