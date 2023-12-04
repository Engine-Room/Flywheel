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
	// Number of vbos created.
	public static final int BUFFER_COUNT = 4;

	public static final long INT_SIZE = Integer.BYTES;
	public static final long PTR_SIZE = Pointer.POINTER_SIZE;

	// Byte size of a draw command, plus our added mesh data.
	public static final long DRAW_COMMAND_STRIDE = 40;
	public static final long DRAW_COMMAND_OFFSET = 0;

	public static final long MODEL_STRIDE = 24;

	// BITS
	private static final int SUB_DATA_BITS = GL_DYNAMIC_STORAGE_BIT;
	private static final int PERSISTENT_BITS = GL_MAP_PERSISTENT_BIT | GL_MAP_WRITE_BIT;
	private static final int MAP_BITS = PERSISTENT_BITS | GL_MAP_FLUSH_EXPLICIT_BIT;
	private static final int GPU_ONLY_BITS = 0;

	// Offsets to the vbos
	private static final long VBO_OFFSET = 0;
	private static final long OBJECT_OFFSET = VBO_OFFSET;
	private static final long TARGET_OFFSET = INT_SIZE;
	private static final long MODEL_OFFSET = INT_SIZE * 2;
	private static final long DRAW_OFFSET = INT_SIZE * 3;

	// Offsets to the 3 segments
	private static final long OFFSET_OFFSET = BUFFER_COUNT * INT_SIZE;
	private static final long SIZE_OFFSET = OFFSET_OFFSET + BUFFER_COUNT * PTR_SIZE;
	private static final long OBJECT_SIZE_OFFSET = SIZE_OFFSET;
	private static final long TARGET_SIZE_OFFSET = SIZE_OFFSET + PTR_SIZE;
	private static final long MODEL_SIZE_OFFSET = SIZE_OFFSET + PTR_SIZE * 2;
	private static final long DRAW_SIZE_OFFSET = SIZE_OFFSET + PTR_SIZE * 3;
	// Total size of the buffer.
	private static final long BUFFERS_SIZE_BYTES = SIZE_OFFSET + BUFFER_COUNT * PTR_SIZE;


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
	private final MemoryBlock buffers;
	private final long objectStride;
	private int object;
	private int target;
	private int model;
	private int draw;

	long objectPtr;
	MemoryBlock modelPtr;
	MemoryBlock drawPtr;

	private int maxObjectCount = 0;
	private int maxModelCount = 0;
	private int maxDrawCount = 0;

	private static final float OBJECT_GROWTH_FACTOR = 1.25f;
	private static final float MODEL_GROWTH_FACTOR = 2f;
	private static final float DRAW_GROWTH_FACTOR = 2f;

	IndirectBuffers(long objectStride) {
		this.objectStride = objectStride;
		this.buffers = MemoryBlock.calloc(BUFFERS_SIZE_BYTES, 1);
	}

	void createBuffers() {
		final long ptr = buffers.ptr();
		nglCreateBuffers(BUFFER_COUNT, ptr);
		object = MemoryUtil.memGetInt(ptr + OBJECT_OFFSET);
		target = MemoryUtil.memGetInt(ptr + TARGET_OFFSET);
		model = MemoryUtil.memGetInt(ptr + MODEL_OFFSET);
		draw = MemoryUtil.memGetInt(ptr + DRAW_OFFSET);
	}

	void updateCounts(int objectCount, int drawCount, int modelCount) {
		if (objectCount > maxObjectCount) {
			createObjectStorage((int) (objectCount * OBJECT_GROWTH_FACTOR));
		}
		if (modelCount > maxModelCount) {
			createModelStorage((int) (modelCount * MODEL_GROWTH_FACTOR));
		}
		if (drawCount > maxDrawCount) {
			createDrawStorage((int) (drawCount * DRAW_GROWTH_FACTOR));
		}

		final long ptr = buffers.ptr();
		MemoryUtil.memPutAddress(ptr + OBJECT_SIZE_OFFSET, objectStride * objectCount);
		MemoryUtil.memPutAddress(ptr + TARGET_SIZE_OFFSET, INT_SIZE * objectCount);
		MemoryUtil.memPutAddress(ptr + MODEL_SIZE_OFFSET, MODEL_STRIDE * modelCount);
		MemoryUtil.memPutAddress(ptr + DRAW_SIZE_OFFSET, DRAW_COMMAND_STRIDE * drawCount);
	}

	void createObjectStorage(int objectCount) {
		freeObjectStorage();
		var objectSize = objectStride * objectCount;
		var targetSize = INT_SIZE * objectCount;

		if (maxObjectCount > 0) {
			final long ptr = buffers.ptr();
			nglCreateBuffers(2, ptr);

			int objectNew = MemoryUtil.memGetInt(ptr + OBJECT_OFFSET);
			int targetNew = MemoryUtil.memGetInt(ptr + TARGET_OFFSET);

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

	void createModelStorage(int modelCount) {
		freeModelStorage();

		var modelSize = MODEL_STRIDE * modelCount;
		if (maxModelCount > 0) {
			int modelNew = glCreateBuffers();

			glNamedBufferStorage(modelNew, modelSize, SUB_DATA_BITS);

			glDeleteBuffers(model);

			MemoryUtil.memPutInt(buffers.ptr() + MODEL_OFFSET, modelNew);
			model = modelNew;
			modelPtr = modelPtr.realloc(modelSize);
		} else {
			glNamedBufferStorage(model, modelSize, SUB_DATA_BITS);
			modelPtr = MemoryBlock.malloc(modelSize);
		}
		maxModelCount = modelCount;
		FlwMemoryTracker._allocGPUMemory(maxModelCount * MODEL_STRIDE);
	}

	void createDrawStorage(int drawCount) {
		freeDrawStorage();

		var drawSize = DRAW_COMMAND_STRIDE * drawCount;
		if (maxDrawCount > 0) {
			int drawNew = glCreateBuffers();

			glNamedBufferStorage(drawNew, drawSize, SUB_DATA_BITS);

			glDeleteBuffers(draw);

			MemoryUtil.memPutInt(buffers.ptr() + DRAW_OFFSET, drawNew);
			draw = drawNew;
			drawPtr = drawPtr.realloc(drawSize);
		} else {
			glNamedBufferStorage(draw, drawSize, SUB_DATA_BITS);
			drawPtr = MemoryBlock.malloc(drawSize);
		}
		maxDrawCount = drawCount;
		FlwMemoryTracker._allocGPUMemory(maxDrawCount * DRAW_COMMAND_STRIDE);
	}

	private void freeObjectStorage() {
		FlwMemoryTracker._freeGPUMemory(maxObjectCount * objectStride);
	}

	private void freeModelStorage() {
		FlwMemoryTracker._freeGPUMemory(maxModelCount * MODEL_STRIDE);
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

	void flushModels(long length) {
		nglNamedBufferSubData(model, 0, length, modelPtr.ptr());
	}

	void flushDrawCommands(long length) {
		nglNamedBufferSubData(draw, 0, length, drawPtr.ptr());
	}

	public void delete() {
		nglDeleteBuffers(BUFFER_COUNT, buffers.ptr());
		buffers.free();
		if (modelPtr != null) {
			modelPtr.free();
		}
		if (drawPtr != null) {
			drawPtr.free();
		}
		freeObjectStorage();
		freeModelStorage();
		freeDrawStorage();
	}
}
