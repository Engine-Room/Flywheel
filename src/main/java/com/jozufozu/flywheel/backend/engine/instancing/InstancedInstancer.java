package com.jozufozu.flywheel.backend.engine.instancing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.InstanceWriter;
import com.jozufozu.flywheel.backend.engine.AbstractInstancer;
import com.jozufozu.flywheel.backend.engine.LayoutAttributes;
import com.jozufozu.flywheel.backend.gl.array.GlVertexArray;
import com.jozufozu.flywheel.backend.gl.array.VertexAttribute;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferUsage;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;

public class InstancedInstancer<I extends Instance> extends AbstractInstancer<I> {
	private final List<VertexAttribute> instanceAttributes;
	private final int instanceStride;

	private final Set<GlVertexArray> boundTo = new HashSet<>();
	private final InstanceWriter<I> writer;
	@Nullable
	private GlBuffer vbo;

	private final List<DrawCall> drawCalls = new ArrayList<>();

	public InstancedInstancer(InstanceType<I> type, Context context) {
		super(type, context);
		var layout = type.layout();
		instanceAttributes = LayoutAttributes.attributes(layout);
		instanceStride = layout.byteSize();
		writer = type.writer();
	}

	public void init() {
		if (vbo != null) {
			return;
		}

		vbo = new GlBuffer(GlBufferUsage.DYNAMIC_DRAW);
	}

	public void update() {
		removeDeletedInstances();
		updateBuffer();
	}

	private void updateBuffer() {
		if (changed.isEmpty() || vbo == null) {
			return;
		}

		int byteSize = instanceStride * instances.size();
		if (needsToGrow(byteSize)) {
			// TODO: Should this memory block be persistent?
			var temp = MemoryBlock.malloc(increaseSize(byteSize));

			writeAll(temp.ptr());

			vbo.upload(temp);

			temp.free();
		} else {
			writeChanged();
		}

		changed.clear();
	}

	private void writeChanged() {
		changed.forEachSetSpan((startInclusive, endInclusive) -> {
			var temp = MemoryBlock.malloc((long) instanceStride * (endInclusive - startInclusive + 1));
			long ptr = temp.ptr();
			for (int i = startInclusive; i <= endInclusive; i++) {
				writer.write(ptr, instances.get(i));
				ptr += instanceStride;
			}

			vbo.uploadSpan((long) startInclusive * instanceStride, temp);

			temp.free();
		});
	}

	private void writeAll(long ptr) {
		for (I instance : instances) {
			writer.write(ptr, instance);
			ptr += instanceStride;
		}
	}

	private long increaseSize(long capacity) {
		return Math.max(capacity + (long) instanceStride * 16, (long) (capacity * 1.6));
	}

	public boolean needsToGrow(long capacity) {
		if (capacity < 0) {
			throw new IllegalArgumentException("Size " + capacity + " < 0");
		}

		if (capacity == 0) {
			return false;
		}

        return capacity > vbo.size();
    }


	/**
	 * Bind this instancer's vbo to the given vao if it hasn't already been bound.
	 * @param vao The vao to bind to.
	 * @param startAttrib The first attribute to bind. This method will bind attributes in the half open range
	 * 		{@code [startAttrib, startAttrib + instanceFormat.getAttributeCount())}.
	 */
	public void bindIfNeeded(GlVertexArray vao, int startAttrib) {
		if (!boundTo.add(vao)) {
			return;
		}

		bindRaw(vao, startAttrib, 0);
	}

	/**
	 * Bind this instancer's vbo to the given vao with the given base instance to calculate the binding offset.
	 * @param vao The vao to bind to.
	 * @param startAttrib The first attribute to bind. This method will bind attributes in the half open range
	 * 		{@code [startAttrib, startAttrib + instanceFormat.getAttributeCount())}.
	 * @param baseInstance The base instance to calculate the binding offset from.
	 */
	public void bindRaw(GlVertexArray vao, int startAttrib, int baseInstance) {
		long offset = (long) baseInstance * instanceStride;
		vao.bindVertexBuffer(1, vbo.handle(), offset, instanceStride);
		vao.setBindingDivisor(1, 1);
		vao.bindAttributes(1, startAttrib, instanceAttributes);
	}

	public void delete() {
		if (vbo == null) {
			return;
		}
		vbo.delete();
		vbo = null;

		for (DrawCall drawCall : drawCalls) {
			drawCall.delete();
		}
	}

	public void addDrawCall(DrawCall drawCall) {
		drawCalls.add(drawCall);
	}

	public List<DrawCall> drawCalls() {
		return drawCalls;
	}
}
