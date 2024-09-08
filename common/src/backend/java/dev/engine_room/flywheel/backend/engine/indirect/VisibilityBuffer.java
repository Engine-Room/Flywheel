package dev.engine_room.flywheel.backend.engine.indirect;

import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL46;
import org.lwjgl.opengl.GL46C;

import com.mojang.blaze3d.platform.GlStateManager;

import dev.engine_room.flywheel.backend.FlwBackend;
import dev.engine_room.flywheel.backend.gl.GlTextureUnit;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import dev.engine_room.flywheel.lib.math.MoreMath;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.Minecraft;

public class VisibilityBuffer {
	private static final int READ_GROUP_SIZE = 16;
	private static final int ATTACHMENT = GL30.GL_COLOR_ATTACHMENT1;

	private final GlProgram readVisibilityProgram;
	private final ResizableStorageBuffer lastFrameVisibility;
	private final int textureId;

	private int lastWidth = -1;
	private int lastHeight = -1;

	private final IntSet attached = new IntArraySet();

	public VisibilityBuffer(GlProgram readVisibilityProgram) {
		this.readVisibilityProgram = readVisibilityProgram;
		lastFrameVisibility = new ResizableStorageBuffer();
		textureId = GL32.glGenTextures();

		GlStateManager._bindTexture(textureId);
		GlStateManager._texParameter(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_MIN_FILTER, GL32.GL_NEAREST);
		GlStateManager._texParameter(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_MAG_FILTER, GL32.GL_NEAREST);
		GlStateManager._texParameter(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_WRAP_S, GL32.GL_CLAMP_TO_EDGE);
		GlStateManager._texParameter(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_WRAP_T, GL32.GL_CLAMP_TO_EDGE);
	}

	public void read(int pageCount) {
		if (pageCount == 0) {
			return;
		}

		lastFrameVisibility.ensureCapacity((long) pageCount << 2);

		GL46.nglClearNamedBufferData(lastFrameVisibility.handle(), GL46.GL_R32UI, GL46.GL_RED_INTEGER, GL46.GL_UNSIGNED_INT, 0);

		if (lastWidth == -1 || lastHeight == -1) {
			return;
		}

		readVisibilityProgram.bind();
		bind();

		GlTextureUnit.T0.makeActive();
		GlStateManager._bindTexture(textureId);

		GL46.glDispatchCompute(MoreMath.ceilingDiv(lastWidth, READ_GROUP_SIZE), MoreMath.ceilingDiv(lastHeight, READ_GROUP_SIZE), 1);
	}

	public void bind() {
		GL46.glBindBufferBase(GL46.GL_SHADER_STORAGE_BUFFER, BufferBindings.LAST_FRAME_VISIBILITY, lastFrameVisibility.handle());
	}

	public void attach() {
		var mainRenderTarget = Minecraft.getInstance()
				.getMainRenderTarget();

		setupTexture(mainRenderTarget.width, mainRenderTarget.height);

		if (attached.add(mainRenderTarget.frameBufferId)) {
			GL46.glNamedFramebufferTexture(mainRenderTarget.frameBufferId, ATTACHMENT, textureId, 0);

			try {
				mainRenderTarget.checkStatus();
			} catch (Exception e) {
				FlwBackend.LOGGER.error("Error attaching visbuffer", e);
			}
		}

		// Enable writes
		GL46.glNamedFramebufferDrawBuffers(mainRenderTarget.frameBufferId, new int[] { GL30.GL_COLOR_ATTACHMENT0, ATTACHMENT });
	}

	public void detach() {
		var mainRenderTarget = Minecraft.getInstance()
				.getMainRenderTarget();

		// Disable writes
		GL46.glNamedFramebufferDrawBuffers(mainRenderTarget.frameBufferId, new int[] { GL30.GL_COLOR_ATTACHMENT0 });
	}

	public void delete() {
		GL32.glDeleteTextures(textureId);
	}

	public void clear() {
		if (lastWidth == -1 || lastHeight == -1) {
			return;
		}

		GL46C.nglClearTexImage(textureId, 0, GL32.GL_RED_INTEGER, GL32.GL_UNSIGNED_INT, 0);
	}

	private void setupTexture(int width, int height) {
		if (lastWidth == width && lastHeight == height) {
			return;
		}

		// Need to rebind to all fbos because an attachment becomes incomplete when it's resized
		attached.clear();

		lastWidth = width;
		lastHeight = height;

		GlTextureUnit.T0.makeActive();
		GlStateManager._bindTexture(textureId);

		// TODO: DSA texture storage?
		GL32.glTexImage2D(GL32.GL_TEXTURE_2D, 0, GL32.GL_R32UI, width, height, 0, GL32.GL_RED_INTEGER, GL32.GL_UNSIGNED_INT, 0);
		GlStateManager._bindTexture(0);
	}
}
