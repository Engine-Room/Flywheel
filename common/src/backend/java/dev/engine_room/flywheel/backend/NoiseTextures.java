package dev.engine_room.flywheel.backend;

import java.io.IOException;

import dev.engine_room.flywheel.lib.util.IdentifierUtil;

import org.jetbrains.annotations.UnknownNullability;
import org.lwjgl.opengl.GL32;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.engine_room.flywheel.backend.gl.GlTextureUnit;
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

			BLUE_NOISE = new DynamicTexture(image);

			GlTextureUnit.T0.makeActive();
			BLUE_NOISE.bind();

			NoiseTextures.BLUE_NOISE.setFilter(true, false);
			RenderSystem.texParameter(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_WRAP_S, GL32.GL_REPEAT);
			RenderSystem.texParameter(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_WRAP_T, GL32.GL_REPEAT);

			RenderSystem.bindTexture(0);
		} catch (IOException e) {

		}
	}
}
