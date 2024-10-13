package dev.engine_room.flywheel.impl;

import com.mojang.blaze3d.font.SheetGlyphInfo;

/**
 * For use in {@link dev.engine_room.flywheel.impl.mixin.text.FontTextureMixin}
 * to batch glyph uploads when they're created in a flywheel worker thread.
 */
public record FontTextureUpload(SheetGlyphInfo info, int x, int y) {
}
