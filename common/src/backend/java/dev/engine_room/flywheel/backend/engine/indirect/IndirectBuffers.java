package dev.engine_room.flywheel.backend.engine.indirect;

import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL44.nglBindBuffersRange;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;

import dev.engine_room.flywheel.backend.gl.buffer.GlBufferType;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;

public class IndirectBuffers {
	// Number of vbos created.
	public static final int BUFFER_COUNT = 5;

	public static final long INT_SIZE = Integer.BYTES;
	public static final long PTR_SIZE = Pointer.POINTER_SIZE;

	public static final long MODEL_STRIDE = 28;

	// Byte size of a draw command, plus our added mesh data.
	public static final long DRAW_COMMAND_STRIDE = 44;
	public static final long DRAW_COMMAND_OFFSET = 0;

	// Offsets to the 3 segments
	private static final long HANDLE_OFFSET = 0;
	private static final long OFFSET_OFFSET = BUFFER_COUNT * INT_SIZE;
	private static final long SIZE_OFFSET = OFFSET_OFFSET + BUFFER_COUNT * PTR_SIZE;
	// Total size of the buffer.
	private static final long BUFFERS_SIZE_BYTES = SIZE_OFFSET + BUFFER_COUNT * PTR_SIZE;

	// Offsets to the vbos
	private static final long INSTANCE_HANDLE_OFFSET = HANDLE_OFFSET;
	private static final long TARGET_HANDLE_OFFSET = INT_SIZE;
	private static final long MODEL_INDEX_HANDLE_OFFSET = INT_SIZE * 2;
	private static final long MODEL_HANDLE_OFFSET = INT_SIZE * 3;
	private static final long DRAW_HANDLE_OFFSET = INT_SIZE * 4;

	// Offsets to the sizes
	private static final long INSTANCE_SIZE_OFFSET = SIZE_OFFSET;
	private static final long TARGET_SIZE_OFFSET = SIZE_OFFSET + PTR_SIZE;
	private static final long MODEL_INDEX_SIZE_OFFSET = SIZE_OFFSET + PTR_SIZE * 2;
	private static final long MODEL_SIZE_OFFSET = SIZE_OFFSET + PTR_SIZE * 3;
	private static final long DRAW_SIZE_OFFSET = SIZE_OFFSET + PTR_SIZE * 4;

	private static final float INSTANCE_GROWTH_FACTOR = 1.25f;
	private static final float MODEL_GROWTH_FACTOR = 2f;
	private static final float DRAW_GROWTH_FACTOR = 2f;

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
	 * one for the instance buffer, target buffer, model index buffer, model buffer, and draw buffer.
	 */
	private final MemoryBlock multiBindBlock;
	private final long instanceStride;

	public final InstancePager pageFile;
	public final ResizableStorageArray target;
	public final ResizableStorageArray model;
	public final ResizableStorageArray draw;

	IndirectBuffers(long instanceStride) {
		this.instanceStride = instanceStride;
		this.multiBindBlock = MemoryBlock.calloc(BUFFERS_SIZE_BYTES, 1);

		pageFile = new InstancePager(instanceStride);
		target = new ResizableStorageArray(INT_SIZE, INSTANCE_GROWTH_FACTOR);
		model = new ResizableStorageArray(MODEL_STRIDE, MODEL_GROWTH_FACTOR);
		draw = new ResizableStorageArray(DRAW_COMMAND_STRIDE, DRAW_GROWTH_FACTOR);
	}

	void updateCounts(int instanceCount, int modelCount, int drawCount) {
		target.ensureCapacity(instanceCount);
		model.ensureCapacity(modelCount);
		draw.ensureCapacity(drawCount);

		final long ptr = multiBindBlock.ptr();
		MemoryUtil.memPutInt(ptr + INSTANCE_HANDLE_OFFSET, pageFile.storage.handle());
		MemoryUtil.memPutInt(ptr + TARGET_HANDLE_OFFSET, target.handle());
		MemoryUtil.memPutInt(ptr + MODEL_INDEX_HANDLE_OFFSET, pageFile.pageTable.handle());
		MemoryUtil.memPutInt(ptr + MODEL_HANDLE_OFFSET, model.handle());
		MemoryUtil.memPutInt(ptr + DRAW_HANDLE_OFFSET, draw.handle());

		MemoryUtil.memPutAddress(ptr + INSTANCE_SIZE_OFFSET, pageFile.storage.byteCapacity());
		MemoryUtil.memPutAddress(ptr + TARGET_SIZE_OFFSET, INT_SIZE * instanceCount);
		MemoryUtil.memPutAddress(ptr + MODEL_INDEX_SIZE_OFFSET, pageFile.pageTable.byteCapacity());
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
		nglBindBuffersRange(GL_SHADER_STORAGE_BUFFER, BufferBindings.INSTANCE, IndirectBuffers.BUFFER_COUNT, ptr, ptr + OFFSET_OFFSET, ptr + SIZE_OFFSET);
	}

	/**
	 * Bind all buffers except the draw command buffer.
	 */
	public void bindForCrumbling() {
		final long ptr = multiBindBlock.ptr();
		nglBindBuffersRange(GL_SHADER_STORAGE_BUFFER, BufferBindings.INSTANCE, 4, ptr, ptr + OFFSET_OFFSET, ptr + SIZE_OFFSET);
	}

	public void delete() {
		multiBindBlock.free();

		pageFile.delete();
		target.delete();
		model.delete();
		draw.delete();
	}
}
