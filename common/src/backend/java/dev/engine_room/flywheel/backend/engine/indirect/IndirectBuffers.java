package dev.engine_room.flywheel.backend.engine.indirect;

import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL44.nglBindBuffersRange;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;

import dev.engine_room.flywheel.backend.gl.buffer.GlBufferType;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;

public class IndirectBuffers {
	// Number of vbos created.
	public static final int BUFFER_COUNT = 7;

	public static final long INT_SIZE = Integer.BYTES;
	public static final long PTR_SIZE = Pointer.POINTER_SIZE;

	public static final long MODEL_STRIDE = 28;

	// Byte size of a draw command, plus our added mesh data.
	public static final long DRAW_COMMAND_STRIDE = 36;
	public static final long DRAW_COMMAND_OFFSET = 0;

	// Offsets to the 3 segments
	private static final long HANDLE_OFFSET = 0;
	private static final long OFFSET_OFFSET = BUFFER_COUNT * INT_SIZE;
	private static final long SIZE_OFFSET = OFFSET_OFFSET + BUFFER_COUNT * PTR_SIZE;
	// Total size of the buffer.
	private static final long BUFFERS_SIZE_BYTES = SIZE_OFFSET + BUFFER_COUNT * PTR_SIZE;

	// Offsets to the vbos
	private static final long PASS_TWO_DISPATCH_HANDLE_OFFSET = HANDLE_OFFSET + BufferBindings.PASS_TWO_DISPATCH * INT_SIZE;
	private static final long PASS_TWO_INSTANCE_INDEX_HANDLE_OFFSET = HANDLE_OFFSET + BufferBindings.PASS_TWO_INSTANCE_INDEX * INT_SIZE;
	private static final long PAGE_FRAME_DESCRIPTOR_HANDLE_OFFSET = HANDLE_OFFSET + BufferBindings.PAGE_FRAME_DESCRIPTOR * INT_SIZE;
	private static final long INSTANCE_HANDLE_OFFSET = HANDLE_OFFSET + BufferBindings.INSTANCE * INT_SIZE;
	private static final long DRAW_INSTANCE_INDEX_HANDLE_OFFSET = HANDLE_OFFSET + BufferBindings.DRAW_INSTANCE_INDEX * INT_SIZE;
	private static final long MODEL_HANDLE_OFFSET = HANDLE_OFFSET + BufferBindings.MODEL * INT_SIZE;
	private static final long DRAW_HANDLE_OFFSET = HANDLE_OFFSET + BufferBindings.DRAW * INT_SIZE;

	// Offsets to the sizes
	private static final long PASS_TWO_DISPATCH_SIZE_OFFSET = SIZE_OFFSET + BufferBindings.PASS_TWO_DISPATCH * PTR_SIZE;
	private static final long PASS_TWO_INSTANCE_INDEX_SIZE_OFFSET = SIZE_OFFSET + BufferBindings.PASS_TWO_INSTANCE_INDEX * PTR_SIZE;
	private static final long PAGE_FRAME_DESCRIPTOR_SIZE_OFFSET = SIZE_OFFSET + BufferBindings.PAGE_FRAME_DESCRIPTOR * PTR_SIZE;
	private static final long INSTANCE_SIZE_OFFSET = SIZE_OFFSET + BufferBindings.INSTANCE * PTR_SIZE;
	private static final long DRAW_INSTANCE_INDEX_SIZE_OFFSET = SIZE_OFFSET + BufferBindings.DRAW_INSTANCE_INDEX * PTR_SIZE;
	private static final long MODEL_SIZE_OFFSET = SIZE_OFFSET + BufferBindings.MODEL * PTR_SIZE;
	private static final long DRAW_SIZE_OFFSET = SIZE_OFFSET + BufferBindings.DRAW * PTR_SIZE;


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

	public final ResizableStorageBuffer passTwoDispatch;
	public final ResizableStorageArray passTwoInstanceIndex;
	public final ObjectStorage objectStorage;
	public final ResizableStorageArray drawInstanceIndex;
	public final ResizableStorageArray model;
	public final ResizableStorageArray draw;

