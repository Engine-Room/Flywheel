package com.jozufozu.flywheel.backend.engine.indirect;

import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.nglDeleteBuffers;
import static org.lwjgl.opengl.GL30.GL_MAP_FLUSH_EXPLICIT_BIT;
import static org.lwjgl.opengl.GL30.GL_MAP_WRITE_BIT;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL44.GL_DYNAMIC_STORAGE_BIT;
import static org.lwjgl.opengl.GL44.GL_MAP_PERSISTENT_BIT;
import static org.lwjgl.opengl.GL44.nglBindBuffersRange;
import static org.lwjgl.opengl.GL45.glCopyNamedBufferSubData;
import static org.lwjgl.opengl.GL45.glCreateBuffers;
import static org.lwjgl.opengl.GL45.glFlushMappedNamedBufferRange;
import static org.lwjgl.opengl.GL45.glNamedBufferStorage;
import static org.lwjgl.opengl.GL45.nglCreateBuffers;
import static org.lwjgl.opengl.GL45.nglMapNamedBufferRange;
import static org.lwjgl.opengl.GL45.nglNamedBufferSubData;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;

import com.jozufozu.flywheel.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.lib.memory.FlwMemoryTracker;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;

public class IndirectBuffers {
	public static final int BUFFER_COUNT = 3;
	public static final long INT_SIZE = Integer.BYTES;
	public static final long PTR_SIZE = Pointer.POINTER_SIZE;

	// DRAW COMMAND
	public static final long DRAW_COMMAND_STRIDE = 44;
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
	private static final long DRAW_SIZE_OFFSET = TARGET_SIZE_OFFSET + PTR_SIZE;

	private final MemoryBlock buffers;
	private final long objectStride;
	private int object;
	private int target;
	private int draw;

	long objectPtr;
	long drawPtr;

	private int maxObjectCount = 0;
	private int maxDrawCount = 0;

	private static final float OBJECT_GROWTH_FACTOR = 2f;
	private static final float DRAW_GROWTH_FACTOR = 2f;

	IndirectBuffers(long objectStride) {
		this.objectStride = objectStride;
		this.buffers = MemoryBlock.calloc(BUFFERS_SIZE_BYTES, 1);
	}

	void createBuffers() {
		final long ptr = buffers.ptr();
		nglCreateBuffers(BUFFER_COUNT, ptr);
		object = MemoryUtil.memGetInt(ptr);
		target = MemoryUtil.memGetInt(ptr + 4);
		draw = MemoryUtil.memGetInt(ptr + 8);
	}

	void updateCounts(int objectCount, int drawCount) {
		if (objectCount > maxObjectCount) {
			createObjectStorage((int) (objectCount * OBJECT_GROWTH_FACTOR));
		}
		if (drawCount > maxDrawCount) {
			createDrawStorage((int) (drawCount * DRAW_GROWTH_FACTOR));
		}

		final long objectSize = objectStride * objectCount;
		final long targetSize = INT_SIZE * objectCount;
		final long drawSize = DRAW_COMMAND_STRIDE * drawCount;

		final long ptr = buffers.ptr();
		MemoryUtil.memPutAddress(ptr + OBJECT_SIZE_OFFSET, objectSize);
		MemoryUtil.memPutAddress(ptr + TARGET_SIZE_OFFSET, targetSize);
		MemoryUtil.memPutAddress(ptr + DRAW_SIZE_OFFSET, drawSize);
	}

	void createObjectStorage(int objectCount) {
		freeObjectStogare();
		var objectSize = objectStride * objectCount;
		var targetSize = INT_SIZE * objectCount;

		if (maxObjectCount > 0) {
			final long ptr = buffers.ptr();
			nglCreateBuffers(BUFFER_COUNT - 1, ptr);

			int objectNew = MemoryUtil.memGetInt(ptr);
			int targetNew = MemoryUtil.memGetInt(ptr + 4);

			glNamedBufferStorage(objectNew, objectSize, PERSISTENT_BITS);
			glNamedBufferStorage(targetNew, targetSize, GPU_ONLY_BITS);

			glCopyNamedBufferSubData(object, objectNew, 0, 0, objectStride * maxObjectCount);
			glCopyNamedBufferSubData(target, targetNew, 0, 0, INT_SIZE * maxObjectCount);

			glDeleteBuffers(object);
			glDeleteBuffers(target);

			object = objectNew;
			target = targetNew;
		} else {
			glNamedBufferStorage(object, objectSize, PERSISTENT_BITS);
			glNamedBufferStorage(target, targetSize, GPU_ONLY_BITS);
		}

		objectPtr = nglMapNamedBufferRange(object, 0, objectSize, MAP_BITS);
		maxObjectCount = objectCount;

		FlwMemoryTracker._allocGPUMemory(maxObjectCount * objectStride);
	}

	void createDrawStorage(int drawCount) {
		freeDrawStorage();

		var drawSize = DRAW_COMMAND_STRIDE * drawCount;
		if (maxDrawCount > 0) {
			int drawNew = glCreateBuffers();

			glNamedBufferStorage(drawNew, drawSize, SUB_DATA_BITS);

			glDeleteBuffers(draw);

			MemoryUtil.memPutInt(buffers.ptr() + INT_SIZE * 2, drawNew);
			draw = drawNew;
			drawPtr = MemoryUtil.nmemRealloc(drawPtr, drawSize);
		} else {

			glNamedBufferStorage(draw, drawSize, SUB_DATA_BITS);
			drawPtr = MemoryUtil.nmemAlloc(drawSize);
		}
		maxDrawCount = drawCount;
		FlwMemoryTracker._allocGPUMemory(maxDrawCount * DRAW_COMMAND_STRIDE);
	}

	private void freeObjectStogare() {
		FlwMemoryTracker._freeGPUMemory(maxObjectCount * objectStride);
	}

	private void freeDrawStorage() {
		FlwMemoryTracker._freeGPUMemory(maxDrawCount * DRAW_COMMAND_STRIDE);
	}

	public void bindForCompute() {
		multiBind();
	}

	public void bindForDraw() {
		multiBind();
		GlBufferType.DRAW_INDIRECT_BUFFER.bind(draw);
	}

	private void multiBind() {
		final long ptr = buffers.ptr();
		nglBindBuffersRange(GL_SHADER_STORAGE_BUFFER, 0, IndirectBuffers.BUFFER_COUNT, ptr, ptr + OFFSET_OFFSET, ptr + SIZE_OFFSET);
	}

	void flushObjects(long length) {
		glFlushMappedNamedBufferRange(object, 0, length);
	}

	void flushDrawCommands(long length) {
		nglNamedBufferSubData(draw, 0, length, drawPtr);
		// glFlushMappedNamedBufferRange(this.draw, 0, length);
	}

	public void delete() {
		nglDeleteBuffers(BUFFER_COUNT, buffers.ptr());
		buffers.free();
		freeObjectStogare();
		freeDrawStorage();
	}
}
