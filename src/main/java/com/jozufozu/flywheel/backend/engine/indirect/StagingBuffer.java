package com.jozufozu.flywheel.backend.engine.indirect;

import java.util.function.LongConsumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL45;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.backend.compile.IndirectPrograms;
import com.jozufozu.flywheel.backend.gl.GlCompat;
import com.jozufozu.flywheel.backend.gl.GlFence;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
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

	private final OverflowStagingBuffer overflow = new OverflowStagingBuffer();
	private final TransferList transfers = new TransferList();
	private final PriorityQueue<FencedRegion> fencedRegions = new ObjectArrayFIFOQueue<>();
	private final GlBuffer scatterBuffer = new GlBuffer();
	private final ScatterList scatterList = new ScatterList();

	private final GlProgram scatterProgram;

	/**
	 * The position in the buffer at the time of the last flush.
	 */
	private long start = 0;
	/**
	 * The current position in the buffer,
	 * incremented as transfers are enqueued.
	 */
	private long pos = 0;
	/**
	 * The number of bytes used in the buffer since the last flush,
	 * decremented as transfers are enqueued.
	 */
	private long usedCapacity = 0;

	/**
	 * The number of bytes available in the buffer.
	 * <br>
	 * This decreases as transfers are enqueued and increases as fenced regions are reclaimed.
	 */
	private long totalAvailable;

	/**
	 * A scratch buffer for when there is not enough contiguous space
	 * in the staging buffer for the write the user wants to make.
	 */
	@Nullable
	private MemoryBlock scratch;

	public StagingBuffer(IndirectPrograms programs) {
		this(DEFAULT_CAPACITY, programs);
	}

	public StagingBuffer(long capacity, IndirectPrograms programs) {
		this.capacity = capacity;
		vbo = GL45C.glCreateBuffers();

		GL45C.glNamedBufferStorage(vbo, capacity, STORAGE_FLAGS);
		map = GL45C.nglMapNamedBufferRange(vbo, 0, capacity, MAP_FLAGS);

		totalAvailable = capacity;

		FlwMemoryTracker._allocCPUMemory(capacity);

		scatterProgram = programs.getScatterProgram();
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
		var direct = reserveForCopy(size, dstVbo, dstOffset);

		if (direct != MemoryUtil.NULL) {
			write.accept(direct);
			return;
		}

		// Otherwise, write to a scratch buffer and enqueue a copy.
		var block = getScratch(size);
		write.accept(block.ptr());
		enqueueCopy(block.ptr(), size, dstVbo, dstOffset);
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
		assertMultipleOf4(size);

		if (size > totalAvailable) {
			overflow.upload(ptr, size, dstVbo, dstOffset);
			return;
		}

		long remaining = capacity - pos;

		if (size > remaining) {
			long split = size - remaining;

			// Put the first span at the tail of the buffer...
			MemoryUtil.memCopy(ptr, map + pos, remaining);
			pushTransfer(dstVbo, pos, dstOffset, remaining);

			// ... and the rest at the head.
			MemoryUtil.memCopy(ptr + remaining, map, split);
			pushTransfer(dstVbo, 0, dstOffset + remaining, split);

			pos = split;
		} else {
			MemoryUtil.memCopy(ptr, map + pos, size);
			pushTransfer(dstVbo, pos, dstOffset, size);

			pos += size;
		}
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
	public long reserveForCopy(long size, int dstVbo, long dstOffset) {
		assertMultipleOf4(size);
		// Don't need to check totalAvailable here because that's a looser constraint than the bytes remaining.
		long remaining = capacity - pos;
		if (size > remaining) {
			return MemoryUtil.NULL;
		}

		long out = map + pos;

		pushTransfer(dstVbo, pos, dstOffset, size);

		pos += size;

		return out;
	}

	public void flush() {
		if (transfers.isEmpty()) {
			return;
		}

		flushUsedRegion();

		dispatchComputeCopies();

		transfers.reset();
		fencedRegions.enqueue(new FencedRegion(new GlFence(), usedCapacity));

		usedCapacity = 0;
		start = pos;
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
		scatterBuffer.delete();

		if (scratch != null) {
			scratch.free();
		}

		transfers.delete();
		scatterList.delete();

		FlwMemoryTracker._freeCPUMemory(capacity);
	}

	@NotNull
	private MemoryBlock getScratch(long size) {
		if (scratch == null) {
			scratch = MemoryBlock.malloc(size);
		} else if (scratch.size() < size) {
			scratch = scratch.realloc(size);
		}
		return scratch;
	}

	private void pushTransfer(int dstVbo, long srcOffset, long dstOffset, long size) {
		transfers.push(dstVbo, srcOffset, dstOffset, size);
		usedCapacity += size;
		totalAvailable -= size;
	}

	/**
	 * We <em>could</em> just use {@link #sendCopyCommands}, but that has significant
	 * overhead for many small transfers, such as when the object buffer is sparsely updated.
	 * <br>
	 * Instead, we use a compute shader to scatter the data from the staging buffer to the destination VBOs.
	 * This approach is recommended by nvidia in
	 * <a href=https://on-demand.gputechconf.com/gtc/2016/presentation/s6138-christoph-kubisch-pierre-boudier-gpu-driven-rendering.pdf>this presentation</a>
	 */
	private void dispatchComputeCopies() {
		scatterProgram.bind();

		// These bindings don't change between dstVbos.
		GL45.glBindBufferBase(GL45C.GL_SHADER_STORAGE_BUFFER, 0, scatterBuffer.handle());
		GL45.glBindBufferBase(GL45C.GL_SHADER_STORAGE_BUFFER, 1, vbo);

		int dstVbo;
		var transferCount = transfers.length();
		for (int i = 0; i < transferCount; i++) {
			dstVbo = transfers.vbo(i);

			scatterList.pushTransfer(transfers, i);

			int nextVbo = i == transferCount - 1 ? -1 : transfers.vbo(i + 1);

			// If we're switching VBOs, dispatch the copies for the previous VBO.
			// Generally VBOs don't appear in multiple spans of the list,
			// so submitting duplicates is rare.
			if (dstVbo != nextVbo) {
				dispatchScatter(dstVbo);
			}
		}
	}

	private void dispatchScatter(int dstVbo) {
		scatterBuffer.upload(scatterList.ptr(), scatterList.usedBytes());

		GL45.glBindBufferBase(GL45C.GL_SHADER_STORAGE_BUFFER, 2, dstVbo);

		GL45.glDispatchCompute(GlCompat.getComputeGroupCount(scatterList.copyCount()), 1, 1);

		scatterList.reset();
	}

	private void assertMultipleOf4(long size) {
		if (size % 4 != 0) {
			throw new IllegalArgumentException("Size must be a multiple of 4");
		}
	}

	private long sendCopyCommands() {
		long usedCapacity = 0;

		for (int i = 0; i < transfers.length(); i++) {
			var size = transfers.size(i);

			usedCapacity += size;

			GL45C.glCopyNamedBufferSubData(vbo, transfers.vbo(i), transfers.srcOffset(i), transfers.dstOffset(i), size);
		}

		return usedCapacity;
	}

	private void flushUsedRegion() {
		if (pos < start) {
			// we rolled around, need to flush 2 ranges.
			GL45C.glFlushMappedNamedBufferRange(vbo, start, capacity - start);
			GL45C.glFlushMappedNamedBufferRange(vbo, 0, pos);
		} else {
			GL45C.glFlushMappedNamedBufferRange(vbo, start, pos - start);
		}
	}

	private record FencedRegion(GlFence fence, long capacity) {
	}

	private static class OverflowStagingBuffer {
		private final int vbo;

		public OverflowStagingBuffer() {
			vbo = GL45C.glCreateBuffers();
		}

		public void upload(long ptr, long size, int dstVbo, long dstOffset) {
			GL45C.nglNamedBufferData(vbo, size, ptr, GL45C.GL_STREAM_COPY);
			GL45C.glCopyNamedBufferSubData(vbo, dstVbo, 0, dstOffset, size);
		}

		public void delete() {
			GL45C.glDeleteBuffers(vbo);
		}
	}
}
