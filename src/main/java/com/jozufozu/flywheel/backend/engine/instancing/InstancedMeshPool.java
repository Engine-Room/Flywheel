package com.jozufozu.flywheel.backend.engine.instancing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL32;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.vertex.VertexView;
import com.jozufozu.flywheel.backend.InternalVertex;
import com.jozufozu.flywheel.backend.gl.GlPrimitive;
import com.jozufozu.flywheel.backend.gl.array.GlVertexArray;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;

public class InstancedMeshPool {
	private final VertexView vertexView;
	private final Map<Mesh, BufferedMesh> meshes = new HashMap<>();
	private final List<BufferedMesh> allBuffered = new ArrayList<>();
	private final List<BufferedMesh> pendingUpload = new ArrayList<>();

	private final GlBuffer vbo;
	private long byteSize;

	private boolean dirty;
	private boolean anyToRemove;

	/**
	 * Create a new mesh pool.
	 */
	public InstancedMeshPool() {
		vertexView = InternalVertex.createVertexView();
		int stride = InternalVertex.STRIDE;
		vbo = new GlBuffer();
		vbo.growthFunction(l -> Math.max(l + stride * 128L, (long) (l * 1.6)));
	}

	/**
	 * Allocate a mesh in the arena.
	 *
	 * @param mesh     The mesh to allocate.
	 * @param eboCache The EBO cache to use.
	 * @return A handle to the allocated mesh.
	 */
	public BufferedMesh alloc(Mesh mesh, EboCache eboCache) {
		return meshes.computeIfAbsent(mesh, m -> {
			BufferedMesh bufferedMesh = new BufferedMesh(m, byteSize, eboCache);
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
		if (!dirty) {
			return;
		}

		if (anyToRemove) {
			processDeletions();
		}

		vbo.ensureCapacity(byteSize);

		uploadPending();

		dirty = false;
		pendingUpload.clear();
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

	private void uploadPending() {
		try (MappedBuffer mapped = vbo.map()) {
			long ptr = mapped.ptr();

			for (BufferedMesh mesh : pendingUpload) {
				mesh.write(ptr, vertexView);
				mesh.boundTo.clear();
			}

			pendingUpload.clear();
		} catch (Exception e) {
			Flywheel.LOGGER.error("Error uploading pooled meshes:", e);
		}
	}

	public void delete() {
		vbo.delete();
		meshes.clear();
		allBuffered.clear();
		pendingUpload.clear();
	}

	@Override
	public String toString() {
		return "InstancedMeshPool{" + "byteSize=" + byteSize + ", meshCount=" + meshes.size() + '}';
	}

	public class BufferedMesh {
		private final Mesh mesh;
		private final int vertexCount;
		private final int byteSize;
		private final int ebo;

		private long byteIndex;
		private boolean deleted;

		private final Set<GlVertexArray> boundTo = new HashSet<>();

		private BufferedMesh(Mesh mesh, long byteIndex, EboCache eboCache) {
			this.mesh = mesh;
			vertexCount = mesh.vertexCount();
			byteSize = vertexCount * InternalVertex.STRIDE;
			this.byteIndex = byteIndex;
			this.ebo = eboCache.get(mesh.indexSequence(), mesh.indexCount());
		}

		public int vertexCount() {
			return vertexCount;
		}

		public int size() {
			return byteSize;
		}

		public boolean isDeleted() {
			return deleted;
		}

		public boolean isEmpty() {
			return mesh.isEmpty() || isDeleted();
		}

		private void write(long ptr, VertexView vertexView) {
			if (isEmpty()) {
				return;
			}

			vertexView.ptr(ptr + byteIndex);
			vertexView.vertexCount(vertexCount);
			mesh.write(vertexView);
		}

		public void setup(GlVertexArray vao) {
			if (boundTo.add(vao)) {
				vao.bindVertexBuffer(0, InstancedMeshPool.this.vbo.handle(), byteIndex, InternalVertex.STRIDE);
				vao.bindAttributes(0, 0, InternalVertex.ATTRIBUTES);
				vao.setElementBuffer(ebo);
			}
		}

		public void draw(int instanceCount) {
			if (instanceCount > 1) {
				GL32.glDrawElementsInstanced(GlPrimitive.TRIANGLES.glEnum, mesh.indexCount(), GL32.GL_UNSIGNED_INT, 0, instanceCount);
			} else {
				GL32.glDrawElements(GlPrimitive.TRIANGLES.glEnum, mesh.indexCount(), GL32.GL_UNSIGNED_INT, 0);
			}
		}

		public void delete() {
			deleted = true;
			InstancedMeshPool.this.dirty = true;
			InstancedMeshPool.this.anyToRemove = true;
		}
	}
}
