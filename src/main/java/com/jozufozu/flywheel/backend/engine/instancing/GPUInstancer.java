package com.jozufozu.flywheel.backend.engine.instancing;

import java.util.HashSet;
import java.util.Set;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.instancer.InstancePart;
import com.jozufozu.flywheel.api.layout.BufferLayout;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.struct.StructWriter;
import com.jozufozu.flywheel.backend.engine.AbstractInstancer;
import com.jozufozu.flywheel.gl.array.GlVertexArray;
import com.jozufozu.flywheel.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.gl.buffer.GlBufferUsage;
import com.jozufozu.flywheel.gl.buffer.MappedBuffer;

public class GPUInstancer<P extends InstancePart> extends AbstractInstancer<P> {

	final BufferLayout instanceFormat;
	final Set<GlVertexArray> boundTo = new HashSet<>();
	GlBuffer vbo;
	int glInstanceCount = 0;

	boolean anyToUpdate;

	public GPUInstancer(StructType<P> type) {
		super(type);
		this.instanceFormat = type.getLayout();
	}

	public void init() {
		if (vbo != null) {
			return;
		}

		vbo = new GlBuffer(GlBufferType.ARRAY_BUFFER, GlBufferUsage.DYNAMIC_DRAW);
		vbo.setGrowthMargin(instanceFormat.getStride() * 16);
	}

	public boolean isEmpty() {
		return deleted.isEmpty() && changed.isEmpty() && glInstanceCount == 0;
	}

	void update() {
		if (!deleted.isEmpty()) {
			removeDeletedInstances();
		}

		if (checkAndGrowBuffer()) {
			// The instance vbo has moved, so we need to re-bind attributes
			boundTo.clear();
		}

		if (!changed.isEmpty()) {
			clearAndUpdateBuffer();
		}

		glInstanceCount = data.size();
	}

	private void clearAndUpdateBuffer() {
		final int size = data.size();
		final long clearStart = (long) size * instanceFormat.getStride();
		final long clearLength = vbo.getSize() - clearStart;

		try (MappedBuffer buf = vbo.map()) {
			buf.clear(clearStart, clearLength);

			if (size > 0) {
				final long ptr = buf.getPtr();
				final long stride = type.getLayout()
						.getStride();
				final StructWriter<P> writer = type.getWriter();

				for (int i = changed.nextSetBit(0); i >= 0 && i < size; i = changed.nextSetBit(i + 1)) {
					writer.write(ptr + i * stride, data.get(i));
				}
				changed.clear();
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

	public void delete() {
		vbo.delete();
		vbo = null;
	}
}
