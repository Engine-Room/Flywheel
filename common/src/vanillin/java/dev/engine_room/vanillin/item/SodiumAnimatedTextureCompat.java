package dev.engine_room.vanillin.item;

import dev.engine_room.vanillin.VanillinXplat;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceSets;
import net.caffeinemc.mods.sodium.client.render.texture.SpriteUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * An attempt to be compatible with sodium's animated texture optimization.
 *
 * <p>Unfortunately Vanillin does not have high time resolution as to what models
 * are on the screen, so once a sprite is observed we'll keep marking it as active
 * until the next renderer reload.
 *
 * <p>This should probably be implemented in Flywheel proper with an API on
 * Mesh to get a list of TextureAtlasSprites. That way a backend would be able
 * to decide which sprites are active based on instance counts, visibility, etc.
 * I have a feeling such an API could be useful otherwise too.
 */
public class SodiumAnimatedTextureCompat {
	private static final ReferenceSet<TextureAtlasSprite> VISIBLE = ReferenceSets.synchronize(new ReferenceArraySet<>());

	private static final boolean IS_SODIUM_LOADED = VanillinXplat.INSTANCE.isModLoaded("sodium");

	public static void add(TextureAtlasSprite sprite) {
		if (IS_SODIUM_LOADED) {
			Internals.add(sprite);
		}
	}

	public static void beginFrame() {
		if (IS_SODIUM_LOADED) {
			Internals.beginFrame();
		}
	}

	public static void onReloadRenderer() {
		VISIBLE.clear();
	}

	private static final class Internals {
		private static void add(TextureAtlasSprite sprite) {
			if (SpriteUtil.hasAnimation(sprite)) {
				VISIBLE.add(sprite);
			}
		}

		private static void beginFrame() {
			for (var sprite : VISIBLE) {
				SpriteUtil.markSpriteActive(sprite);
			}
		}
	}
}
