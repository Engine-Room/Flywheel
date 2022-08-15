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

	@Override
	void freeInner() {
		super.freeInner();
		cleaningAction.freed = true;
		cleanable.clean();
	}

	static MemoryBlock mallocBlockTracked(long size) {
		MemoryBlock block = new TrackedMemoryBlockImpl(FlwMemoryTracker.malloc(size), size, FlwMemoryTracker.CLEANER);
		FlwMemoryTracker._allocCPUMemory(block.size());
		return block;
	}

	static MemoryBlock callocBlockTracked(long num, long size) {
		MemoryBlock block = new TrackedMemoryBlockImpl(FlwMemoryTracker.calloc(num, size), num * size, FlwMemoryTracker.CLEANER);
		FlwMemoryTracker._allocCPUMemory(block.size());
		return block;
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
