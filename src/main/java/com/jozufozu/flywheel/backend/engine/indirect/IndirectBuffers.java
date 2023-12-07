package com.jozufozu.flywheel.backend.engine.indirect;

import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL44.nglBindBuffersRange;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;

import com.jozufozu.flywheel.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;

public class IndirectBuffers {
	// Number of vbos created.
	public static final int BUFFER_COUNT = 4;

	public static final long INT_SIZE = Integer.BYTES;
	public static final long PTR_SIZE = Pointer.POINTER_SIZE;

	// Byte size of a draw command, plus our added mesh data.
	public static final long DRAW_COMMAND_STRIDE = 40;
	public static final long DRAW_COMMAND_OFFSET = 0;

	public static final long MODEL_STRIDE = 24;

	// Offsets to the 3 segments
	private static final long HANDLE_OFFSET = 0;
	private static final long OFFSET_OFFSET = BUFFER_COUNT * INT_SIZE;
	private static final long SIZE_OFFSET = OFFSET_OFFSET + BUFFER_COUNT * PTR_SIZE;
	// Total size of the buffer.
	private static final long BUFFERS_SIZE_BYTES = SIZE_OFFSET + BUFFER_COUNT * PTR_SIZE;

	// Offsets to the vbos
	private static final long OBJECT_HANDLE_OFFSET = HANDLE_OFFSET;
	private static final long TARGET_HANDLE_OFFSET = INT_SIZE;
	private static final long MODEL_HANDLE_OFFSET = INT_SIZE * 2;
	private static final long DRAW_HANDLE_OFFSET = INT_SIZE * 3;

	// Offsets to the sizes
	private static final long OBJECT_SIZE_OFFSET = SIZE_OFFSET;
	private static final long TARGET_SIZE_OFFSET = SIZE_OFFSET + PTR_SIZE;
	private static final long MODEL_SIZE_OFFSET = SIZE_OFFSET + PTR_SIZE * 2;
	private static final long DRAW_SIZE_OFFSET = SIZE_OFFSET + PTR_SIZE * 3;


	/**
	 * A small block of memory divided into 3 contiguous segments:
	 * <br>
	 * {@code buffers}: an array of {@link IndirectBuffers#INT_SIZE} buffer handles.
	 * <br>
	 * {@code offsets}: an array of {@link IndirectBuffers#PTR_SIZE} offsets into the buffers, currently just zeroed.
	 * <br>
	 * {@code sizes}: an array of {@link IndirectBuffers#PTR_SIZE} byte lengths of the buffers.
	 * <br>
	 * Each segment stores {@link IndirectBuffers#BUFFER_COUNT} elements,
	 * one for the object buffer, target buffer, model buffer, and draw buffer.
	 */
	private final MemoryBlock multiBindBlock;
	private final long objectStride;
	public final ResizableStorageArray object;
	public final ResizableStorageArray target;
	public final ResizableStorageArray model;
	public final ResizableStorageArray draw;

	IndirectBuffers(long objectStride) {
		this.objectStride = objectStride;
		this.multiBindBlock = MemoryBlock.calloc(BUFFERS_SIZE_BYTES, 1);

		object = new ResizableStorageArray(objectStride, 1.75);
		target = new ResizableStorageArray(INT_SIZE, 1.75);
		model = new ResizableStorageArray(MODEL_STRIDE, 2);
		draw = new ResizableStorageArray(DRAW_COMMAND_STRIDE, 2);
	}

	void updateCounts(int objectCount, int drawCount, int modelCount) {
		object.ensureCapacity(objectCount);
		target.ensureCapacity(objectCount);
		model.ensureCapacity(modelCount);
		draw.ensureCapacity(drawCount);

		final long ptr = multiBindBlock.ptr();
		MemoryUtil.memPutInt(ptr + OBJECT_HANDLE_OFFSET, object.handle());
		MemoryUtil.memPutInt(ptr + TARGET_HANDLE_OFFSET, target.handle());
		MemoryUtil.memPutInt(ptr + MODEL_HANDLE_OFFSET, model.handle());
		MemoryUtil.memPutInt(ptr + DRAW_HANDLE_OFFSET, draw.handle());

		MemoryUtil.memPutAddress(ptr + OBJECT_SIZE_OFFSET, objectStride * objectCount);
		MemoryUtil.memPutAddress(ptr + TARGET_SIZE_OFFSET, INT_SIZE * objectCount);
		MemoryUtil.memPutAddress(ptr + MODEL_SIZE_OFFSET, MODEL_STRIDE * modelCount);
		MemoryUtil.memPutAddress(ptr + DRAW_SIZE_OFFSET, DRAW_COMMAND_STRIDE * drawCount);
	}

	public void bindForCompute() {
		multiBind();
	}

	public void bindForDraw() {
		multiBind();
		GlBufferType.DRAW_INDIRECT_BUFFER.bind(draw.handle());
	}

	private void multiBind() {
		final long ptr = multiBindBlock.ptr();
		nglBindBuffersRange(GL_SHADER_STORAGE_BUFFER, 0, IndirectBuffers.BUFFER_COUNT, ptr, ptr + OFFSET_OFFSET, ptr + SIZE_OFFSET);
	}

	public void delete() {
		multiBindBlock.free();

		object.delete();
		target.delete();
		model.delete();
		draw.delete();
	}
}
