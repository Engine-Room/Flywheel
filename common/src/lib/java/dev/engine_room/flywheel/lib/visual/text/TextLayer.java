package dev.engine_room.flywheel.lib.visual.text;

import java.util.function.Consumer;

import org.joml.Vector3f;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.lib.material.CutoutShaders;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;

public interface TextLayer {
	float ONE_PIXEL = 0.125f;

	/**
	 * The style of individual glyphs.
	 *
	 * @return A GlyphMeshStyle.
	 */
	GlyphMeshStyle style();

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
	 * The instancer bias for this layer.
	 *
	 * @return The bias.
	 */
	int bias();

	/**
	 * The x offset of text content in this layer.
	 *
	 * @return The x offset.
	 */
	float offsetX();

	/**
	 * The y offset of text content in this layer.
	 *
	 * @return The y offset.
	 */
	float offsetY();

	/**
	 * The x offset of text effects such as strikethrough or underline in this layer.
	 *
	 * @return The x offset.
	 */
	float effectOffsetX();

	/**
	 * The y offset of text effects such as strikethrough or underline in this layer.
	 *
	 * @return The y offset.
	 */
	float effectOffsetY();

	@FunctionalInterface
	interface GlyphColor {
		/**
		 * Default to the given color if no color is specified in the style.
		 *
		 * @param color The ARGB color to default to.
		 * @return A new GlyphColor.
		 */
		static GlyphColor defaultTo(int color) {
			return style -> {
				TextColor textColor = style.getColor();
				if (textColor != null) {
					return adjustColor(textColor.getValue());
				}
				return color;
			};
		}

		/**
		 * Always use the given color, regardless of the style.
		 *
		 * @param color The ARGB color to use.
		 * @return A new GlyphColor.
		 */
		static GlyphColor always(int color) {
			return style -> color;
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
		 * Convert a style to a color.
		 *
		 * @param style The style of the text to colorize.
		 * @return The color to use, in ARGB format.
		 */
		int color(Style style);
	}

	@FunctionalInterface
	interface GlyphMaterial {
		GlyphMaterial SIMPLE = texture -> SimpleMaterial.builder()
				.texture(texture)
				.cutout(CutoutShaders.ONE_TENTH)
				.diffuse(false)
				.build();

		GlyphMaterial POLYGON_OFFSET = texture -> SimpleMaterial.builder()
				.texture(texture)
				.cutout(CutoutShaders.ONE_TENTH)
				.diffuse(false)
				.polygonOffset(true)
				.build();

		/**
		 * Create a Flywheel material for the given glyph texture.
		 *
		 * @param texture The texture to use.
		 * @return A material.
		 */
		Material create(ResourceLocation texture);
	}

	@FunctionalInterface
	interface GlyphMeshStyle {
		/**
		 * The standard style for glyphs with no repetition.
		 */
		GlyphMeshStyle SIMPLE = out -> out.accept(new Vector3f(0, 0, 0));

		/**
		 * The style for glyphs with a 8x outline as used by glowing text on signs.
		 */
		GlyphMeshStyle OUTLINE = out -> {
			for (int x = -1; x <= 1; ++x) {
				for (int y = -1; y <= 1; ++y) {
					if (x == 0 && y == 0) {
						continue;
					}

					out.accept(new Vector3f(x * ONE_PIXEL, y * ONE_PIXEL, 0));
				}
			}
		};

		/**
		 * Add quads to the mesh. Each vec3 submitted to out will be expanded
		 * into a unit quad in the XY plane with the lowest corner at the given vec3.
		 * You can think of each submitted vec3 as a duplication of a glyph.
		 *
		 * @param out The consumer to accept the quads
		 */
		void addQuads(Consumer<Vector3f> out);

	}
}
