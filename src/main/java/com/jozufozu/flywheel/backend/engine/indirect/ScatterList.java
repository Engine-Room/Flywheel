package com.jozufozu.flywheel.backend.engine.indirect;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.backend.util.MemoryBuffer;

public class ScatterList {
	public static final long STRIDE = Integer.BYTES * 2;
	public final long maxBytesPerScatter;
	private final MemoryBuffer block = new MemoryBuffer(STRIDE);
	private int length;
	private long usedBytes;

	public ScatterList() {
		this(64);
	}

	public ScatterList(long maxBytesPerScatter) {
		if ((maxBytesPerScatter & 0b1111111100L) != maxBytesPerScatter) {
			throw new IllegalArgumentException("Max bytes per scatter must be a multiple of 4 and less than 1024");
		}

		this.maxBytesPerScatter = maxBytesPerScatter;
	}

	/**
	 * Breaks a transfer into many smaller scatter commands if it is too large, and appends them to this list.
	 *
	 * @param transfers     The list of transfers to push.
	 * @param transferIndex The index of the transfer to push.
	 */
	public void pushTransfer(TransferList transfers, int transferIndex) {
		long size = transfers.size(transferIndex);
		long srcOffset = transfers.srcOffset(transferIndex);
		long dstOffset = transfers.dstOffset(transferIndex);

		long offset = 0;
		long remaining = size;

		while (offset < size) {
			long copySize = Math.min(remaining, maxBytesPerScatter);
			push(copySize, srcOffset + offset, dstOffset + offset);
			offset += copySize;
			remaining -= copySize;
		}
	}

	public void push(long sizeBytes, long srcOffsetBytes, long dstOffsetBytes) {
		block.reallocIfNeeded(length);

		long ptr = block.ptrForIndex(length);
		MemoryUtil.memPutInt(ptr, packSizeAndSrcOffset(sizeBytes, srcOffsetBytes));
		MemoryUtil.memPutInt(ptr + Integer.BYTES, (int) (dstOffsetBytes >> 2));

		length++;
		usedBytes += STRIDE;
	}

	public int copyCount() {
		return length;
	}

	public long usedBytes() {
		return usedBytes;
	}

	public boolean isEmpty() {
		return length == 0;
	}

	public void reset() {
		length = 0;
		usedBytes = 0;
	}

	public long ptr() {
		return block.ptr();
	}

	public void delete() {
		block.delete();
	}

	private static int packSizeAndSrcOffset(long sizeBytes, long srcOffsetBytes) {
		// Divide by 4 and put the offset in the lower 3 bytes.
		int out = (int) (srcOffsetBytes >>> 2) & 0xFFFFFF;
		// Place the size divided by 4 in the upper byte.
		out |= (int) (sizeBytes << 22) & 0xFF000000;
		return out;
	}
}
