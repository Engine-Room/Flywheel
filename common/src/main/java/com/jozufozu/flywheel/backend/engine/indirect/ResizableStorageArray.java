package com.jozufozu.flywheel.backend.engine.indirect;

import com.jozufozu.flywheel.lib.math.MoreMath;

/**
 * A buffer that is aware of its content's stride with some control over how it grows.
 */
public class ResizableStorageArray {
	private static final double DEFAULT_GROWTH_FACTOR = 1.25;
	private final ResizableStorageBuffer buffer;
	private final long stride;
	private final double growthFactor;

	private long capacity;

	public ResizableStorageArray(long stride) {
		this(stride, DEFAULT_GROWTH_FACTOR);
	}

	public ResizableStorageArray(long stride, double growthFactor) {
		this.stride = stride;
		this.growthFactor = growthFactor;

		if (stride <= 0) {
			throw new IllegalArgumentException("Stride must be positive!");
		}

		if (growthFactor <= 1) {
			throw new IllegalArgumentException("Growth factor must be greater than 1!");
		}

		this.buffer = new ResizableStorageBuffer();
	}

	public int handle() {
		return buffer.handle();
	}

	public long stride() {
		return stride;
	}

	public long capacity() {
		return capacity;
	}

	public long byteCapacity() {
		return buffer.capacity();
	}

	public void ensureCapacity(long capacity) {
		if (capacity > this.capacity) {
			long newCapacity = grow(capacity);
			buffer.ensureCapacity(stride * newCapacity);
			this.capacity = newCapacity;
		}
	}

	public void delete() {
		buffer.delete();
	}

	private long grow(long capacity) {
		return MoreMath.ceilLong(capacity * growthFactor);
	}
}
