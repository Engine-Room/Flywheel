package com.jozufozu.flywheel.backend.memory;

import java.lang.ref.Cleaner;

final class TrackedMemoryBlockImpl extends MemoryBlockImpl {
	Cleaner.Cleanable cleanable;

	TrackedMemoryBlockImpl(long ptr, long size) {
		super(ptr, size);
	}

	@Override
	public boolean isTracked() {
		return true;
	}

	@Override
	public MemoryBlock realloc(long size) {
		MemoryBlock block = FlwMemoryTracker.reallocBlock(this, size);
		freed = true;
		cleanable.clean();
		return block;
	}

	@Override
	public MemoryBlock reallocTracked(long size) {
		MemoryBlock block = FlwMemoryTracker.reallocBlockTracked(this, size);
		freed = true;
		cleanable.clean();
		return block;
	}

	@Override
	public void free() {
		cleanable.clean();
		freed = true;
	}
}
