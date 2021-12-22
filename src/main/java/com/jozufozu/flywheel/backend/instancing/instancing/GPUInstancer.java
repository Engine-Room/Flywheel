package com.jozufozu.flywheel.backend.instancing.instancing;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.struct.Instanced;
import com.jozufozu.flywheel.api.struct.StructWriter;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlVertexArray;
import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;
import com.jozufozu.flywheel.backend.gl.error.GlError;
import com.jozufozu.flywheel.backend.instancing.AbstractInstancer;
import com.jozufozu.flywheel.backend.model.BufferedModel;
import com.jozufozu.flywheel.backend.model.ModelAllocator;
import com.jozufozu.flywheel.core.model.Model;
import com.jozufozu.flywheel.util.AttribUtil;

public class GPUInstancer<D extends InstanceData> extends AbstractInstancer<D> {

	private final ModelAllocator modelAllocator;
	private final BufferLayout instanceFormat;
	private final Instanced<D> instancedType;

	private BufferedModel model;
	private GlVertexArray vao;
	private GlBuffer instanceVBO;
	private int glBufferSize = -1;
	private int glInstanceCount = 0;
	private boolean deleted;
	private boolean initialized;

	protected boolean anyToUpdate;

	public GPUInstancer(Instanced<D> type, Model model, ModelAllocator modelAllocator) {
		super(type::create, model);
		this.modelAllocator = modelAllocator;
		this.instanceFormat = type.getLayout();
		instancedType = type;
	}

	@Override
	public void notifyDirty() {
		anyToUpdate = true;
	}

	public void render() {
		if (invalid()) return;

		vao.bind();
		GlError.pollAndThrow(() -> modelData.name() + "_bind");

		renderSetup();
		GlError.pollAndThrow(() -> modelData.name() + "_setup");

		if (glInstanceCount > 0) {
			model.drawInstances(glInstanceCount);
			GlError.pollAndThrow(() -> modelData.name() + "_draw");
		}

		// persistent mapping sync point
		instanceVBO.doneForThisFrame();
	}

	private boolean invalid() {
		return deleted || model == null;
	}

	public void init() {
		if (isInitialized()) return;

		initialized = true;

		vao = new GlVertexArray();

		model = modelAllocator.alloc(modelData, arenaModel -> {
			vao.bind();

			arenaModel.setupState();
		});

		vao.bind();

		instanceVBO = GlBuffer.requestPersistent(GlBufferType.ARRAY_BUFFER);
		AttribUtil.enableArrays(model.getAttributeCount() + instanceFormat.getAttributeCount());
	}

	public boolean isInitialized() {
		return initialized;
	}

	public boolean isEmpty() {
		return !anyToUpdate && !anyToRemove && glInstanceCount == 0;
	}

	/**
	 * Free acquired resources. All other Instancer methods are undefined behavior after calling delete.
	 */
	public void delete() {
		if (invalid()) return;

		deleted = true;

		model.delete();

		instanceVBO.delete();
		vao.delete();
	}

	protected void renderSetup() {
		if (anyToRemove) {
			removeDeletedInstances();
		}

		instanceVBO.bind();
		if (!realloc()) {

			if (anyToRemove) {
				clearBufferTail();
			}

			if (anyToUpdate) {
				updateBuffer();
			}

			glInstanceCount = data.size();
		}

		instanceVBO.unbind();

		anyToRemove = anyToUpdate = false;
	}

	private void clearBufferTail() {
		int size = data.size();
		final int offset = size * instanceFormat.getStride();
		final int length = glBufferSize - offset;
		if (length > 0) {
			try (MappedBuffer buf = instanceVBO.getBuffer(offset, length)) {
				buf.putByteArray(new byte[length]);
			} catch (Exception e) {
				Flywheel.log.error("Error clearing buffer tail:", e);
			}
		}
	}

	private void updateBuffer() {
		final int size = data.size();

		if (size <= 0) return;

		try (MappedBuffer mapped = instanceVBO.getBuffer(0, glBufferSize)) {

			final StructWriter<D> writer = instancedType.getWriter(mapped);

			for (int i = 0; i < size; i++) {
				final D element = data.get(i);
				if (element.checkDirtyAndClear()) {
					writer.seek(i);
					writer.write(element);
				}
			}
		} catch (Exception e) {
			Flywheel.log.error("Error updating GPUInstancer:", e);
		}
	}

	private boolean realloc() {
		int size = this.data.size();
		int stride = instanceFormat.getStride();
		int requiredSize = size * stride;
		if (requiredSize > glBufferSize) {
			glBufferSize = requiredSize + stride * 16;
			instanceVBO.alloc(glBufferSize);

			try (MappedBuffer buffer = instanceVBO.getBuffer(0, glBufferSize)) {
				StructWriter<D> writer = instancedType.getWriter(buffer);
				for (D datum : data) {
					writer.write(datum);
				}
			} catch (Exception e) {
				Flywheel.log.error("Error reallocating GPUInstancer:", e);
			}

			glInstanceCount = size;

			informAttribDivisors();

			return true;
		}
		return false;
	}

	private void informAttribDivisors() {
		int staticAttributes = model.getAttributeCount();
		instanceFormat.vertexAttribPointers(staticAttributes);

		for (int i = 0; i < instanceFormat.getAttributeCount(); i++) {
			Backend.getInstance().compat.instancedArrays.vertexAttribDivisor(i + staticAttributes, 1);
		}
	}
}
