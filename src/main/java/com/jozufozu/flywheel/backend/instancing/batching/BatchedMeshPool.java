package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.vertex.ReusableVertexList;
import com.jozufozu.flywheel.api.vertex.VertexListProvider;
import com.jozufozu.flywheel.backend.memory.MemoryBlock;
import com.jozufozu.flywheel.core.model.Mesh;
import com.mojang.blaze3d.vertex.VertexFormat;

public class BatchedMeshPool {
	private final VertexFormat vertexFormat;
	private final ReusableVertexList vertexList;
	private final int growthMargin;

	private final Map<Mesh, BufferedMesh> meshes = new HashMap<>();
	private final List<BufferedMesh> allBuffered = new ArrayList<>();
	private final List<BufferedMesh> pendingUpload = new ArrayList<>();

	private MemoryBlock memory;
	private long byteSize;

	private boolean dirty;
	private boolean anyToRemove;

	/**
	 * Create a new mesh pool.
	 */
	public BatchedMeshPool(VertexFormat vertexFormat) {
		this.vertexFormat = vertexFormat;
		vertexList = VertexListProvider.get(vertexFormat).createVertexList();
		growthMargin = vertexList.vertexStride() * 32;
	}

	/**
	 * Allocate a mesh in the arena.
	 *
	 * @param mesh The mesh to allocate.
	 * @return A handle to the allocated mesh.
	 */
	public BufferedMesh alloc(Mesh mesh) {
		return meshes.computeIfAbsent(mesh, m -> {
			BufferedMesh bufferedMesh = new BufferedMesh(m, byteSize, m.getVertexCount() * vertexList.vertexStride());
			byteSize += bufferedMesh.size();
			allBuffered.add(bufferedMesh);
			pendingUpload.add(bufferedMesh);

			dirty = true;
			return bufferedMesh;
		});
	}

	@Nullable
	public BufferedMesh get(Mesh mesh) {
		return meshes.get(mesh);
	}

	public void flush() {
		if (dirty) {
			if (anyToRemove) {
				processDeletions();
			}

			realloc();
			uploadPending();

			dirty = false;
			pendingUpload.clear();
		}
	}

	private void processDeletions() {
		// remove deleted meshes
		allBuffered.removeIf(bufferedMesh -> {
			boolean deleted = bufferedMesh.isDeleted();
			if (deleted) {
				meshes.remove(bufferedMesh.mesh);
			}
			return deleted;
		});

		// re-evaluate first vertex for each mesh
		int byteIndex = 0;
		for (BufferedMesh mesh : allBuffered) {
			if (mesh.byteIndex != byteIndex) {
				pendingUpload.add(mesh);
			}

			mesh.byteIndex = byteIndex;

			byteIndex += mesh.size();
		}

		this.byteSize = byteIndex;
		this.anyToRemove = false;
	}

	private void realloc() {
		if (byteSize < 0) {
			throw new IllegalArgumentException("Size " + byteSize + " < 0");
		}

		if (byteSize == 0) {
			return;
		}

		if (memory == null) {
			memory = MemoryBlock.malloc(byteSize);
		} else if (byteSize > memory.size()) {
			memory = memory.realloc(byteSize + growthMargin);
		}
	}

	private void uploadPending() {
		try {
			for (BufferedMesh mesh : pendingUpload) {
				mesh.buffer(vertexList);
			}

			pendingUpload.clear();
		} catch (Exception e) {
			Flywheel.LOGGER.error("Error uploading pooled meshes:", e);
		}
	}

	public void delete() {
		if (memory != null) {
			memory.free();
		}
		meshes.clear();
		allBuffered.clear();
		pendingUpload.clear();
	}

	public VertexFormat getVertexFormat() {
		return vertexFormat;
	}

	@Override
	public String toString() {
		return "BatchedMeshPool{" + "vertexFormat=" + vertexFormat + ", byteSize=" + byteSize + ", meshCount=" + meshes.size() + '}';
	}

	public class BufferedMesh {
		private final Mesh mesh;
		private long byteIndex;
		private int byteSize;
		private boolean deleted;

		private BufferedMesh(Mesh mesh, long byteIndex, int byteSize) {
			this.mesh = mesh;
			this.byteIndex = byteIndex;
			this.byteSize = byteSize;
		}

		private void buffer(ReusableVertexList vertexList) {
			vertexList.ptr(ptr());
			vertexList.vertexCount(mesh.getVertexCount());

			mesh.write(vertexList);
		}

		public void copyTo(long ptr) {
			if (isEmpty()) {
				return;
			}

			MemoryUtil.memCopy(ptr(), ptr, byteSize);
		}

		private boolean isEmpty() {
			return mesh.isEmpty() || isDeleted();
		}

		private long ptr() {
			return BatchedMeshPool.this.memory.ptr() + byteIndex;
		}

		public void delete() {
			deleted = true;
			BatchedMeshPool.this.dirty = true;
			BatchedMeshPool.this.anyToRemove = true;
		}

		public Mesh getMesh() {
			return mesh;
		}

		public int size() {
			return byteSize;
		}

		public VertexFormat getVertexFormat() {
			return vertexFormat;
		}

		public boolean isDeleted() {
			return deleted;
		}
	}
}
