package dev.engine_room.flywheel.lib.instance;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import dev.engine_room.flywheel.api.instance.InstanceHandle;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.lib.internal.BakedGlyphExtension;
import dev.engine_room.flywheel.lib.internal.FlwLibLink;
import dev.engine_room.flywheel.lib.math.DataPacker;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;

public class GlyphInstance extends ColoredLitInstance {
	// Skew x by 1 - 0.25 * y
	// Note that columns are written as rows.
	private static final Matrix4fc ITALIC_SKEW = new Matrix4f(1, 0, 0, 0, -0.25f, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 1);

	public final Matrix4f pose = new Matrix4f();

	public int packedUs;
	public int packedVs;

	public GlyphInstance(InstanceType<? extends GlyphInstance> type, InstanceHandle handle) {
		super(type, handle);
	}

	public GlyphInstance setGlyph(BakedGlyph glyph, Matrix4fc initialPose, float x, float y, boolean italic) {
		var glyphExtension = FlwLibLink.INSTANCE.getBakedGlyphExtension(glyph);
		setUvs(glyphExtension);

		float left = glyphExtension.flywheel$left();
		float up = glyphExtension.flywheel$up();

		pose.set(initialPose);
		pose.translate(x, y, 0.0f);

		if (italic) {
			pose.mul(ITALIC_SKEW);
		}

		pose.translate(left, up - 3.0f, 0.0f);

		return this;
	}

	public GlyphInstance setEffect(BakedGlyph glyph, Matrix4fc initialPose, float x0, float y0, float x1, float y1, float depth) {
		var glyphExtension = FlwLibLink.INSTANCE.getBakedGlyphExtension(glyph);
		setUvs(glyphExtension);

		pose.set(initialPose);
		pose.translate(x0, y0, depth);
		pose.scale(x1 - x0, y1 - y0, 1.0f);

		return this;
	}

	private void setUvs(BakedGlyphExtension glyphExtension) {
		float u0 = glyphExtension.flywheel$u0();
		float u1 = glyphExtension.flywheel$u1();
		float v0 = glyphExtension.flywheel$v0();
		float v1 = glyphExtension.flywheel$v1();

		// Need to make sure at least u0/v0 don't get their sign bit extended in the cast.
		// It causes u1/v1 to be completely saturated.
		packedUs = (Short.toUnsignedInt(DataPacker.packNormU16(u1)) << 16) | Short.toUnsignedInt(DataPacker.packNormU16(u0));
		packedVs = (Short.toUnsignedInt(DataPacker.packNormU16(v1)) << 16) | Short.toUnsignedInt(DataPacker.packNormU16(v0));
	}
}
