package dev.engine_room.flywheel.backend.engine.indirect;

import org.lwjgl.opengl.GL46;
import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.backend.engine.embed.EnvironmentStorage;

public class MatrixBuffer {
	private final ResizableStorageArray matrices = new ResizableStorageArray(EnvironmentStorage.MATRIX_SIZE_BYTES);

	public void flush(StagingBuffer stagingBuffer, EnvironmentStorage environmentStorage) {
		var arena = environmentStorage.arena;
		var capacity = arena.capacity();

		if (capacity == 0) {
			return;
		}

		matrices.ensureCapacity(capacity);

		stagingBuffer.enqueueCopy((long) arena.capacity() * EnvironmentStorage.MATRIX_SIZE_BYTES, matrices.handle(), 0, ptr -> {
			MemoryUtil.memCopy(arena.indexToPointer(0), ptr, (long) arena.capacity() * EnvironmentStorage.MATRIX_SIZE_BYTES);
		});
	}

	public void bind() {
		if (matrices.capacity() == 0) {
			return;
		}

		GL46.glBindBufferRange(GL46.GL_SHADER_STORAGE_BUFFER, BufferBindings.MATRICES, matrices.handle(), 0, matrices.byteCapacity());
	}
}
