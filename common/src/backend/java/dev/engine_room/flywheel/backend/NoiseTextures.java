package dev.engine_room.flywheel.backend;

import java.io.IOException;

import org.jetbrains.annotations.UnknownNullability;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.platform.NativeImage;

import dev.engine_room.flywheel.backend.gl.GlTextureUnit;
import dev.engine_room.flywheel.lib.util.IdentifierUtil;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public class NoiseTextures {
	public static final Identifier NOISE_TEXTURE = IdentifierUtil.id("textures/flywheel/noise/blue.png");

	@UnknownNullability
	public static DynamicTexture BLUE_NOISE;

	public static void reload(ResourceManager manager) {
		if (BLUE_NOISE != null) {
			BLUE_NOISE.close();
			BLUE_NOISE = null;
		}
		var optional = manager.getResource(NOISE_TEXTURE);

		if (optional.isEmpty()) {
			return;
		}

		try (var is = optional.get()
				.open()) {
			var image = NativeImage.read(NativeImage.Format.LUMINANCE, is);

			// TODO 1.21.11: maybe we should not use DynamicTexture here and do gen/upload manually
			BLUE_NOISE = new DynamicTexture(() -> "Flywheel Blue Noise", image);

			GlTextureUnit.T0.makeActive();
			GlStateManager._bindTexture(((GlTexture) BLUE_NOISE.getTexture()).glId());

			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_LINEAR);
			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_LINEAR);
			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GlConst.GL_REPEAT);
			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GlConst.GL_REPEAT);

			GlStateManager._bindTexture(0);
		} catch (IOException e) {

		}
	}
}
