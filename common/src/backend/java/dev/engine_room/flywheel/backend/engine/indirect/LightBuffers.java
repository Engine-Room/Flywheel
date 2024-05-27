package dev.engine_room.flywheel.backend.engine.indirect;

import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.backend.engine.embed.LightStorage;

public class LightBuffers {
	private final ResizableStorageBuffer lightArena = new ResizableStorageBuffer();
	private final ResizableStorageArray lut = new ResizableStorageArray(4);

	public LightBuffers() {
	}

	public void flush(StagingBuffer staging, LightStorage light) {
		light.uploadChangedSections(staging, lightArena.handle());

		if (light.hasNewSections()) {
			var lut = light.createLut();

			this.lut.ensureCapacity(lut.size());

			staging.enqueueCopy((long) lut.size() * Integer.BYTES, this.lut.handle(), 0, ptr -> {
				for (int i = 0; i < lut.size(); i++) {
					MemoryUtil.memPutInt(ptr + (long) i * Integer.BYTES, lut.getInt(i));
				}
			});
		}
	}
}
