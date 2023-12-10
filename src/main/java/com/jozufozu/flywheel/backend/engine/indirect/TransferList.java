package com.jozufozu.flywheel.backend.engine.indirect;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.lib.memory.MemoryBlock;

public class TransferList {
	private static final long STRIDE = Long.BYTES * 4;
	private MemoryBlock block;
	private int length;

	/**
	 * Append a transfer to the end of the list, combining with the last transfer if possible.
	 *
	 * @param vbo       The VBO to transfer to.
	 * @param srcOffset The offset in the staging buffer.
	 * @param dstOffset The offset in the VBO.
	 * @param size      The size of the transfer.
	 */
	public void push(int vbo, long srcOffset, long dstOffset, long size) {
		if (continuesLast(vbo, srcOffset, dstOffset)) {
			int lastIndex = length - 1;
			size(lastIndex, size(lastIndex) + size);
			return;
		}

		reallocIfNeeded(length);

		vbo(length, vbo);
		srcOffset(length, srcOffset);
		dstOffset(length, dstOffset);
		size(length, size);

		length++;
	}

	/**
	 * @return The number of transfers in the list.
	 */
	public int length() {
		return length;
	}

	/**
	 * @return {@code true} if there are no transfers in the list, {@code false} otherwise.
	 */
	public boolean isEmpty() {
		return length == 0;
	}

	/**
	 * Reset the list to be empty.
	 */
	public void reset() {
		length = 0;
	}

	public int vbo(int index) {
		return MemoryUtil.memGetInt(ptrForIndex(index));
	}

	public long srcOffset(int index) {
		return MemoryUtil.memGetLong(ptrForIndex(index) + Long.BYTES);
	}

	public long dstOffset(int index) {
		return MemoryUtil.memGetLong(ptrForIndex(index) + Long.BYTES * 2);
	}

	public long size(int index) {
		return MemoryUtil.memGetLong(ptrForIndex(index) + Long.BYTES * 3);
	}

	public void delete() {
		if (block != null) {
			block.free();
		}
	}

	private boolean continuesLast(int vbo, long srcOffset, long dstOffset) {
		if (length == 0) {
			return false;
		}
		int lastIndex = length - 1;
		var lastSize = size(lastIndex);
		return vbo(lastIndex) == vbo && dstOffset(lastIndex) + lastSize == dstOffset && srcOffset(lastIndex) + lastSize == srcOffset;
	}

	private void vbo(int index, int vbo) {
		MemoryUtil.memPutInt(ptrForIndex(index), vbo);
	}

	private void srcOffset(int index, long srcOffset) {
		MemoryUtil.memPutLong(ptrForIndex(index) + Long.BYTES, srcOffset);
	}

	private void dstOffset(int index, long dstOffset) {
		MemoryUtil.memPutLong(ptrForIndex(index) + Long.BYTES * 2, dstOffset);
	}

	private void size(int index, long size) {
		MemoryUtil.memPutLong(ptrForIndex(index) + Long.BYTES * 3, size);
	}

	private void reallocIfNeeded(int index) {
		if (block == null) {
			block = MemoryBlock.malloc(neededCapacityForIndex(index + 8));
		} else if (block.size() < neededCapacityForIndex(index)) {
			block = block.realloc(neededCapacityForIndex(index + 8));
		}
	}

	private long ptrForIndex(int index) {
		return block.ptr() + bytePosForIndex(index);
	}

	private static long bytePosForIndex(int index) {
		return index * STRIDE;
	}

	private static long neededCapacityForIndex(int index) {
		return (index + 1) * STRIDE;
	}
}
