package dev.engine_room.flywheel.backend.engine.instancing;

import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.backend.Samplers;
import dev.engine_room.flywheel.backend.engine.LightStorage;
import dev.engine_room.flywheel.backend.gl.TextureBuffer;
import dev.engine_room.flywheel.backend.gl.buffer.GlBuffer;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;

public class InstancedLight {
	private final GlBuffer lut;
	private final GlBuffer sections;
	private final TextureBuffer lutTexture;
	private final TextureBuffer sectionsTexture;

	public InstancedLight() {
		lut = new GlBuffer();
		sections = new GlBuffer();
		lutTexture = new TextureBuffer(GL32.GL_R32UI);
		sectionsTexture = new TextureBuffer(GL32.GL_R32UI);
	}

	public void bind() {
		Samplers.LIGHT_LUT.makeActive();
		lutTexture.bind(lut.handle());
		Samplers.LIGHT_SECTIONS.makeActive();
		sectionsTexture.bind(sections.handle());
	}

	public void flush(LightStorage light) {
		if (light.capacity() == 0) {
			return;
		}

		light.upload(sections);

		if (light.checkNeedsLutRebuildAndClear()) {
			var lut = light.createLut();

			var up = MemoryBlock.malloc((long) lut.size() * Integer.BYTES);

			long ptr = up.ptr();

			for (int i = 0; i < lut.size(); i++) {
				MemoryUtil.memPutInt(ptr + (long) Integer.BYTES * i, lut.getInt(i));
			}

			this.lut.upload(up);

			up.free();
		}
	}

	public void delete() {
		lut.delete();
		sections.delete();
		lutTexture.delete();
		sectionsTexture.delete();
	}
}
