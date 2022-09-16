package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL32;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.GlPrimitive;
import com.jozufozu.flywheel.backend.gl.array.GlVertexArray;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;
import com.jozufozu.flywheel.core.model.Mesh;

public class InstancedMeshPool {
	private final VertexType vertexType;

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
	public InstancedMeshPool(VertexType vertexType) {
		this.vertexType = vertexType;
		int stride = vertexType.getStride();
		this.vbo = new GlBuffer(GlBufferType.ARRAY_BUFFER);

		this.vbo.setGrowthMargin(stride * 32);
	}

	/**
	 * Allocate a mesh in the arena.
	 *
	 * @param mesh The mesh to allocate.
	 * @return A handle to the allocated mesh.
	 */
	public BufferedMesh alloc(Mesh mesh) {
		return meshes.computeIfAbsent(mesh, m -> {
			if (m.getVertexType() != vertexType) {
				throw new IllegalArgumentException("Mesh has wrong vertex type");
			}

			BufferedMesh bufferedMesh = new BufferedMesh(m, byteSize);
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

			if (realloc()) {
				uploadAll();
			} else {
				uploadPending();
			}

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

	/**
	 * Assumes vbo is bound.
	 *
	 * @return true if the buffer was reallocated
	 */
	private boolean realloc() {
		return vbo.ensureCapacity(byteSize);
	}

	private void uploadAll() {
		try (MappedBuffer mapped = vbo.map()) {
			long ptr = mapped.getPtr();

			int byteIndex = 0;
			for (BufferedMesh mesh : allBuffered) {
				mesh.byteIndex = byteIndex;

				mesh.buffer(ptr);

				byteIndex += mesh.size();
			}
		} catch (Exception e) {
			Flywheel.LOGGER.error("Error uploading pooled meshes:", e);
		}
	}

	private void uploadPending() {
		try (MappedBuffer mapped = vbo.map()) {
			long ptr = mapped.getPtr();

			for (BufferedMesh mesh : pendingUpload) {
				mesh.buffer(ptr);
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

	public VertexType getVertexType() {
		return vertexType;
	}

	@Override
	public String toString() {
		return "InstancedMeshPool{" + "vertexType=" + vertexType + ", byteSize=" + byteSize + ", meshCount=" + meshes.size() + '}';
	}

	public class BufferedMesh {
		private final Mesh mesh;
		private final ElementBuffer ebo;
		private long byteIndex;
		private boolean deleted;

		private final Set<GlVertexArray> boundTo = new HashSet<>();

		private BufferedMesh(Mesh mesh, long byteIndex) {
			this.mesh = mesh;
			this.byteIndex = byteIndex;
			this.ebo = mesh.createEBO();
		}

		private void buffer(long ptr) {
			mesh.write(ptr + byteIndex);

			boundTo.clear();
		}

		public void drawCall(GlVertexArray vao) {
			drawInstances(vao, 1);
		}

		public void drawInstances(GlVertexArray vao, int instanceCount) {
			if (isEmpty()) {
				return;
			}

			setup(vao);

			draw(instanceCount);
		}

		private boolean isEmpty() {
			return mesh.isEmpty() || isDeleted();
		}

		private void setup(GlVertexArray vao) {
			if (boundTo.add(vao)) {
				vao.enableArrays(getAttributeCount());
				vao.bindAttributes(InstancedMeshPool.this.vbo, 0, vertexType.getLayout(), byteIndex);
			}
			vao.bindElementArray(ebo.buffer);
			vao.bind();
		}

		private void draw(int instanceCount) {
			if (instanceCount > 1) {
				GL32.glDrawElementsInstanced(GlPrimitive.TRIANGLES.glEnum, ebo.elementCount, ebo.eboIndexType.getGlEnum(), 0, instanceCount);
			} else {
				GL32.glDrawElements(GlPrimitive.TRIANGLES.glEnum, ebo.elementCount, ebo.eboIndexType.getGlEnum(), 0);
			}
		}

		public void delete() {
			deleted = true;
			InstancedMeshPool.this.dirty = true;
			InstancedMeshPool.this.anyToRemove = true;
		}

		public Mesh getMesh() {
			return mesh;
		}

		public int size() {
			return mesh.size();
		}

		public VertexType getVertexType() {
			return vertexType;
		}

		public int getAttributeCount() {
			return vertexType.getLayout().getAttributeCount();
		}

		public boolean isDeleted() {
			return deleted;
		}
	}
}
