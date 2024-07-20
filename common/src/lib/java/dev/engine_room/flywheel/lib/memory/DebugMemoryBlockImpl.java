package dev.engine_room.flywheel.lib.memory;

import java.lang.ref.Cleaner;

import dev.engine_room.flywheel.lib.internal.FlwLibLink;
import dev.engine_room.flywheel.lib.util.StringUtil;

class DebugMemoryBlockImpl extends AbstractMemoryBlockImpl {
	final Cleaner cleaner;
	final CleaningAction cleaningAction;
	final Cleaner.Cleanable cleanable;

	DebugMemoryBlockImpl(long ptr, long size, Cleaner cleaner, int skipFrames) {
		super(ptr, size);
		this.cleaner = cleaner;
		cleaningAction = new CleaningAction(ptr, size, getStackTrace(skipFrames + 1));
		cleanable = cleaner.register(this, cleaningAction);
	}

	@Override
	public boolean isTracked() {
		return false;
	}

	@Override
	void freeInner() {
		super.freeInner();
		cleaningAction.freed = true;
		cleanable.clean();
	}

	@Override
	public MemoryBlock realloc(long size) {
		assertAllocated();
		MemoryBlock block = new DebugMemoryBlockImpl(FlwMemoryTracker.realloc(ptr, size), size, cleaner, 1);
		FlwMemoryTracker._allocCpuMemory(block.size());
		freeInner();
		return block;
	}

	static StackWalker.StackFrame[] getStackTrace(int skipFrames) {
		// for DebugMemoryBlockImpl::realloc, skip 3 frames to get the allocation site:
		// - this method
		// - DebugMemoryBlockImpl::new
		// - DebugMemoryBlockImpl::realloc
		// - {caller is here}
		// for DebugMemoryBlockImpl::malloc/calloc, skip 4 frames to get the allocation site:
		// - this method
		// - DebugMemoryBlockImpl::new
		// - DebugMemoryBlockImpl::malloc/calloc
		// - MemoryBlock::malloc/calloc
		// - {caller is here}
		return StackWalker.getInstance().walk(s -> s.skip(skipFrames + 1).toArray(StackWalker.StackFrame[]::new));
	}

	static MemoryBlock malloc(long size) {
		MemoryBlock block = new DebugMemoryBlockImpl(FlwMemoryTracker.malloc(size), size, CLEANER, 2);
		FlwMemoryTracker._allocCpuMemory(block.size());
		return block;
	}

	static MemoryBlock calloc(long num, long size) {
		MemoryBlock block = new DebugMemoryBlockImpl(FlwMemoryTracker.calloc(num, size), num * size, CLEANER, 2);
		FlwMemoryTracker._allocCpuMemory(block.size());
		return block;
	}

	static class CleaningAction implements Runnable {
		final long ptr;
		final long size;
		final StackWalker.StackFrame[] allocationSite;

		boolean freed;

		CleaningAction(long ptr, long size, StackWalker.StackFrame[] allocationSite) {
			this.ptr = ptr;
			this.size = size;
			this.allocationSite = allocationSite;
		}

		@Override
		public void run() {
			if (!freed) {
				StringBuilder builder = new StringBuilder();
				builder
					.append("Reclaimed ")
					.append(size)
					.append(" bytes at address ")
					.append(StringUtil.formatAddress(ptr))
					.append(" that were leaked from allocation site:");
				for (StackWalker.StackFrame frame : allocationSite) {
					builder.append("\n\t");
					builder.append(frame);
				}
				FlwLibLink.INSTANCE.getLogger().warn(builder.toString());

				FlwMemoryTracker.free(ptr);
				FlwMemoryTracker._freeCpuMemory(size);
			}
		}
	}
}
