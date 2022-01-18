package com.jozufozu.flywheel.backend.instancing.instancing;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.struct.Instanced;
import com.jozufozu.flywheel.api.struct.StructWriter;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlVertexArray;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;
import com.jozufozu.flywheel.backend.instancing.AbstractInstancer;
import com.jozufozu.flywheel.backend.model.BufferedModel;
import com.jozufozu.flywheel.backend.model.ModelAllocator;
import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.core.model.Model;

public class GPUInstancer<D extends InstanceData> extends AbstractInstancer<D> {

	private final ModelAllocator modelAllocator;
	private final BufferLayout instanceFormat;
	private final Instanced<D> instancedType;

	private BufferedModel model;
	private GlVertexArray vao;
	private GlBuffer instanceVBO;
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

		renderSetup();

		if (glInstanceCount > 0) {
			model.drawInstances(glInstanceCount);
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

			arenaModel.setupState(vao);
		});

		vao.bind();
		vao.enableArrays(model.getAttributeCount() + instanceFormat.getAttributeCount());

		instanceVBO = GlBuffer.requestPersistent(GlBufferType.ARRAY_BUFFER);
		instanceVBO.setGrowthMargin(instanceFormat.getStride() * 16);
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
		final long length = instanceVBO.getCapacity() - offset;
		if (length > 0) {
			try (MappedBuffer buf = instanceVBO.getBuffer(offset, length)) {
				MemoryUtil.memSet(MemoryUtil.memAddress(buf.unwrap()), 0, length);
			} catch (Exception e) {
				Flywheel.LOGGER.error("Error clearing buffer tail:", e);
			}
		}
	}

	private void updateBuffer() {
		final int size = data.size();

		if (size <= 0) return;

		try (MappedBuffer mapped = instanceVBO.getBuffer()) {

			final StructWriter<D> writer = instancedType.getWriter(mapped);

			boolean sequential = true;
			for (int i = 0; i < size; i++) {
				final D element = data.get(i);
				if (element.checkDirtyAndClear()) {
					if (!sequential) {
						writer.seek(i);
					}
					writer.write(element);
					sequential = true;
				} else {
					sequential = false;
				}
			}
		} catch (Exception e) {
			Flywheel.LOGGER.error("Error updating GPUInstancer:", e);
		}
	}

	private boolean realloc() {
		int size = this.data.size();
		int stride = instanceFormat.getStride();
		int requiredSize = size * stride;
		if (instanceVBO.ensureCapacity(requiredSize)) {

			try (MappedBuffer buffer = instanceVBO.getBuffer()) {
				StructWriter<D> writer = instancedType.getWriter(buffer);
				for (D datum : data) {
					writer.write(datum);
				}
			} catch (Exception e) {
				Flywheel.LOGGER.error("Error reallocating GPUInstancer:", e);
			}

			glInstanceCount = size;

			bindInstanceAttributes();

			return true;
		}
		return false;
	}

	private void bindInstanceAttributes() {
		int attributeBaseIndex = model.getAttributeCount();
		vao.bindAttributes(attributeBaseIndex, instanceFormat);

		for (int i = 0; i < instanceFormat.getAttributeCount(); i++) {
			Backend.compat.instancedArrays.vertexAttribDivisor(attributeBaseIndex + i, 1);
		}
	}
}
