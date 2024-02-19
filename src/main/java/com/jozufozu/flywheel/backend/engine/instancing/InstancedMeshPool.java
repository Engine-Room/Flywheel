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
	private final Map<Mesh, BufferedMesh> byMesh = new HashMap<>();
	private final List<BufferedMesh> ordered = new ArrayList<>();

	private final GlBuffer vbo;
	private final EboCache eboCache;
	private long byteSize;

	private boolean dirty;
	private boolean anyToRemove;

	/**
	 * Create a new mesh pool.
	 */
	public InstancedMeshPool(EboCache eboCache) {
		this.eboCache = eboCache;
		vertexView = InternalVertex.createVertexView();
        vbo = new GlBuffer();
		vbo.growthFunction(l -> Math.max(l + InternalVertex.STRIDE * 128L, (long) (l * 1.6)));
	}

	/**
	 * Allocate a mesh in the arena.
	 *
	 * @param mesh     The mesh to allocate.
	 * @return A handle to the allocated mesh.
	 */
	public BufferedMesh alloc(Mesh mesh) {
		return byMesh.computeIfAbsent(mesh, this::_alloc);
	}

	private BufferedMesh _alloc(Mesh m) {
		BufferedMesh bufferedMesh = new BufferedMesh(m, this.eboCache);
		ordered.add(bufferedMesh);

		dirty = true;
		return bufferedMesh;
	}

	@Nullable
	public BufferedMesh get(Mesh mesh) {
		return byMesh.get(mesh);
	}

	public void flush() {
		if (!dirty) {
			return;
		}

		if (anyToRemove) {
			anyToRemove = false;
			processDeletions();
		}

		var forUpload = calculateByteSizeAndGetMeshesForUpload();

		if (!forUpload.isEmpty()) {
			vbo.ensureCapacity(byteSize);

			upload(forUpload);
		}

		dirty = false;
	}

	private void processDeletions() {
		// remove deleted meshes
		ordered.removeIf(bufferedMesh -> {
			boolean deleted = bufferedMesh.deleted();
			if (deleted) {
				byMesh.remove(bufferedMesh.mesh);
			}
			return deleted;
		});
	}

	private List<BufferedMesh> calculateByteSizeAndGetMeshesForUpload() {
		List<BufferedMesh> out = new ArrayList<>();

		long byteIndex = 0;
		for (BufferedMesh mesh : ordered) {
			if (mesh.byteIndex != byteIndex) {
				out.add(mesh);
			}

			mesh.byteIndex = byteIndex;

			byteIndex += mesh.byteSize;
		}

		this.byteSize = byteIndex;

		return out;
	}

	private void upload(List<BufferedMesh> meshes) {
		try (MappedBuffer mapped = vbo.map()) {
			long ptr = mapped.ptr();

			for (BufferedMesh mesh : meshes) {
				mesh.write(ptr, vertexView);
				mesh.boundTo.clear();
			}
		} catch (Exception e) {
			Flywheel.LOGGER.error("Error uploading pooled meshes:", e);
		}
	}

	public void delete() {
		vbo.delete();
		byMesh.clear();
		ordered.clear();
	}

	@Override
	public String toString() {
		return "InstancedMeshPool{" + "byteSize=" + byteSize + ", meshCount=" + byMesh.size() + '}';
	}

	public class BufferedMesh {
		private final Mesh mesh;
		private final int vertexCount;
		private final int byteSize;
		private final int ebo;

		private long byteIndex = -1;
		private int referenceCount = 0;

		private final Set<GlVertexArray> boundTo = new HashSet<>();

		private BufferedMesh(Mesh mesh, EboCache eboCache) {
			this.mesh = mesh;
			vertexCount = mesh.vertexCount();
			byteSize = vertexCount * InternalVertex.STRIDE;
			this.ebo = eboCache.get(mesh.indexSequence(), mesh.indexCount());
		}

		public boolean deleted() {
			return referenceCount <= 0;
		}

		public boolean invalid() {
			return mesh.vertexCount() == 0 || deleted() || byteIndex == -1;
		}

		private void write(long ptr, VertexView vertexView) {
			if (invalid()) {
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

		public void acquire() {
			referenceCount++;
		}

		public void drop() {
			if (--referenceCount == 0) {
				InstancedMeshPool.this.dirty = true;
				InstancedMeshPool.this.anyToRemove = true;
			}
		}
	}
}
