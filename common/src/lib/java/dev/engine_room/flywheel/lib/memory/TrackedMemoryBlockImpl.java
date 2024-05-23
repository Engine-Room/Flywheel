package dev.engine_room.flywheel.lib.memory;

import java.lang.ref.Cleaner;

class TrackedMemoryBlockImpl extends AbstractMemoryBlockImpl {
	final Cleaner cleaner;
	final CleaningAction cleaningAction;
	final Cleaner.Cleanable cleanable;

	TrackedMemoryBlockImpl(long ptr, long size, Cleaner cleaner) {
		super(ptr, size);
		this.cleaner = cleaner;
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

	@Override
	public MemoryBlock realloc(long size) {
		MemoryBlock block = new TrackedMemoryBlockImpl(FlwMemoryTracker.realloc(ptr, size), size, cleaner);
		FlwMemoryTracker._allocCPUMemory(block.size());
		freeInner();
		return block;
	}

	static MemoryBlock malloc(long size) {
		MemoryBlock block = new TrackedMemoryBlockImpl(FlwMemoryTracker.malloc(size), size, CLEANER);
		FlwMemoryTracker._allocCPUMemory(block.size());
		return block;
	}

	static MemoryBlock calloc(long num, long size) {
		MemoryBlock block = new TrackedMemoryBlockImpl(FlwMemoryTracker.calloc(num, size), num * size, CLEANER);
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