	IndirectBuffers(long instanceStride) {
		this.multiBindBlock = MemoryBlock.calloc(BUFFERS_SIZE_BYTES, 1);

		passTwoDispatch = new ResizableStorageBuffer();
		passTwoInstanceIndex = new ResizableStorageArray(INT_SIZE, INSTANCE_GROWTH_FACTOR);
		objectStorage = new ObjectStorage(instanceStride);
		drawInstanceIndex = new ResizableStorageArray(INT_SIZE, INSTANCE_GROWTH_FACTOR);
		model = new ResizableStorageArray(MODEL_STRIDE, MODEL_GROWTH_FACTOR);
		draw = new ResizableStorageArray(DRAW_COMMAND_STRIDE, DRAW_GROWTH_FACTOR);

		passTwoDispatch.ensureCapacity(INT_SIZE * 4);
	}

	void updateCounts(int instanceCount, int modelCount, int drawCount) {
		drawInstanceIndex.ensureCapacity(instanceCount);
		passTwoInstanceIndex.ensureCapacity(instanceCount);
		model.ensureCapacity(modelCount);
		draw.ensureCapacity(drawCount);

		final long ptr = multiBindBlock.ptr();

		MemoryUtil.memPutInt(ptr + PASS_TWO_DISPATCH_HANDLE_OFFSET, passTwoDispatch.handle());
		MemoryUtil.memPutInt(ptr + PASS_TWO_INSTANCE_INDEX_HANDLE_OFFSET, passTwoInstanceIndex.handle());
		MemoryUtil.memPutInt(ptr + PAGE_FRAME_DESCRIPTOR_HANDLE_OFFSET, objectStorage.frameDescriptorBuffer.handle());
		MemoryUtil.memPutInt(ptr + INSTANCE_HANDLE_OFFSET, objectStorage.objectBuffer.handle());
		MemoryUtil.memPutInt(ptr + DRAW_INSTANCE_INDEX_HANDLE_OFFSET, drawInstanceIndex.handle());
		MemoryUtil.memPutInt(ptr + MODEL_HANDLE_OFFSET, model.handle());
		MemoryUtil.memPutInt(ptr + DRAW_HANDLE_OFFSET, draw.handle());

		MemoryUtil.memPutAddress(ptr + PASS_TWO_DISPATCH_SIZE_OFFSET, passTwoDispatch.capacity());
		MemoryUtil.memPutAddress(ptr + PASS_TWO_INSTANCE_INDEX_SIZE_OFFSET, INT_SIZE * instanceCount);
		MemoryUtil.memPutAddress(ptr + PAGE_FRAME_DESCRIPTOR_SIZE_OFFSET, objectStorage.frameDescriptorBuffer.capacity());
		MemoryUtil.memPutAddress(ptr + INSTANCE_SIZE_OFFSET, objectStorage.objectBuffer.capacity());
		MemoryUtil.memPutAddress(ptr + DRAW_INSTANCE_INDEX_SIZE_OFFSET, INT_SIZE * instanceCount);
		MemoryUtil.memPutAddress(ptr + MODEL_SIZE_OFFSET, MODEL_STRIDE * modelCount);
		MemoryUtil.memPutAddress(ptr + DRAW_SIZE_OFFSET, DRAW_COMMAND_STRIDE * drawCount);
	}

	public void bindForCullPassOne() {
		multiBind(0, 6);
	}

	public void bindForCullPassTwo() {
		multiBind(0, 6);
		GlBufferType.DISPATCH_INDIRECT_BUFFER.bind(passTwoDispatch.handle());
	}

	public void bindForApply() {
		multiBind(5, 2);
	}

	public void bindForModelReset() {
		multiBind(5, 1);
	}

	public void bindForDraw() {
		multiBind(3, 4);
		GlBufferType.DRAW_INDIRECT_BUFFER.bind(draw.handle());
	}

	/**
	 * Bind all buffers except the draw command buffer.
	 */
	public void bindForCrumbling() {
		multiBind(3, 3);
	}

	private void multiBind(int base, int count) {
		final long ptr = multiBindBlock.ptr();
		long handlePtr = ptr + HANDLE_OFFSET + base * INT_SIZE;
		long offsetPtr = ptr + OFFSET_OFFSET + base * PTR_SIZE;
		long sizePtr = ptr + SIZE_OFFSET + base * PTR_SIZE;
		nglBindBuffersRange(GL_SHADER_STORAGE_BUFFER, base, count, handlePtr, offsetPtr, sizePtr);
	}

	public void delete() {
		multiBindBlock.free();

		objectStorage.delete();
		drawInstanceIndex.delete();
		model.delete();
		draw.delete();
		passTwoDispatch.delete();
		passTwoInstanceIndex.delete();
	}
}
