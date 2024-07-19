package dev.engine_room.flywheel.backend.engine.indirect;

import org.lwjgl.opengl.GL46;
import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.backend.engine.LightStorage;

public class LightBuffers {
	private final ResizableStorageArray lut = new ResizableStorageArray(4);
	private final ResizableStorageArray sections = new ResizableStorageArray(LightStorage.SECTION_SIZE_BYTES);

	public void flush(StagingBuffer staging, LightStorage light) {
		var capacity = light.capacity();

		if (capacity == 0) {
			return;
		}

		sections.ensureCapacity(capacity);
		light.uploadChangedSections(staging, sections.handle());

		if (light.checkNeedsLutRebuildAndClear()) {
			var lut = light.createLut();

			this.lut.ensureCapacity(lut.size());

			staging.enqueueCopy((long) lut.size() * Integer.BYTES, this.lut.handle(), 0, ptr -> {
				for (int i = 0; i < lut.size(); i++) {
					MemoryUtil.memPutInt(ptr + (long) i * Integer.BYTES, lut.getInt(i));
				}
			});
		}
	}

	public void bind() {
		if (sections.capacity() == 0) {
			return;
		}

		GL46.glBindBufferRange(GL46.GL_SHADER_STORAGE_BUFFER, BufferBindings.LIGHT_LUT, lut.handle(), 0, lut.byteCapacity());
		GL46.glBindBufferRange(GL46.GL_SHADER_STORAGE_BUFFER, BufferBindings.LIGHT_SECTION, sections.handle(), 0, sections.byteCapacity());
	}
}
