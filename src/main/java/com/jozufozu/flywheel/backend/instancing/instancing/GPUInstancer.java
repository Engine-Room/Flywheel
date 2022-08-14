package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.HashSet;
import java.util.Set;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.struct.StructWriter;
import com.jozufozu.flywheel.backend.gl.array.GlVertexArray;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferUsage;
import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.instancing.AbstractInstancer;
import com.jozufozu.flywheel.core.layout.BufferLayout;

public class GPUInstancer<D extends InstancedPart> extends AbstractInstancer<D> {

	public final BufferLayout instanceFormat;
	public final StructType<D> structType;
	public final InstancedModel<D> parent;

	GlBuffer vbo;
	int attributeBaseIndex;
	int glInstanceCount = 0;

	boolean anyToUpdate;

	public GPUInstancer(InstancedModel<D> parent, StructType<D> type) {
		super(type);
		this.parent = parent;
		this.instanceFormat = type.getLayout();
		this.structType = type;
	}

	@Override
	public void notifyDirty() {
		anyToUpdate = true;
	}

	public void init() {
		if (vbo != null) return;

		vbo = new GlBuffer(GlBufferType.ARRAY_BUFFER, GlBufferUsage.DYNAMIC_DRAW);
		vbo.setGrowthMargin(instanceFormat.getStride() * 16);
	}

	public boolean isEmpty() {
		return !anyToUpdate && !anyToRemove && glInstanceCount == 0;
	}

	private final Set<GlVertexArray> boundTo = new HashSet<>();

	void renderSetup(GlVertexArray vao) {
		update();

		if (boundTo.add(vao)) {
			bindInstanceAttributes(vao);
		}
	}

	private void update() {
		if (anyToRemove) {
			removeDeletedInstances();
		}

		if (checkAndGrowBuffer()) {
			// The instance vbo has moved, so we need to re-bind attributes
			boundTo.clear();
		}

		if (anyToUpdate) {
			clearAndUpdateBuffer();
		}

		glInstanceCount = data.size();

		anyToRemove = anyToUpdate = false;
	}

	private void clearAndUpdateBuffer() {
		final int size = data.size();
		final long clearStart = (long) size * instanceFormat.getStride();
		final long clearLength = vbo.getSize() - clearStart;

		try (MappedBuffer buf = vbo.map()) {
			buf.clear(clearStart, clearLength);

			if (size > 0) {
				final long ptr = buf.getPtr();
				final long stride = structType.getLayout().getStride();
				final StructWriter<D> writer = structType.getWriter();

				for (int i = 0; i < size; i++) {
					final D element = data.get(i);
					if (element.checkDirtyAndClear()) {
						writer.write(ptr + i * stride, element);
					}
				}
			}
		} catch (Exception e) {
			Flywheel.LOGGER.error("Error updating GPUInstancer:", e);
		}
	}

	/**
	 * @return {@code true} if the buffer moved.
	 */
	private boolean checkAndGrowBuffer() {
		int size = this.data.size();
		int stride = instanceFormat.getStride();
		int requiredSize = size * stride;

		return vbo.ensureCapacity(requiredSize);
	}

	private void bindInstanceAttributes(GlVertexArray vao) {
		vao.bindAttributes(this.vbo, this.attributeBaseIndex, this.instanceFormat, 0L);

		for (int i = 0; i < this.instanceFormat.getAttributeCount(); i++) {
			vao.setAttributeDivisor(this.attributeBaseIndex + i, 1);
		}
	}

	@Override
	public void delete() {
		vbo.delete();
		vbo = null;
	}
}
