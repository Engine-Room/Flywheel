package com.jozufozu.flywheel.backend.instancing.indirect;

import static org.lwjgl.opengl.GL46.*;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;

public class IndirectBuffers {
	public static final int BUFFER_COUNT = 4;
	public static final long INT_SIZE = Integer.BYTES;
	public static final long PTR_SIZE = Pointer.POINTER_SIZE;

	// DRAW COMMAND
	public static final long DRAW_COMMAND_STRIDE = 36;
	public static final long DRAW_COMMAND_OFFSET = 0;

	// BITS
	private static final int SUB_DATA_BITS = GL_DYNAMIC_STORAGE_BIT;
	private static final int PERSISTENT_BITS = GL_MAP_PERSISTENT_BIT | GL_MAP_WRITE_BIT;
	private static final int MAP_BITS = PERSISTENT_BITS | GL_MAP_FLUSH_EXPLICIT_BIT;
	private static final int GPU_ONLY_BITS = 0;

	// OFFSETS
	private static final long OFFSET_OFFSET = BUFFER_COUNT * INT_SIZE;
	private static final long SIZE_OFFSET = OFFSET_OFFSET + BUFFER_COUNT * PTR_SIZE;
	private static final long BUFFERS_SIZE_BYTES = SIZE_OFFSET + BUFFER_COUNT * PTR_SIZE;

	private static final long OBJECT_SIZE_OFFSET = SIZE_OFFSET;
	private static final long TARGET_SIZE_OFFSET = OBJECT_SIZE_OFFSET + PTR_SIZE;
	private static final long BATCH_SIZE_OFFSET = TARGET_SIZE_OFFSET + PTR_SIZE;
	private static final long DRAW_SIZE_OFFSET = BATCH_SIZE_OFFSET + PTR_SIZE;

	final long buffers;
	final long objectStride;
	int object;
	int target;
	int batch;
	int draw;

	long objectPtr;
	long batchPtr;
	long drawPtr;

	int maxObjectCount;
	int maxDrawCount;

	IndirectBuffers(long objectStride) {
		this.objectStride = objectStride;
		this.buffers = MemoryUtil.nmemAlloc(BUFFERS_SIZE_BYTES);

		if (this.buffers == MemoryUtil.NULL) {
			throw new OutOfMemoryError();
		}

		MemoryUtil.memSet(this.buffers, 0, BUFFERS_SIZE_BYTES);
	}

	void createBuffers() {
		nglCreateBuffers(4, buffers);
		object = MemoryUtil.memGetInt(buffers);
		target = MemoryUtil.memGetInt(buffers + 4);
		batch = MemoryUtil.memGetInt(buffers + 8);
		draw = MemoryUtil.memGetInt(buffers + 12);
	}

	void updateCounts(int objectCount, int drawCount) {

		if (objectCount > maxObjectCount) {
			createObjectStorage(objectCount);
		}
		if (drawCount > maxDrawCount) {
			createDrawStorage(drawCount);
		}

		long objectSize = objectStride * objectCount;
		long targetSize = INT_SIZE * objectCount;
		long drawSize = DRAW_COMMAND_STRIDE * drawCount;

		MemoryUtil.memPutAddress(buffers + OBJECT_SIZE_OFFSET, objectSize);
		MemoryUtil.memPutAddress(buffers + TARGET_SIZE_OFFSET, targetSize);
		MemoryUtil.memPutAddress(buffers + BATCH_SIZE_OFFSET, targetSize);
		MemoryUtil.memPutAddress(buffers + DRAW_SIZE_OFFSET, drawSize);
	}

	void createObjectStorage(int objectCount) {
		var objectSize = objectStride * objectCount;
		var targetSize = INT_SIZE * objectCount;

		glNamedBufferStorage(object, objectSize, PERSISTENT_BITS);
		glNamedBufferStorage(target, targetSize, GPU_ONLY_BITS);
		glNamedBufferStorage(batch, targetSize, PERSISTENT_BITS);

		objectPtr = nglMapNamedBufferRange(object, 0, objectSize, MAP_BITS);
		batchPtr = nglMapNamedBufferRange(batch, 0, targetSize, MAP_BITS);
		maxObjectCount = objectCount;
	}

	void createDrawStorage(int drawCount) {
		var drawSize = DRAW_COMMAND_STRIDE * drawCount;
		glNamedBufferStorage(draw, drawSize, SUB_DATA_BITS);
		drawPtr = MemoryUtil.nmemAlloc(drawSize);
		// drawPtr = nglMapNamedBufferRange(draw, 0, drawSize, MAP_BITS);
		maxDrawCount = drawCount;
	}

	public void bindAll() {
		bindN(BUFFER_COUNT);
	}

	public void bindObjectAndTarget() {
		bindN(2);
	}

	private void bindN(int bufferCount) {
		nglBindBuffersRange(GL_SHADER_STORAGE_BUFFER, 0, bufferCount, buffers, buffers + OFFSET_OFFSET, buffers + SIZE_OFFSET);
	}

	void bindIndirectBuffer() {
		glBindBuffer(GL_DRAW_INDIRECT_BUFFER, draw);
	}

	void flushBatchIDs(long length) {
		glFlushMappedNamedBufferRange(batch, 0, length);
	}

	void flushObjects(long length) {
		glFlushMappedNamedBufferRange(object, 0, length);
	}

	void flushDrawCommands(long length) {
		nglNamedBufferSubData(draw, 0, length, drawPtr);
		// glFlushMappedNamedBufferRange(this.draw, 0, length);
	}
}
