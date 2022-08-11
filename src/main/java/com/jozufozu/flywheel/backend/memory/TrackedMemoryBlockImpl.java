package com.jozufozu.flywheel.backend.memory;

import java.lang.ref.Cleaner;

final class TrackedMemoryBlockImpl extends MemoryBlockImpl {
	final CleaningAction cleaningAction;
	final Cleaner.Cleanable cleanable;

	TrackedMemoryBlockImpl(long ptr, long size, Cleaner cleaner) {
		super(ptr, size);
		cleaningAction = new CleaningAction(ptr, size);
		cleanable = cleaner.register(this, cleaningAction);
	}

	@Override
	public boolean isTracked() {
		return true;
	}

	void freeInner() {
		freed = true;
		cleaningAction.freed = true;
		cleanable.clean();
	}

	@Override
	public MemoryBlock realloc(long size) {
		MemoryBlock block = FlwMemoryTracker.reallocBlock(this, size);
		freeInner();
		return block;
	}

	@Override
	public MemoryBlock reallocTracked(long size) {
		MemoryBlock block = FlwMemoryTracker.reallocBlockTracked(this, size);
		freeInner();
		return block;
	}

	@Override
	public void free() {
		FlwMemoryTracker.freeBlock(this);
		freeInner();
	}

	static class CleaningAction implements Runnable {
		final long ptr;
		final long size;

		boolean freed;

		CleaningAction(long ptr, long size) {
			this.ptr = ptr;
			this.size = size;
		}

		@Override
		public void run() {
			if (!freed) {
				FlwMemoryTracker.free(ptr);
				FlwMemoryTracker._freeCPUMemory(size);
			}
		}
	}
}
