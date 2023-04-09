package com.jozufozu.flywheel.backend.engine.instancing;

import java.util.HashSet;
import java.util.Set;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.InstanceWriter;
import com.jozufozu.flywheel.api.layout.BufferLayout;
import com.jozufozu.flywheel.backend.engine.AbstractInstancer;
import com.jozufozu.flywheel.gl.array.GlVertexArray;
import com.jozufozu.flywheel.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.gl.buffer.GlBufferUsage;
import com.jozufozu.flywheel.gl.buffer.MappedBuffer;

public class GPUInstancer<I extends Instance> extends AbstractInstancer<I> {
	private final BufferLayout instanceFormat;
	private final int instanceStride;

	private final Set<GlVertexArray> boundTo = new HashSet<>();
	private GlBuffer vbo;

	public GPUInstancer(InstanceType<I> type) {
		super(type);
		instanceFormat = type.getLayout();
		instanceStride = instanceFormat.getStride();
	}

	public int getAttributeCount() {
		return instanceFormat.getAttributeCount();
	}

	public boolean isInvalid() {
		return vbo == null;
	}

	public void init() {
		if (vbo != null) {
			return;
		}

		vbo = new GlBuffer(GlBufferType.ARRAY_BUFFER, GlBufferUsage.DYNAMIC_DRAW);
		vbo.setGrowthMargin(instanceStride * 16);
	}

	public void update() {
		removeDeletedInstances();
		ensureBufferCapacity();
		updateBuffer();
	}

	private void ensureBufferCapacity() {
		int count = instances.size();
		int byteSize = instanceStride * count;
		if (vbo.ensureCapacity(byteSize)) {
			// The vbo has moved, so we need to re-bind attributes
			boundTo.clear();
		}
	}

	private void updateBuffer() {
		if (changed.isEmpty()) {
			return;
		}

		int count = instances.size();
		long clearStart = instanceStride * (long) count;
		long clearLength = vbo.getSize() - clearStart;

		try (MappedBuffer buf = vbo.map()) {
			buf.clear(clearStart, clearLength);

			long ptr = buf.getPtr();
			InstanceWriter<I> writer = type.getWriter();

			for (int i = changed.nextSetBit(0); i >= 0 && i < count; i = changed.nextSetBit(i + 1)) {
				writer.write(ptr + instanceStride * i, instances.get(i));
			}

			changed.clear();
		} catch (Exception e) {
			Flywheel.LOGGER.error("Error updating GPUInstancer:", e);
		}
	}

	public void bindToVAO(GlVertexArray vao, int attributeOffset) {
		if (!boundTo.add(vao)) {
			return;
		}

		vao.bindAttributes(vbo, attributeOffset, instanceFormat, 0L);

		for (int i = 0; i < instanceFormat.getAttributeCount(); i++) {
			vao.setAttributeDivisor(attributeOffset + i, 1);
		}
	}

	public void delete() {
		vbo.delete();
		vbo = null;
	}
}
