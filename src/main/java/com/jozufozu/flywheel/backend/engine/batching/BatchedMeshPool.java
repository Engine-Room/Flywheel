package com.jozufozu.flywheel.backend.engine.batching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.vertex.VertexView;
import com.jozufozu.flywheel.api.vertex.VertexViewProviderRegistry;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;
import com.mojang.blaze3d.vertex.VertexFormat;

public class BatchedMeshPool {
	private final VertexFormat vertexFormat;
	private final VertexView vertexView;
	private final int growthMargin;

	private final Map<Mesh, BufferedMesh> meshes = new HashMap<>();
	private final List<BufferedMesh> allBuffered = new ArrayList<>();
	private final List<BufferedMesh> pendingBuffer = new ArrayList<>();

	private MemoryBlock data;
	private long byteSize;

	private boolean dirty;
	private boolean anyToRemove;

	/**
	 * Create a new mesh pool.
	 */
	public BatchedMeshPool(VertexFormat vertexFormat) {
		this.vertexFormat = vertexFormat;
		vertexView = VertexViewProviderRegistry.getProvider(vertexFormat).createVertexView();
		growthMargin = vertexFormat.getVertexSize() * 128;
	}

	public VertexFormat getVertexFormat() {
		return vertexFormat;
	}

	/**
	 * Allocate a mesh in the arena.
	 *
	 * @param mesh The mesh to allocate.
	 * @return A handle to the allocated mesh.
	 */
	public BufferedMesh alloc(Mesh mesh) {
		return meshes.computeIfAbsent(mesh, m -> {
			BufferedMesh bufferedMesh = new BufferedMesh(m, byteSize);
			byteSize += bufferedMesh.size();
			allBuffered.add(bufferedMesh);
			pendingBuffer.add(bufferedMesh);

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
			bufferPending();

			dirty = false;
			pendingBuffer.clear();
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
				pendingBuffer.add(mesh);
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

		if (data == null) {
			data = MemoryBlock.malloc(byteSize);
		} else if (byteSize > data.size()) {
			data = data.realloc(byteSize + growthMargin);
		}
	}

	private void bufferPending() {
		try {
			for (BufferedMesh mesh : pendingBuffer) {
				mesh.write(vertexView);
			}

			pendingBuffer.clear();
		} catch (Exception e) {
			Flywheel.LOGGER.error("Error uploading pooled meshes:", e);
		}
	}

	public void delete() {
		if (data != null) {
			data.free();
		}
		meshes.clear();
		allBuffered.clear();
		pendingBuffer.clear();
	}

	@Override
	public String toString() {
		return "BatchedMeshPool{" + "vertexFormat=" + vertexFormat + ", byteSize=" + byteSize + ", meshCount=" + meshes.size() + '}';
	}

	public class BufferedMesh {
		public final Mesh mesh;
		private final int vertexCount;
		private final int byteSize;
		private final Vector4fc boundingSphere;

		private long byteIndex;
		private boolean deleted;

		private BufferedMesh(Mesh mesh, long byteIndex) {
			this.mesh = mesh;
			vertexCount = mesh.vertexCount();
			byteSize = vertexCount * vertexFormat.getVertexSize();
			boundingSphere = mesh.boundingSphere();
			this.byteIndex = byteIndex;
		}

		public VertexFormat vertexFormat() {
			return vertexFormat;
		}

		public int vertexCount() {
			return vertexCount;
		}

		public int size() {
			return byteSize;
		}

		public Vector4fc boundingSphere() {
			return boundingSphere;
		}

		public boolean isDeleted() {
			return deleted;
		}

		public boolean isEmpty() {
			return mesh.isEmpty() || isDeleted();
		}

		private long ptr() {
			return BatchedMeshPool.this.data.ptr() + byteIndex;
		}

		private void write(VertexView vertexView) {
			if (isEmpty()) {
				return;
			}

			vertexView.ptr(ptr());
			vertexView.vertexCount(vertexCount);
			mesh.write(vertexView);
		}

		public void copyTo(long ptr) {
			if (isEmpty()) {
				return;
			}

			MemoryUtil.memCopy(ptr(), ptr, byteSize);
		}

		public void delete() {
			deleted = true;
			BatchedMeshPool.this.dirty = true;
			BatchedMeshPool.this.anyToRemove = true;
		}
	}
}
