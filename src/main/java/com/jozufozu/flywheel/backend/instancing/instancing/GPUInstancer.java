package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.HashSet;
import java.util.Set;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.struct.InstancedStructType;
import com.jozufozu.flywheel.api.struct.StructWriter;
import com.jozufozu.flywheel.backend.gl.GlVertexArray;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;
import com.jozufozu.flywheel.backend.instancing.AbstractInstancer;
import com.jozufozu.flywheel.core.layout.BufferLayout;

public class GPUInstancer<D extends InstanceData> extends AbstractInstancer<D> {

	final BufferLayout instanceFormat;
	final InstancedStructType<D> instancedType;

	GlBuffer vbo;
	int attributeBaseIndex;
	int glInstanceCount = 0;

	boolean anyToUpdate;

	public GPUInstancer(InstancedStructType<D> type) {
		super(type::create);
		this.instanceFormat = type.getLayout();
		instancedType = type;
	}

	@Override
	public void notifyDirty() {
		anyToUpdate = true;
	}

	public void init() {
		if (vbo != null) return;

		vbo = GlBuffer.requestPersistent(GlBufferType.ARRAY_BUFFER);
		vbo.setGrowthMargin(instanceFormat.getStride() * 16);
	}

	public boolean isEmpty() {
		return !anyToUpdate && !anyToRemove && glInstanceCount == 0;
	}

	private final Set<GlVertexArray> boundTo = new HashSet<>();

	void renderSetup(GlVertexArray vao) {
		if (anyToRemove) {
			removeDeletedInstances();
		}

		vbo.bind();

		if (!realloc()) {

			boundTo.clear();

			if (anyToRemove) {
				clearBufferTail();
			}

			if (anyToUpdate) {
				updateBuffer();
			}

			glInstanceCount = data.size();
		}

		if (boundTo.add(vao)) {
			bindInstanceAttributes(vao);
		}

		vbo.unbind();

		anyToRemove = anyToUpdate = false;
	}

	private void clearBufferTail() {
		int size = data.size();
		final int offset = size * instanceFormat.getStride();
		final long length = vbo.getCapacity() - offset;
		if (length > 0) {
			try (MappedBuffer buf = vbo.getBuffer(offset, length)) {
				MemoryUtil.memSet(MemoryUtil.memAddress(buf.unwrap()), 0, length);
			} catch (Exception e) {
				Flywheel.LOGGER.error("Error clearing buffer tail:", e);
			}
		}
	}

	private void updateBuffer() {
		final int size = data.size();

		if (size <= 0) return;

		try (MappedBuffer mapped = vbo.getBuffer()) {

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
		if (vbo.ensureCapacity(requiredSize)) {

			try (MappedBuffer buffer = vbo.getBuffer()) {
				StructWriter<D> writer = instancedType.getWriter(buffer);
				for (D datum : data) {
					writer.write(datum);
				}
			} catch (Exception e) {
				Flywheel.LOGGER.error("Error reallocating GPUInstancer:", e);
			}

			glInstanceCount = size;

			return true;
		}
		return false;
	}

	private void bindInstanceAttributes(GlVertexArray vao) {
		vao.bindAttributes(attributeBaseIndex, instanceFormat);

		for (int i = 0; i < instanceFormat.getAttributeCount(); i++) {
			vao.setAttributeDivisor(attributeBaseIndex + i, 1);
		}
	}
}
