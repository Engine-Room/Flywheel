package dev.engine_room.flywheel.lib.vertex;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.api.vertex.MutableVertexList;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;

public interface VertexView extends MutableVertexList {
	long ptr();

	void ptr(long ptr);

	void vertexCount(int vertexCount);

	long stride();

	@Nullable
	Object nativeMemoryOwner();

	/**
	 * The memory referenced by this vertex view's pointer may be owned by another object, such that the memory is
	 * automatically freed when the other object becomes phantom reachable or is garbage collected. Use this method to
	 * ensure this vertex view retains a strong reference to the memory owner so this vertex view's pointer remains
	 * valid even when no other references to the memory owner are retained.
	 */
	void nativeMemoryOwner(@Nullable Object owner);

	default void load(MemoryBlock data) {
		long bytes = data.size();
		long stride = stride();
		if (bytes % stride != 0) {
			throw new IllegalArgumentException("MemoryBlock contains non-whole amount of vertices!");
		}
		int vertexCount = (int) (bytes / stride);

		ptr(data.ptr());
		vertexCount(vertexCount);
		nativeMemoryOwner(data);
	}

	@Override
	default void write(MutableVertexList dst, int srcIndex, int dstIndex) {
		if (dst.getClass() == getClass()) {
			long stride = stride();
			long dstPtr = ((VertexView) dst).ptr();
			MemoryUtil.memCopy(ptr() + srcIndex * stride, dstPtr + dstIndex * stride, stride);
		} else {
			MutableVertexList.super.write(dst, srcIndex, dstIndex);
		}
	}

	@Override
	default void write(MutableVertexList dst, int srcStartIndex, int dstStartIndex, int vertexCount) {
		if (dst.getClass() == getClass()) {
			long stride = stride();
			long dstPtr = ((VertexView) dst).ptr();
			MemoryUtil.memCopy(ptr() + srcStartIndex * stride, dstPtr + dstStartIndex * stride, vertexCount * stride);
		} else {
			MutableVertexList.super.write(dst, srcStartIndex, dstStartIndex, vertexCount);
		}
	}

	@Override
	default void writeAll(MutableVertexList dst) {
		if (dst.getClass() == getClass()) {
			long stride = stride();
			long dstPtr = ((VertexView) dst).ptr();
			MemoryUtil.memCopy(ptr(), dstPtr, Math.min(vertexCount(), dst.vertexCount()) * stride);
		} else {
			MutableVertexList.super.writeAll(dst);
		}
	}
}
