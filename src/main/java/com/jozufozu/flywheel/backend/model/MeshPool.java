package com.jozufozu.flywheel.backend.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.opengl.GL32;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.api.vertex.VertexWriter;
import com.jozufozu.flywheel.backend.gl.GlPrimitive;
import com.jozufozu.flywheel.backend.gl.GlVertexArray;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.MappedGlBuffer;
import com.jozufozu.flywheel.core.model.Mesh;

public class MeshPool {

	protected final VertexType vertexType;

	private final List<BufferedMesh> models = new ArrayList<>();

	private final List<BufferedMesh> pendingUpload = new ArrayList<>();

	private final GlBuffer vbo;

	private int vertices;

	private boolean dirty;
	private boolean anyToRemove;

	/**
	 * Create a new model pool.
	 *
	 * @param vertexType The vertex type of the models that will be stored in the pool.
	 */
	public MeshPool(VertexType vertexType) {
		this.vertexType = vertexType;
		int stride = vertexType.getStride();

		vbo = new MappedGlBuffer(GlBufferType.ARRAY_BUFFER);

		vbo.setGrowthMargin(stride * 64);
	}

	/**
	 * Allocate a model in the arena.
	 *
	 * @param mesh The model to allocate.
	 * @return A handle to the allocated model.
	 */
	public BufferedMesh alloc(Mesh mesh) {
		BufferedMesh bufferedModel = new BufferedMesh(mesh, vertices);
		vertices += mesh.getVertexCount();
		models.add(bufferedModel);
		pendingUpload.add(bufferedModel);

		dirty = true;
		return bufferedModel;
	}

	public void flush() {
		if (dirty) {
			if (anyToRemove) processDeletions();

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

		// remove deleted models
		models.removeIf(BufferedMesh::isDeleted);

		// re-evaluate first vertex for each model
		int vertices = 0;
		for (BufferedMesh model : models) {
			if (model.first != vertices)
				pendingUpload.add(model);

			model.first = vertices;

			vertices += model.mesh.getVertexCount();
		}

		this.vertices = vertices;
		this.anyToRemove = false;
	}

	/**
	 * Assumes vbo is bound.
	 *
	 * @return true if the buffer was reallocated
	 */
	private boolean realloc() {
		return vbo.ensureCapacity((long) vertices * vertexType.getStride());
	}

	private void uploadAll() {
		try (MappedBuffer buffer = vbo.map()) {
			VertexWriter writer = vertexType.createWriter(buffer.unwrap());

			int vertices = 0;
			for (BufferedMesh model : models) {
				model.first = vertices;

				model.buffer(writer);

				vertices += model.mesh.getVertexCount();
			}

		} catch (Exception e) {
			Flywheel.LOGGER.error("Error uploading pooled models:", e);
		}
	}

	private void uploadPending() {
		try (MappedBuffer buffer = vbo.map()) {
			VertexWriter writer = vertexType.createWriter(buffer.unwrap());
			for (BufferedMesh model : pendingUpload) {
				model.buffer(writer);
			}
			pendingUpload.clear();
		} catch (Exception e) {
			Flywheel.LOGGER.error("Error uploading pooled models:", e);
		}
	}

	public void delete() {
		vbo.delete();
	}

	public class BufferedMesh {

		private final ElementBuffer ebo;
		private final Mesh mesh;
		private int first;

		private boolean deleted;

		private final Set<GlVertexArray> boundTo = new HashSet<>();

		public BufferedMesh(Mesh mesh, int first) {
			this.mesh = mesh;
			this.first = first;
			this.ebo = mesh.createEBO();
		}

		public void drawCall(GlVertexArray vao) {
			attachTo(vao);
			vao.bind();
			this.ebo.bind();
			GL32.glDrawElementsBaseVertex(GlPrimitive.TRIANGLES.glEnum, this.ebo.elementCount, this.ebo.eboIndexType.getGlEnum(), 0, this.first);
		}

		public void drawInstances(GlVertexArray vao, int instanceCount) {
			if (mesh.getVertexCount() <= 0 || isDeleted()) return;

			attachTo(vao);

			vao.bind();
			this.ebo.bind();

			//Backend.log.info(StringUtil.args("drawElementsInstancedBaseVertex", GlPrimitive.TRIANGLES, ebo.elementCount, ebo.eboIndexType, 0, instanceCount, first));

			GL32.glDrawElementsInstancedBaseVertex(GlPrimitive.TRIANGLES.glEnum, this.ebo.elementCount, this.ebo.eboIndexType.getGlEnum(), 0, instanceCount, this.first);
		}

		private void attachTo(GlVertexArray vao) {
			if (this.boundTo.add(vao)) {
				vao.enableArrays(getAttributeCount());
				vao.bindAttributes(MeshPool.this.vbo, 0, MeshPool.this.vertexType.getLayout());
			}
		}

		public boolean isDeleted() {
			return this.deleted;
		}

		public void delete() {
			MeshPool.this.dirty = true;
			MeshPool.this.anyToRemove = true;
			this.deleted = true;
		}

		private void buffer(VertexWriter writer) {
			writer.seekToVertex(this.first);
			writer.writeVertexList(this.mesh.getReader());
			this.boundTo.clear();
		}

		public int getAttributeCount() {
			return MeshPool.this.vertexType.getLayout().getAttributeCount();
		}
	}

}
