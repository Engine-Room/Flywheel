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
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_3D;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL12.GL_UNPACK_IMAGE_HEIGHT;
import static org.lwjgl.opengl.GL12.GL_UNPACK_SKIP_IMAGES;
import static org.lwjgl.opengl.GL12.glTexImage3D;
import static org.lwjgl.opengl.GL12.glTexSubImage3D;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE4;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL14.GL_MIRRORED_REPEAT;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlTexture;
import com.jozufozu.flywheel.backend.gl.GlTextureUnit;
import com.jozufozu.flywheel.backend.gl.versioned.RGPixelFormat;

import net.minecraft.world.level.LightLayer;

public class GPULightVolume extends LightVolume {

	protected final GridAlignedBB sampleVolume = new GridAlignedBB();
	private final GlTexture glTexture;

	private final RGPixelFormat pixelFormat;
	private final GlTextureUnit textureUnit = GlTextureUnit.T4;
	protected boolean bufferDirty;

	public GPULightVolume(ImmutableBox sampleVolume) {
		super(sampleVolume);
		this.sampleVolume.assign(sampleVolume);

		pixelFormat = Backend.getInstance().compat.pixelFormat;
		glTexture = new GlTexture(GL_TEXTURE_3D);

		// allocate space for the texture
		glActiveTexture(GL_TEXTURE4);
		glTexture.bind();

		int sizeX = box.sizeX();
		int sizeY = box.sizeY();
		int sizeZ = box.sizeZ();
		glTexImage3D(GL_TEXTURE_3D, 0, pixelFormat.internalFormat(), sizeX, sizeY, sizeZ, 0, pixelFormat.format(), GL_UNSIGNED_BYTE, 0);

		glTexture.unbind();
		glActiveTexture(GL_TEXTURE0);
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
		if (lightData == null) return;

		textureUnit.makeActive();
		glTexture.bind();
		glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
		glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_MIRRORED_REPEAT);
		glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);

		uploadTexture();
	}

	private void uploadTexture() {
		if (bufferDirty) {
			glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
			glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
			glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
			glPixelStorei(GL_UNPACK_SKIP_IMAGES, 0);
			glPixelStorei(GL_UNPACK_IMAGE_HEIGHT, 0);
			glPixelStorei(GL_UNPACK_ALIGNMENT, 2);
			int sizeX = box.sizeX();
			int sizeY = box.sizeY();
			int sizeZ = box.sizeZ();

			glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, 0, sizeX, sizeY, sizeZ, pixelFormat.format(), GL_UNSIGNED_BYTE, lightData);

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

	public void move(LightProvider world, ImmutableBox newSampleVolume) {
		if (lightData == null) return;

		if (box.contains(newSampleVolume)) {
			if (newSampleVolume.intersects(sampleVolume)) {
				GridAlignedBB newArea = newSampleVolume.intersect(sampleVolume);
				sampleVolume.assign(newSampleVolume);

				copyLight(world, newArea);
			} else {
				sampleVolume.assign(newSampleVolume);
				initialize(world);
			}
		} else {
			super.move(world, newSampleVolume);
		}
	}

	public void onLightUpdate(LightProvider world, LightLayer type, ImmutableBox changedVolume) {
		super.onLightUpdate(world, type, changedVolume);
		bufferDirty = true;
	}

	public void onLightPacket(LightProvider world, int chunkX, int chunkZ) {
		super.onLightPacket(world, chunkX, chunkZ);
		bufferDirty = true;
	}

	/**
	 * Completely (re)populate this volume with block and sky lighting data.
	 * This is expensive and should be avoided.
	 */
	public void initialize(LightProvider world) {
		super.initialize(world);
		bufferDirty = true;
	}

	@Override
	protected int getStride() {
		return Backend.getInstance().compat.pixelFormat.byteCount();
	}

	@Override
	public ImmutableBox getVolume() {
		return sampleVolume;
	}
}
