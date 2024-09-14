package dev.engine_room.flywheel.lib.vertex;

import org.jetbrains.annotations.Nullable;

public abstract class AbstractVertexView implements VertexView {
	protected long ptr;
	protected int vertexCount;
	@Nullable
	private Object nativeMemoryOwner;

	@Override
	public long ptr() {
		return ptr;
	}

	@Override
	public void ptr(long ptr) {
		this.ptr = ptr;
	}

	@Override
	public int vertexCount() {
		return vertexCount;
	}

	@Override
	public void vertexCount(int vertexCount) {
		this.vertexCount = vertexCount;
	}

	@Override
	@Nullable
	public final Object nativeMemoryOwner() {
		return nativeMemoryOwner;
	}

	@Override
	public final void nativeMemoryOwner(@Nullable Object owner) {
		nativeMemoryOwner = owner;
	}
}
