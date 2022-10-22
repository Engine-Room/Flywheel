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
import com.jozufozu.flywheel.core.model.Model;

public class ModelPool implements ModelAllocator {

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
	public ModelPool(VertexType vertexType) {
		this.vertexType = vertexType;
		int stride = vertexType.getStride();

		vbo = new MappedGlBuffer(GlBufferType.ARRAY_BUFFER);

		// XXX ARRAY_BUFFER is bound and not reset or restored
		vbo.bind();
		vbo.setGrowthMargin(stride * 64);
	}

	/**
	 * Allocate a model in the arena.
	 *
	 * @param model The model to allocate.
	 * @return A handle to the allocated model.
	 */
	@Override
	public PooledModel alloc(Model model, Callback callback) {
		PooledModel bufferedModel = new PooledModel(model, vertices);
		bufferedModel.callback = callback;
		vertices += model.vertexCount();
		models.add(bufferedModel);
		pendingUpload.add(bufferedModel);

		setDirty();
		return bufferedModel;
	}

	public void flush() {
		if (dirty) {
			if (anyToRemove) processDeletions();

			// XXX ARRAY_BUFFER is bound and reset
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

			vertices += model.getVertexCount();
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

				buffer(writer, model);

				vertices += model.getVertexCount();
			}

		} catch (Exception e) {
			Flywheel.LOGGER.error("Error uploading pooled models:", e);
		}
	}

	private void uploadPending() {
		try (MappedBuffer buffer = vbo.getBuffer()) {
			VertexWriter writer = vertexType.createWriter(buffer.unwrap());
			for (PooledModel model : pendingUpload) {
				buffer(writer, model);
			}
			pendingUpload.clear();
		} catch (Exception e) {
			Flywheel.LOGGER.error("Error uploading pooled models:", e);
		}
	}

	private void buffer(VertexWriter writer, PooledModel model) {
		writer.seekToVertex(model.first);
		writer.writeVertexList(model.model.getReader());
		if (model.callback != null) model.callback.onAlloc(model);
	}

	private void setDirty() {
		dirty = true;
	}

	public void delete() {
		vbo.delete();
	}

	public class PooledModel implements BufferedModel {

		private final ElementBuffer ebo;
		private Callback callback;

		private final Model model;
		private int first;

		private boolean remove;

		public PooledModel(Model model, int first) {
			this.model = model;
			this.first = first;
			ebo = model.createEBO();
		}

		@Override
		public VertexType getType() {
			return ModelPool.this.vertexType;
		}

		@Override
		public int getVertexCount() {
			return model.vertexCount();
		}

		@Override
		public void setupState(GlVertexArray vao) {
			// XXX ARRAY_BUFFER is bound and not reset or restored
			vbo.bind();
			vao.enableArrays(getAttributeCount());
			vao.bindAttributes(0, vertexType.getLayout());
			ebo.bind();
		}

		@Override
		public void drawCall() {
			GL32.glDrawElementsBaseVertex(GlPrimitive.TRIANGLES.glEnum, ebo.getElementCount(), ebo.getEboIndexType().asGLType, 0, first);
		}

		@Override
		public void drawInstances(int instanceCount) {
			if (!valid()) return;

			//Backend.log.info(StringUtil.args("drawElementsInstancedBaseVertex", GlPrimitive.TRIANGLES, ebo.elementCount, ebo.eboIndexType, 0, instanceCount, first));

			GL32.glDrawElementsInstancedBaseVertex(GlPrimitive.TRIANGLES.glEnum, ebo.getElementCount(), ebo.getEboIndexType().asGLType, 0, instanceCount, first);
		}

		@Override
		public boolean isDeleted() {
			return false;
		}

		@Override
		public void delete() {
			setDirty();
			anyToRemove = true;
			remove = true;
		}
	}

}
