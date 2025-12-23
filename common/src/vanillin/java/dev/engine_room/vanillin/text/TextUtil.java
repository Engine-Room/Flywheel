package dev.engine_room.vanillin.text;

import dev.engine_room.vanillin.mixin.text.FontAccessor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.Identifier;

public class TextUtil {
	public static FontSet getFontSet(Font font, Identifier id) {
		return ((FontAccessor) font).flywheel$getFontSet(id);
	}

	public static boolean getFilterFishyGlyphs(Font font) {
		return ((FontAccessor) font).flywheel$getFilterFishyGlyphs();
	}

	public static BakedGlyphExtension getBakedGlyphExtension(BakedGlyph glyph) {
		return (BakedGlyphExtension) glyph;
	}
}
