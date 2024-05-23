package dev.engine_room.flywheel.backend.engine.embed;

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
import static org.lwjgl.opengl.GL14.GL_MIRRORED_REPEAT;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL30;

import dev.engine_room.flywheel.backend.gl.GlTexture;
import net.minecraft.util.Mth;

public class EmbeddedLightTexture {
	@Nullable
	private GlTexture texture;

	public int sizeX;
	public int sizeY;
	public int sizeZ;

	public void bind() {

		texture().bind();
	}

	private GlTexture texture() {
		if (texture == null) {
			texture = new GlTexture(GL_TEXTURE_3D);
		}
		return texture;
	}

	public void ensureCapacity(int sizeX, int sizeY, int sizeZ) {
		sizeX = Mth.smallestEncompassingPowerOfTwo(sizeX);
		sizeY = Mth.smallestEncompassingPowerOfTwo(sizeY);
		sizeZ = Mth.smallestEncompassingPowerOfTwo(sizeZ);

		if (sizeX > this.sizeX || sizeY > this.sizeY || sizeZ > this.sizeZ) {
			this.sizeX = sizeX;
			this.sizeY = sizeY;
			this.sizeZ = sizeZ;

			glTexImage3D(GL_TEXTURE_3D, 0, GL30.GL_RG8, sizeX, sizeY, sizeZ, 0, GL30.GL_RG, GL_UNSIGNED_BYTE, 0);

			glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
			glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_MIRRORED_REPEAT);
			glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
		}
	}

	public void upload(long ptr, int sizeX, int sizeY, int sizeZ) {
		glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
		glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
		glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
		glPixelStorei(GL_UNPACK_SKIP_IMAGES, 0);
		glPixelStorei(GL_UNPACK_IMAGE_HEIGHT, 0);
		glPixelStorei(GL_UNPACK_ALIGNMENT, (int) EmbeddedLightVolume.STRIDE);

		glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, 0, sizeX, sizeY, sizeZ, GL30.GL_RG, GL_UNSIGNED_BYTE, ptr);

		glPixelStorei(GL_UNPACK_ALIGNMENT, 4); // 4 is the default
	}

	public void delete() {
		if (texture != null) {
			texture.delete();
		}
	}
}
