package dev.engine_room.flywheel.lib.visual.text;

import net.minecraft.client.gui.Font;

public final class TextLayers {
	public static TextLayer normal(int color, Font.DisplayMode displayMode, int bias) {
		return new SimpleTextLayer.Builder().pattern(TextLayer.GlyphPattern.SINGLE)
				.material(TextLayer.GlyphMaterial.fromDisplayMode(displayMode))
				.color(TextLayer.GlyphColor.defaultTo(TextLayer.GlyphColor.adjustColor(color)))
				.bias(bias)
				.build();
	}

	public static TextLayer normal(int color, Font.DisplayMode displayMode) {
		return normal(color, displayMode, 0);
	}

	public static TextLayer dropShadow(int color, Font.DisplayMode displayMode, int bias) {
		return new SimpleTextLayer.Builder().pattern(TextLayer.GlyphPattern.SINGLE)
				.material(TextLayer.GlyphMaterial.fromDisplayMode(displayMode))
				.color(TextLayer.GlyphColor.defaultTo(TextLayer.GlyphColor.adjustColor(color), 0.25f))
				.offset(1, 1)
				.bias(bias)
				.build();
	}

	public static TextLayer dropShadow(int color, Font.DisplayMode displayMode) {
		return dropShadow(color, displayMode, 0);
	}

	public static TextLayer outline(int color, int bias) {
		return new SimpleTextLayer.Builder().pattern(TextLayer.GlyphPattern.OUTLINE)
				.material(TextLayer.GlyphMaterial.NORMAL)
				.color(TextLayer.GlyphColor.always(TextLayer.GlyphColor.adjustColor(color)))
				.bias(bias)
				.build();
	}

	public static TextLayer outline(int color) {
		return outline(color, 0);
	}

	private TextLayers() {
	}
}
