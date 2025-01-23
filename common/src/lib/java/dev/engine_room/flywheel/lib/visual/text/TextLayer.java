package dev.engine_room.flywheel.lib.visual.text;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2fc;

import dev.engine_room.flywheel.api.material.DepthTest;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.material.Transparency;
import dev.engine_room.flywheel.api.material.WriteMask;
import dev.engine_room.flywheel.lib.material.CutoutShaders;
import dev.engine_room.flywheel.lib.material.FogShaders;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

public interface TextLayer {
	/**
	 * The pattern of individual glyphs.
	 *
	 * @return A GlyphPattern.
	 */
	GlyphPattern pattern();

	/**
	 * A mapping from texture ResourceLocations to Flywheel materials.
	 *
	 * @return A GlyphMaterial.
	 */
	GlyphMaterial material();

	/**
	 * A mapping from text styles to ARGB colors.
	 *
	 * @return A GlyphColor.
	 */
	GlyphColor color();

	/**
	 * The offset of text in this layer.
	 *
	 * @return The offset.
	 */
	Vector2fc offset();

	/**
	 * The instancer bias for this layer.
	 *
	 * @return The bias.
	 */
	int bias();

	// TODO: probably just convert this to Iterable<Vector2fc>
	@FunctionalInterface
	interface GlyphPattern {
		/**
		 * The pattern for a single glyph with no offset.
		 */
		GlyphPattern SINGLE = out -> out.accept(new Vector2f(0, 0));

		/**
		 * The pattern for an 8x outline as used by glowing text on signs.
		 */
		GlyphPattern OUTLINE = out -> {
			for (int x = -1; x <= 1; ++x) {
				for (int y = -1; y <= 1; ++y) {
					if (x == 0 && y == 0) {
						continue;
					}

					out.accept(new Vector2f(x, y));
				}
			}
		};

		/**
		 * Add an arbitrary amount of glyphs. Each accepted vector represents
		 * the offset of a new glyph quad.
		 *
		 * @param out The consumer to accept the offset of a new glyph quad
		 */
		void addGlyphs(Consumer<Vector2fc> out);
	}

	@FunctionalInterface
	interface GlyphMaterial {
		// FIXME: account for intensity
		GlyphMaterial NORMAL = texture -> SimpleMaterial.builder()
				.cutout(CutoutShaders.ONE_TENTH)
				.texture(texture)
				.mipmap(false)
				.transparency(Transparency.TRANSLUCENT)
				.diffuse(false)
				.build();

		GlyphMaterial SEE_THROUGH = texture -> SimpleMaterial.builder()
				.fog(FogShaders.NONE)
				.cutout(CutoutShaders.ONE_TENTH)
				.texture(texture)
				.mipmap(false)
				.depthTest(DepthTest.ALWAYS)
				.transparency(Transparency.TRANSLUCENT)
				.writeMask(WriteMask.COLOR)
				.diffuse(false)
				.build();

		GlyphMaterial POLYGON_OFFSET = texture -> SimpleMaterial.builder()
				.cutout(CutoutShaders.ONE_TENTH)
				.texture(texture)
				.mipmap(false)
				.polygonOffset(true)
				.transparency(Transparency.TRANSLUCENT)
				.diffuse(false)
				.build();

		static GlyphMaterial fromDisplayMode(Font.DisplayMode displayMode) {
			return switch (displayMode) {
				case NORMAL -> NORMAL;
				case SEE_THROUGH -> SEE_THROUGH;
				case POLYGON_OFFSET -> POLYGON_OFFSET;
			};
		}

		/**
		 * Create a Flywheel material for the given glyph texture.
		 *
		 * @param texture The texture to use.
		 * @return A material.
		 */
		Material create(ResourceLocation texture);
	}

	@FunctionalInterface
	interface GlyphColor {
		/**
		 * Default to the given color if no color is specified in the style.
		 *
		 * @param color The ARGB color to default to.
		 * @return A new GlyphColor.
		 */
		static GlyphColor defaultTo(int color, float dimFactor) {
			int finalColor;
			if (dimFactor != 1.0f) {
				finalColor = FastColor.ARGB32.color(
						FastColor.ARGB32.alpha(color),
						(int) (FastColor.ARGB32.red(color) * dimFactor),
						(int) (FastColor.ARGB32.green(color) * dimFactor),
						(int) (FastColor.ARGB32.blue(color) * dimFactor)
				);
			} else {
				finalColor = color;
			}

			return textColor -> {
				if (textColor != null) {
					int textColorArgb = textColor.getValue();
					if (dimFactor != 1.0f) {
						return FastColor.ARGB32.color(
								FastColor.ARGB32.alpha(finalColor),
								(int) (FastColor.ARGB32.red(textColorArgb) * dimFactor),
								(int) (FastColor.ARGB32.green(textColorArgb) * dimFactor),
								(int) (FastColor.ARGB32.blue(textColorArgb) * dimFactor)
						);
					} else {
						return (finalColor & 0xFF000000) | (textColorArgb & 0xFFFFFF);
					}
				}
				return finalColor;
			};
		}

		/**
		 * Default to the given color if no color is specified in the style.
		 *
		 * @param color The ARGB color to default to.
		 * @return A new GlyphColor.
		 */
		static GlyphColor defaultTo(int color) {
			return defaultTo(color, 1.0f);
		}

		/**
		 * Always use the given color, regardless of the style.
		 *
		 * @param color The ARGB color to use.
		 * @return A new GlyphColor.
		 */
		static GlyphColor always(int color) {
			return textColor -> color;
		}

		/**
		 * Adjust the color to be fully opaque if it's very close to having 0 alpha.
		 *
		 * @param color The ARGB color to adjust.
		 * @return The adjusted color.
		 */
		static int adjustColor(int color) {
			if ((color & 0xFC000000) == 0) {
				return color | 0xFF000000;
			}
			return color;
		}

		/**
		 * Convert a nullable text color to a color.
		 *
		 * @param textColor The color of the text to colorize.
		 * @return The color to use, in ARGB format.
		 */
		int color(@Nullable TextColor textColor);
	}
}
