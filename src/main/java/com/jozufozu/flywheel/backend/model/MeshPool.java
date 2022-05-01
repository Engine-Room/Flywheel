package com.jozufozu.flywheel.backend.model;

import java.util.ArrayList;
import java.util.List;

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

public class MeshPool implements MeshAllocator {

	protected final VertexType vertexType;

	private final List<PooledModel> models = new ArrayList<>();

	private final List<PooledModel> pendingUpload = new ArrayList<>();

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

		vbo.bind();
		vbo.setGrowthMargin(stride * 64);
	}

	/**
	 * Allocate a model in the arena.
	 *
	 * @param mesh The model to allocate.
	 * @param vao	The vertex array object to attach the model to.
	 * @return A handle to the allocated model.
	 */
	@Override
	public PooledModel alloc(Mesh mesh, GlVertexArray vao) {
		PooledModel bufferedModel = new PooledModel(vao, mesh, vertices);
		vertices += mesh.vertexCount();
		models.add(bufferedModel);
		pendingUpload.add(bufferedModel);

		setDirty();
		return bufferedModel;
	}

	public void flush() {
		if (dirty) {
			if (anyToRemove) processDeletions();

			vbo.bind();
			if (realloc()) {
				uploadAll();
			} else {
				uploadPending();
			}
			vbo.unbind();

			dirty = false;
			pendingUpload.clear();
		}
	}

	private void processDeletions() {

		// remove deleted models
		models.removeIf(PooledModel::isDeleted);

		// re-evaluate first vertex for each model
		int vertices = 0;
		for (PooledModel model : models) {
			if (model.first != vertices)
				pendingUpload.add(model);

			model.first = vertices;

			vertices += model.mesh.vertexCount();
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
		try (MappedBuffer buffer = vbo.getBuffer()) {
			VertexWriter writer = vertexType.createWriter(buffer.unwrap());

			int vertices = 0;
			for (PooledModel model : models) {
				model.first = vertices;

				model.buffer(writer);

				vertices += model.mesh.vertexCount();
			}

		} catch (Exception e) {
			Flywheel.LOGGER.error("Error uploading pooled models:", e);
		}
	}

	private void uploadPending() {
		try (MappedBuffer buffer = vbo.getBuffer()) {
			VertexWriter writer = vertexType.createWriter(buffer.unwrap());
			for (PooledModel model : pendingUpload) {
				model.buffer(writer);
			}
			pendingUpload.clear();
		} catch (Exception e) {
			Flywheel.LOGGER.error("Error uploading pooled models:", e);
		}
	}

	private void setDirty() {
		dirty = true;
	}

	public void delete() {
		vbo.delete();
	}

	public class PooledModel implements BufferedModel {

		private final ElementBuffer ebo;
		private final GlVertexArray vao;

		private final Mesh mesh;
		private int first;

		private boolean deleted;

		public PooledModel(GlVertexArray vao, Mesh mesh, int first) {
			this.vao = vao;
			this.mesh = mesh;
			this.first = first;
			ebo = mesh.createEBO();
		}

		@Override
		public VertexType getType() {
			return MeshPool.this.vertexType;
		}

		@Override
		public int getVertexCount() {
			return mesh.vertexCount();
		}

		@Override
		public void setupState(GlVertexArray vao) {
			vbo.bind();
			vao.enableArrays(getAttributeCount());
			vao.bindAttributes(0, vertexType.getLayout());
		}

		@Override
		public void drawCall() {
			GL32.glDrawElementsBaseVertex(GlPrimitive.TRIANGLES.glEnum, ebo.elementCount, ebo.eboIndexType.getGlEnum(), 0, first);
		}

		@Override
		public void drawInstances(int instanceCount) {
			if (!valid()) return;

			ebo.bind();

			//Backend.log.info(StringUtil.args("drawElementsInstancedBaseVertex", GlPrimitive.TRIANGLES, ebo.elementCount, ebo.eboIndexType, 0, instanceCount, first));

			GL32.glDrawElementsInstancedBaseVertex(GlPrimitive.TRIANGLES.glEnum, ebo.elementCount, ebo.eboIndexType.getGlEnum(), 0, instanceCount, first);
		}

		@Override
		public boolean isDeleted() {
			return deleted;
		}

		@Override
		public void delete() {
			setDirty();
			anyToRemove = true;
			deleted = true;
		}

		private void buffer(VertexWriter writer) {
			writer.seekToVertex(first);
			writer.writeVertexList(mesh.getReader());

			setupState(vao);
		}
	}

}
