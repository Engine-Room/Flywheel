package com.jozufozu.flywheel.backend.engine.indirect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;

import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.gl.GlFence;
import com.jozufozu.flywheel.lib.memory.FlwMemoryTracker;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;

import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

// Used https://github.com/CaffeineMC/sodium-fabric/blob/dev/src/main/java/me/jellysquid/mods/sodium/client/gl/arena/staging/MappedStagingBuffer.java
// as a reference for implementation. Modified to be less safe and to allow for writing directly into the staging buffer.
public class StagingBuffer {
	private static final long DEFAULT_CAPACITY = 1024 * 1024 * 16;
	private static final int STORAGE_FLAGS = GL45C.GL_MAP_PERSISTENT_BIT | GL45C.GL_MAP_WRITE_BIT | GL45C.GL_CLIENT_STORAGE_BIT;
	private static final int MAP_FLAGS = GL45C.GL_MAP_PERSISTENT_BIT | GL45C.GL_MAP_WRITE_BIT | GL45C.GL_MAP_FLUSH_EXPLICIT_BIT | GL45C.GL_MAP_INVALIDATE_BUFFER_BIT;

	private final int vbo;
	private final long map;
	private final long capacity;

	private long start = 0;
	private long pos = 0;

	private long totalAvailable;

	private MemoryBlock scratch;

	private final OverflowStagingBuffer overflow = new OverflowStagingBuffer();
	private final PriorityQueue<Transfer> transfers = new ObjectArrayFIFOQueue<>();
	private final PriorityQueue<FencedRegion> fencedRegions = new ObjectArrayFIFOQueue<>();

	public StagingBuffer() {
		this(DEFAULT_CAPACITY);
	}

	public StagingBuffer(long capacity) {
		this.capacity = capacity;
		vbo = GL45C.glCreateBuffers();

		GL45C.glNamedBufferStorage(vbo, capacity, STORAGE_FLAGS);
		map = GL45C.nglMapNamedBufferRange(vbo, 0, capacity, MAP_FLAGS);

		totalAvailable = capacity;

		FlwMemoryTracker._allocCPUMemory(capacity);
	}

	/**
	 * Enqueue a copy of a known size to the given VBO.
	 * <br>
	 * The consumer will receive a pointer to a block of memory of the given size, and is expected to write to the
	 * complete range. The initial contents of the memory block are undefined.
	 *
	 * @param size      The size in bytes of the copy.
	 * @param dstVbo    The VBO to copy to.
	 * @param dstOffset The offset in the destination VBO.
	 * @param write     A consumer that will receive a pointer to the memory block.
	 */
	public void enqueueCopy(long size, int dstVbo, long dstOffset, LongConsumer write) {
		// Try to write directly into the staging buffer if there is enough contiguous space.
		var direct = reserveForTransferTo(size, dstVbo, dstOffset);

		if (direct != MemoryUtil.NULL) {
			write.accept(direct);
			return;
		}

		// Otherwise, write to a scratch buffer and enqueue a copy.
		var block = getScratch(size);
		write.accept(block.ptr());
		enqueueCopy(block.ptr(), size, dstVbo, dstOffset);
	}

	private MemoryBlock getScratch(long size) {
		if (scratch == null) {
			scratch = MemoryBlock.malloc(size);
		} else if (scratch.size() < size) {
			scratch = scratch.realloc(size);
		}
		return scratch;
	}

	/**
	 * Enqueue a copy from the given pointer to the given VBO.
	 *
	 * @param block     The block to copy from.
	 * @param dstVbo    The VBO to copy to.
	 * @param dstOffset The offset in the destination VBO.
	 */
	public void enqueueCopy(MemoryBlock block, int dstVbo, long dstOffset) {
		enqueueCopy(block.ptr(), block.size(), dstVbo, dstOffset);
	}

	/**
	 * Enqueue a copy from the given pointer to the given VBO.
	 *
	 * @param ptr       The pointer to copy from.
	 * @param size      The size of the copy.
	 * @param dstVbo    The VBO to copy to.
	 * @param dstOffset The offset in the destination VBO.
	 */
	public void enqueueCopy(long ptr, long size, int dstVbo, long dstOffset) {
		if (size > totalAvailable) {
			overflow.enqueueCopy(ptr, size, dstVbo, dstOffset);
			return;
		}

		long remaining = capacity - pos;

		if (size > remaining) {
			long split = size - remaining;

			// Put the first span at the tail of the buffer...
			MemoryUtil.memCopy(ptr, map + pos, remaining);
			transfers.enqueue(new Transfer(pos, dstVbo, dstOffset, remaining));

			// ... and the rest at the head.
			MemoryUtil.memCopy(ptr + remaining, map, split);
			transfers.enqueue(new Transfer(0, dstVbo, dstOffset + remaining, split));

			pos = split;
		} else {
			MemoryUtil.memCopy(ptr, map + pos, size);
			transfers.enqueue(new Transfer(pos, dstVbo, dstOffset, size));

			pos += size;
		}

		totalAvailable -= size;
	}

