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
import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.core.model.Mesh;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;

public class MeshPool {

	private static MeshPool allocator;

	public static MeshPool getInstance() {
		if (allocator == null) {
			allocator = new MeshPool();
		}
		return allocator;
	}

	public static void reset(ReloadRenderersEvent ignored) {
		if (allocator != null) {
			allocator.delete();
			allocator = null;
		}
	}

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
	public MeshPool() {
		vbo = new GlBuffer(GlBufferType.ARRAY_BUFFER);

		vbo.setGrowthMargin(2048);
	}

	/**
	 * Allocate a model in the arena.
	 *
	 * @param mesh The model to allocate.
	 * @return A handle to the allocated model.
	 */
	public BufferedMesh alloc(Mesh mesh) {
		return meshes.computeIfAbsent(mesh, m -> {
			BufferedMesh bufferedModel = new BufferedMesh(m, byteSize);
			byteSize += m.size();
			allBuffered.add(bufferedModel);
			pendingUpload.add(bufferedModel);

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

		// re-evaluate first vertex for each model
		int byteIndex = 0;
		for (BufferedMesh model : allBuffered) {
			if (model.byteIndex != byteIndex) {
				pendingUpload.add(model);
			}

			model.byteIndex = byteIndex;

			byteIndex += model.mesh.size();
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
			for (BufferedMesh model : allBuffered) {
				model.byteIndex = byteIndex;

				model.buffer(ptr);

				byteIndex += model.mesh.size();
			}

		} catch (Exception e) {
			Flywheel.LOGGER.error("Error uploading pooled meshes:", e);
		}
	}

	private void uploadPending() {
		try (MappedBuffer mapped = vbo.map()) {
			long buffer = mapped.getPtr();
			for (BufferedMesh model : pendingUpload) {
				model.buffer(buffer);
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

	public class BufferedMesh {

		private final ElementBuffer ebo;
		private final Mesh mesh;
		private final BufferLayout layout;
		private long byteIndex;

		private boolean deleted;

		private boolean gpuResident = false;

		private final Set<GlVertexArray> boundTo = new HashSet<>();

		public BufferedMesh(Mesh mesh, long byteIndex) {
			this.mesh = mesh;
			this.byteIndex = byteIndex;
			this.ebo = mesh.createEBO();
			this.layout = mesh.getVertexType()
					.getLayout();
		}

		public void drawCall(GlVertexArray vao) {
			drawInstances(vao, 1);
		}

		public void drawInstances(GlVertexArray vao, int instanceCount) {
			if (hasAnythingToRender()) return;

			setup(vao);

			draw(instanceCount);
		}

		private boolean hasAnythingToRender() {
			return mesh.isEmpty() || isDeleted();
		}

		private void draw(int instanceCount) {
			if (instanceCount > 1) {
				GL32.glDrawElementsInstanced(GlPrimitive.TRIANGLES.glEnum, this.ebo.elementCount, this.ebo.eboIndexType.getGlEnum(), 0, instanceCount);
			} else {
				GL32.glDrawElements(GlPrimitive.TRIANGLES.glEnum, this.ebo.elementCount, this.ebo.eboIndexType.getGlEnum(), 0);
			}
		}

		private void setup(GlVertexArray vao) {
			if (this.boundTo.add(vao)) {
				vao.enableArrays(getAttributeCount());
				vao.bindAttributes(MeshPool.this.vbo, 0, this.layout, this.byteIndex);
			}
			vao.bindElementArray(this.ebo.buffer);
			vao.bind();
		}

		public boolean isDeleted() {
			return this.deleted;
		}

		public void delete() {
			MeshPool.this.dirty = true;
			MeshPool.this.anyToRemove = true;
			this.deleted = true;
		}

		private void buffer(long ptr) {
			this.mesh.write(ptr + byteIndex);

			this.boundTo.clear();
			this.gpuResident = true;
		}

		public int getAttributeCount() {
			return this.layout.getAttributeCount();
		}

		public boolean isGpuResident() {
			return gpuResident;
		}

		public VertexType getVertexType() {
			return this.mesh.getVertexType();
		}
	}

}
