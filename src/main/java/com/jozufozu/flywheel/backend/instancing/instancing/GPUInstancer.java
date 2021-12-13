package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.BitSet;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlVertexArray;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;
import com.jozufozu.flywheel.backend.gl.error.GlError;
import com.jozufozu.flywheel.backend.instancing.AbstractInstancer;
import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.backend.model.IBufferedModel;
import com.jozufozu.flywheel.backend.model.ModelAllocator;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.struct.StructWriter;
import com.jozufozu.flywheel.core.model.Model;
import com.jozufozu.flywheel.util.AttribUtil;

public class GPUInstancer<D extends InstanceData> extends AbstractInstancer<D> {

	private final ModelAllocator modelAllocator;
	private final VertexFormat instanceFormat;

	private IBufferedModel model;
	private GlVertexArray vao;
	private GlBuffer instanceVBO;
	private int glBufferSize = -1;
	private int glInstanceCount = 0;
	private boolean deleted;
	private boolean initialized;

	protected boolean anyToUpdate;

	public GPUInstancer(StructType<D> type, Model model, ModelAllocator modelAllocator) {
		super(type, model);
		this.modelAllocator = modelAllocator;
		this.instanceFormat = type.format();
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

			model.setupState();
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

		final int stride = instanceFormat.getStride();
		final BitSet dirtySet = getDirtyBitSet();

		if (dirtySet.isEmpty()) return;

		final int firstDirty = dirtySet.nextSetBit(0);
		final int lastDirty = dirtySet.previousSetBit(size);

		final int offset = firstDirty * stride;
		final int length = (1 + lastDirty - firstDirty) * stride;

		if (length > 0) {
			try (MappedBuffer mapped = instanceVBO.getBuffer(offset, length)) {

				StructWriter<D> writer = type.asInstanced()
						.getWriter(mapped);

				dirtySet.stream()
						.forEach(i -> {
							writer.seek(i);
							writer.write(data.get(i));
						});
			} catch (Exception e) {
				Flywheel.log.error("Error updating GPUInstancer:", e);
			}
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
				StructWriter<D> writer = type.asInstanced()
						.getWriter(buffer);
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
