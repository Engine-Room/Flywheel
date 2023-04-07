package com.jozufozu.flywheel.backend.engine.indirect;

import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL44.GL_DYNAMIC_STORAGE_BIT;
import static org.lwjgl.opengl.GL45.glCreateBuffers;
import static org.lwjgl.opengl.GL45.glNamedBufferStorage;
import static org.lwjgl.opengl.GL45.nglNamedBufferSubData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;

public class IndirectMeshPool {
	private final VertexType vertexType;

	private final Map<Mesh, BufferedMesh> meshes = new HashMap<>();
	private final List<BufferedMesh> meshList = new ArrayList<>();

	final int vbo;
	private final MemoryBlock clientStorage;

	private boolean dirty;

	/**
	 * Create a new mesh pool.
	 */
	public IndirectMeshPool(VertexType type, int vertexCapacity) {
		vertexType = type;
		vbo = glCreateBuffers();
		var byteCapacity = type.getLayout().getStride() * vertexCapacity;
		glNamedBufferStorage(vbo, byteCapacity, GL_DYNAMIC_STORAGE_BIT);
		clientStorage = MemoryBlock.malloc(byteCapacity);
	}

	public VertexType getVertexType() {
		return vertexType;
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

	public void flush() {
		if (dirty) {
			uploadAll();
			dirty = false;
		}
	}

	private void uploadAll() {
		final long ptr = clientStorage.ptr();

		int byteIndex = 0;
		int baseVertex = 0;
		for (BufferedMesh mesh : meshList) {
			mesh.byteIndex = byteIndex;
			mesh.baseVertex = baseVertex;

			mesh.buffer(ptr);

			byteIndex += mesh.size();
			baseVertex += mesh.mesh.getVertexCount();
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
		private final Mesh mesh;
		private final int vertexCount;

		private long byteIndex;
		private int baseVertex;

		private BufferedMesh(Mesh mesh) {
			this.mesh = mesh;
			vertexCount = mesh.getVertexCount();
		}

		public Mesh getMesh() {
			return mesh;
		}

		public int size() {
			return mesh.size();
		}

		public int getBaseVertex() {
			return baseVertex;
		}

		public int getIndexCount() {
			return vertexCount * 6 / 4;
		}

		public VertexType getVertexType() {
			return vertexType;
		}

		private void buffer(long ptr) {
			mesh.write(ptr + byteIndex);
		}
	}
}
