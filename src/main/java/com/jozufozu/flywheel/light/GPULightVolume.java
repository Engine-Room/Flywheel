package com.jozufozu.flywheel.light;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ROW_LENGTH;
import static org.lwjgl.opengl.GL11.GL_UNPACK_SKIP_PIXELS;
import static org.lwjgl.opengl.GL11.GL_UNPACK_SKIP_ROWS;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_3D;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL12.GL_UNPACK_IMAGE_HEIGHT;
import static org.lwjgl.opengl.GL12.GL_UNPACK_SKIP_IMAGES;
import static org.lwjgl.opengl.GL12.glTexImage3D;
import static org.lwjgl.opengl.GL12.glTexSubImage3D;
import static org.lwjgl.opengl.GL14.GL_MIRRORED_REPEAT;

import org.lwjgl.opengl.GL30;

import com.jozufozu.flywheel.backend.gl.GlTexture;
import com.jozufozu.flywheel.backend.gl.GlTextureUnit;
import com.jozufozu.flywheel.util.box.GridAlignedBB;
import com.jozufozu.flywheel.util.box.ImmutableBox;

import net.minecraft.world.level.BlockAndTintGetter;

public class GPULightVolume extends LightVolume {

	protected final GridAlignedBB sampleVolume = new GridAlignedBB();
	private final GlTexture glTexture;

	private final GlTextureUnit textureUnit = GlTextureUnit.T4;
	protected boolean bufferDirty;

	public GPULightVolume(BlockAndTintGetter level, ImmutableBox sampleVolume) {
		super(level, sampleVolume);
		this.sampleVolume.assign(sampleVolume);

		glTexture = new GlTexture(GL_TEXTURE_3D);

		GlTextureUnit oldState = GlTextureUnit.getActive();

		// allocate space for the texture
		textureUnit.makeActive();
		glTexture.bind();

		int sizeX = box.sizeX();
		int sizeY = box.sizeY();
		int sizeZ = box.sizeZ();
		glTexImage3D(GL_TEXTURE_3D, 0, GL30.GL_RG8, sizeX, sizeY, sizeZ, 0, GL30.GL_RG, GL_UNSIGNED_BYTE, 0);

		glTexture.setParameteri(GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexture.setParameteri(GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexture.setParameteri(GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
		glTexture.setParameteri(GL_TEXTURE_WRAP_R, GL_MIRRORED_REPEAT);
		glTexture.setParameteri(GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);

		glTexture.unbind();
		oldState.makeActive();
	}

	@Override
	protected void setBox(ImmutableBox box) {
		this.box.assign(box);
		this.box.nextPowerOf2Centered();
		// called during super ctor
		if (sampleVolume != null) this.sampleVolume.assign(box);
	}

	public void bind() {
		// just in case something goes wrong, or we accidentally call this before this volume is properly disposed of.
		if (lightData == null || lightData.capacity() == 0) return;

		textureUnit.makeActive();
		glTexture.bind();

		uploadTexture();
	}

	private void uploadTexture() {
		if (bufferDirty) {
			glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
			glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
			glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
			glPixelStorei(GL_UNPACK_SKIP_IMAGES, 0);
			glPixelStorei(GL_UNPACK_IMAGE_HEIGHT, 0);
			glPixelStorei(GL_UNPACK_ALIGNMENT, 2); // we use 2 bytes per texel
			int sizeX = box.sizeX();
			int sizeY = box.sizeY();
			int sizeZ = box.sizeZ();

			glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, 0, sizeX, sizeY, sizeZ, GL30.GL_RG, GL_UNSIGNED_BYTE, lightData);

			glPixelStorei(GL_UNPACK_ALIGNMENT, 4); // 4 is the default
			bufferDirty = false;
		}
	}

	public void unbind() {
		glTexture.unbind();
	}

	@Override
	public void delete() {
		super.delete();
		glTexture.delete();
	}

	public void move(ImmutableBox newSampleVolume) {
		if (lightData == null) return;

		if (box.contains(newSampleVolume)) {
			sampleVolume.assign(newSampleVolume);
			initialize();
		} else {
			super.move(newSampleVolume);
		}
	}

	@Override
	public ImmutableBox getVolume() {
		return sampleVolume;
	}

	@Override
	protected void markDirty() {
		this.bufferDirty = true;
	}
}
