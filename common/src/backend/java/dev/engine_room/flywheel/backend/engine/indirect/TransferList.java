package dev.engine_room.flywheel.backend.engine.indirect;

import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.backend.util.MemoryBuffer;

public class TransferList {
	private static final long STRIDE = Long.BYTES * 4;
	private final MemoryBuffer block = new MemoryBuffer(STRIDE);
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

		block.reallocIfNeeded(length);

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
		return MemoryUtil.memGetInt(block.ptrForIndex(index));
	}

	public long srcOffset(int index) {
		return MemoryUtil.memGetLong(block.ptrForIndex(index) + Long.BYTES);
	}

	public long dstOffset(int index) {
		return MemoryUtil.memGetLong(block.ptrForIndex(index) + Long.BYTES * 2);
	}

	public long size(int index) {
		return MemoryUtil.memGetLong(block.ptrForIndex(index) + Long.BYTES * 3);
	}

	public void delete() {
		block.delete();
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
		MemoryUtil.memPutInt(block.ptrForIndex(index), vbo);
	}

	private void srcOffset(int index, long srcOffset) {
		MemoryUtil.memPutLong(block.ptrForIndex(index) + Long.BYTES, srcOffset);
	}

	private void dstOffset(int index, long dstOffset) {
		MemoryUtil.memPutLong(block.ptrForIndex(index) + Long.BYTES * 2, dstOffset);
	}

	private void size(int index, long size) {
		MemoryUtil.memPutLong(block.ptrForIndex(index) + Long.BYTES * 3, size);
	}
}
