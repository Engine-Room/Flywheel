package com.jozufozu.flywheel.backend.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL32;

import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.vertex.VertexView;
import com.jozufozu.flywheel.backend.InternalVertex;
import com.jozufozu.flywheel.backend.gl.GlPrimitive;
import com.jozufozu.flywheel.backend.gl.array.GlVertexArray;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;

import it.unimi.dsi.fastutil.objects.ReferenceArraySet;

public class MeshPool {
	private final VertexView vertexView;
	private final Map<Mesh, BufferedMesh> meshes = new HashMap<>();
	private final List<BufferedMesh> meshList = new ArrayList<>();
	private final List<BufferedMesh> recentlyAllocated = new ArrayList<>();

	private final GlBuffer vbo;
	private final IndexPool indexPool;

	private boolean dirty;
	private boolean anyToRemove;

	/**
	 * Create a new mesh pool.
	 */
	public MeshPool() {
		vertexView = InternalVertex.createVertexView();
		vbo = new GlBuffer();
		indexPool = new IndexPool();
	}

	/**
	 * Allocate a model in the arena.
	 *
	 * @param mesh The model to allocate.
	 * @return A handle to the allocated model.
	 */
	public BufferedMesh alloc(Mesh mesh) {
		return meshes.computeIfAbsent(mesh, this::_alloc);
	}

	private BufferedMesh _alloc(Mesh m) {
		BufferedMesh bufferedModel = new BufferedMesh(m);
		meshList.add(bufferedModel);
		recentlyAllocated.add(bufferedModel);

		dirty = true;
		return bufferedModel;
	}

	@Nullable
	public BufferedMesh get(Mesh mesh) {
		return meshes.get(mesh);
	}

	public void flush() {
        if (!dirty) {
            return;
        }

		if (anyToRemove) {
			anyToRemove = false;
			processDeletions();

			// Might want to shrink the index pool if something was removed.
			indexPool.reset();
			for (BufferedMesh mesh : meshList) {
				indexPool.updateCount(mesh.mesh.indexSequence(), mesh.indexCount());
			}
		} else {
			// Otherwise, just update the index with the new counts.
			for (BufferedMesh mesh : recentlyAllocated) {
				indexPool.updateCount(mesh.mesh.indexSequence(), mesh.indexCount());
			}
		}

		// Always need to flush the index pool.
		indexPool.flush();

		uploadAll();
        dirty = false;
    }

	private void processDeletions() {
		// remove deleted meshes
		meshList.removeIf(bufferedMesh -> {
			boolean deleted = bufferedMesh.deleted();
			if (deleted) {
				meshes.remove(bufferedMesh.mesh);
			}
			return deleted;
		});
	}

	private void uploadAll() {
		long neededSize = 0;
		for (BufferedMesh mesh : meshList) {
			neededSize += mesh.byteSize();
		}

		final var vertexBlock = MemoryBlock.malloc(neededSize);
		final long vertexPtr = vertexBlock.ptr();

		int byteIndex = 0;
		int baseVertex = 0;
		for (BufferedMesh mesh : meshList) {
			mesh.byteIndex = byteIndex;
			mesh.baseVertex = baseVertex;

			vertexView.ptr(vertexPtr + mesh.byteIndex);
			vertexView.vertexCount(mesh.vertexCount);
			mesh.mesh.write(vertexView);

			byteIndex += mesh.byteSize();
			baseVertex += mesh.vertexCount();

			mesh.boundTo.clear();
		}

		vbo.upload(vertexBlock);

		vertexBlock.free();
	}

	public void bind(GlVertexArray vertexArray) {
		indexPool.bind(vertexArray);
		vertexArray.bindVertexBuffer(0, vbo.handle(), 0, InternalVertex.STRIDE);
		vertexArray.bindAttributes(0, 0, InternalVertex.ATTRIBUTES);
	}

	public void delete() {
		vbo.delete();
		indexPool.delete();
		meshes.clear();
		meshList.clear();
	}

	public class BufferedMesh {
		private final Mesh mesh;
		private final int vertexCount;
		private final int byteSize;

		private long byteIndex;
		private int baseVertex;

		private int referenceCount = 0;
		private final Set<GlVertexArray> boundTo = new ReferenceArraySet<>();

		private BufferedMesh(Mesh mesh) {
			this.mesh = mesh;
			vertexCount = mesh.vertexCount();
			byteSize = vertexCount * InternalVertex.STRIDE;
		}

		public int vertexCount() {
			return vertexCount;
		}

		public int byteSize() {
			return byteSize;
		}

		public int indexCount() {
			return mesh.indexCount();
		}

		public int baseVertex() {
			return baseVertex;
		}

		public int firstIndex() {
			return indexPool.firstIndex(mesh.indexSequence());
		}

		public boolean deleted() {
			return referenceCount <= 0;
		}

		public boolean invalid() {
			return mesh.vertexCount() == 0 || deleted() || byteIndex == -1;
		}

		public void draw(int instanceCount) {
			if (instanceCount > 1) {
				GL32.glDrawElementsInstanced(GlPrimitive.TRIANGLES.glEnum, mesh.indexCount(), GL32.GL_UNSIGNED_INT, firstIndex(), instanceCount);
			} else {
				GL32.glDrawElements(GlPrimitive.TRIANGLES.glEnum, mesh.indexCount(), GL32.GL_UNSIGNED_INT, firstIndex());
			}
		}

		public void setup(GlVertexArray vao) {
            if (!boundTo.add(vao)) {
                return;
            }
            MeshPool.this.indexPool.bind(vao);
            vao.bindVertexBuffer(0, MeshPool.this.vbo.handle(), byteIndex, InternalVertex.STRIDE);
            vao.bindAttributes(0, 0, InternalVertex.ATTRIBUTES);
        }

		public void acquire() {
			referenceCount++;
		}

		public void drop() {
			if (--referenceCount == 0) {
				MeshPool.this.dirty = true;
				MeshPool.this.anyToRemove = true;
			}
		}
	}
}