	/**
	 * Reserve space in this buffer for a transfer to another VBO.
	 * <br>
	 * You must ensure that your writes are complete before the next call to {@link #flush}.
	 * <br>
	 * This will generally be a more efficient way to transfer data as it avoids a copy, however,
	 * this method does not allow for non-contiguous writes, so you should fall back to
	 * {@link #enqueueCopy} if this returns {@code null}.
	 *
	 * @param size      The size of the transfer you wish to make.
	 * @param dstVbo    The VBO you wish to transfer to.
	 * @param dstOffset The offset in the destination VBO.
	 * @return A pointer to the reserved space, or {@code null} if there is not enough contiguous space.
	 */
	public long reserveForTransferTo(long size, int dstVbo, long dstOffset) {
		// Don't need to check totalAvailable here because that's a looser constraint than the bytes remaining.
		long remaining = capacity - pos;
		if (size > remaining) {
			return MemoryUtil.NULL;
		}

		long out = map + pos;

		transfers.enqueue(new Transfer(pos, dstVbo, dstOffset, size));

		pos += size;

		totalAvailable -= size;

		return out;
	}

	public void flush() {
		if (transfers.isEmpty()) {
			return;
		}

		if (pos < start) {
			// we rolled around, need to flush 2 ranges.
			GL45C.glFlushMappedNamedBufferRange(vbo, start, capacity - start);
			GL45C.glFlushMappedNamedBufferRange(vbo, 0, pos);
		} else {
			GL45C.glFlushMappedNamedBufferRange(vbo, start, pos - start);
		}

		long usedCapacity = 0;

		for (Transfer transfer : consolidateCopies(transfers)) {
			usedCapacity += transfer.size;

			GL45C.glCopyNamedBufferSubData(vbo, transfer.dstVbo, transfer.srcOffset, transfer.dstOffset, transfer.size);
		}

		fencedRegions.enqueue(new FencedRegion(new GlFence(), usedCapacity));

		start = pos;
	}

	private static List<Transfer> consolidateCopies(PriorityQueue<Transfer> queue) {
		List<Transfer> merged = new ArrayList<>();
		Transfer last = null;

		while (!queue.isEmpty()) {
			Transfer transfer = queue.dequeue();

			if (last != null) {
				if (areContiguous(last, transfer)) {
					last.size += transfer.size;
					continue;
				}
			}

			merged.add(last = new Transfer(transfer));
		}

		return merged;
	}

	private static boolean areContiguous(Transfer last, Transfer transfer) {
		return last.dstVbo == transfer.dstVbo && last.dstOffset + last.size == transfer.dstOffset && last.srcOffset + last.size == transfer.srcOffset;
	}

	public void reclaim() {
		while (!fencedRegions.isEmpty()) {
			var region = fencedRegions.first();
			if (!region.fence.isSignaled()) {
				// We can't reclaim this region yet, and we know that all the regions after it are also not ready.
				break;
			}
			fencedRegions.dequeue();

			region.fence.delete();

			totalAvailable += region.capacity;
		}
	}

	public void delete() {
		GL45C.glUnmapNamedBuffer(vbo);
		GL45C.glDeleteBuffers(vbo);
		overflow.delete();

		scratch.free();

		FlwMemoryTracker._freeCPUMemory(capacity);
	}

	private static final class Transfer {
		private final long srcOffset;
		private final int dstVbo;
		private final long dstOffset;
		private long size;

		private Transfer(long srcOffset, int dstVbo, long dstOffset, long size) {
			this.srcOffset = srcOffset;
			this.dstVbo = dstVbo;
			this.dstOffset = dstOffset;
			this.size = size;
		}

		public Transfer(Transfer other) {
			this(other.srcOffset, other.dstVbo, other.dstOffset, other.size);
		}
	}

	private record FencedRegion(GlFence fence, long capacity) {
	}

	private static class OverflowStagingBuffer {
		private final int vbo;

		public OverflowStagingBuffer() {
			vbo = GL45C.glCreateBuffers();
		}

		public void enqueueCopy(long ptr, long size, int dstVbo, long dstOffset) {
			GL45C.nglNamedBufferData(vbo, size, ptr, GL45C.GL_STREAM_COPY);
			GL45C.glCopyNamedBufferSubData(vbo, dstVbo, 0, dstOffset, size);
		}

		public void delete() {
			GL45C.glDeleteBuffers(vbo);
		}
	}
}
