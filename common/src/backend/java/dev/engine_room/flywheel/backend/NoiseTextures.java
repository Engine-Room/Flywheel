package dev.engine_room.flywheel.backend;

import java.io.IOException;

import org.jetbrains.annotations.UnknownNullability;

import com.mojang.blaze3d.platform.NativeImage;

import dev.engine_room.flywheel.api.Flywheel;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class NoiseTextures {

	public static final ResourceLocation NOISE_TEXTURE = Flywheel.rl("textures/flywheel/noise/blue/0.png");
	public static final int NOISE_LAYERS = 16;

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
		} catch (IOException e) {

		}
	}
}
