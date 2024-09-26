package dev.engine_room.flywheel.lib.instance;

import org.joml.Matrix4f;

import dev.engine_room.flywheel.api.instance.InstanceHandle;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.lib.internal.FlwLibLink;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.util.FastColor;

public class GlyphInstance extends AbstractInstance {
	// Skew x by 1 - 0.25 * y
	// Note that columns are written as rows.
	private static final Matrix4f ITALIC_SKEW = new Matrix4f(1, 0, 0, 0, -0.25f, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 1);

	public final Matrix4f pose = new Matrix4f();

	public float u0;
	public float u1;
	public float v0;
	public float v1;

	public byte red = (byte) 0xFF;
	public byte green = (byte) 0xFF;
	public byte blue = (byte) 0xFF;
	public byte alpha = (byte) 0xFF;

	public int light = 0;

	public GlyphInstance(InstanceType<?> type, InstanceHandle handle) {
		super(type, handle);
	}

	public GlyphInstance setGlyph(BakedGlyph glyph, float x, float y, boolean italic) {
		var glyphReader = FlwLibLink.INSTANCE.getGlyphExtension(glyph);

		u0 = glyphReader.flywheel$u0();
		u1 = glyphReader.flywheel$u1();
		v0 = glyphReader.flywheel$v0();
		v1 = glyphReader.flywheel$v1();
		float left = glyphReader.flywheel$left();
		float right = glyphReader.flywheel$right();
		float up = glyphReader.flywheel$up();
		float down = glyphReader.flywheel$down();

		pose.translate(x + left, y + up - 3.0f, 0.0f);
		pose.scale(right - left, down - up, 1.0f);

		if (italic) {
			pose.mul(ITALIC_SKEW);
		}

		return this;
	}

	public GlyphInstance setEffect(BakedGlyph glyph, float x0, float y0, float x1, float y1, float depth) {
		var glyphReader = FlwLibLink.INSTANCE.getGlyphExtension(glyph);

		u0 = glyphReader.flywheel$u0();
		u1 = glyphReader.flywheel$u1();
		v0 = glyphReader.flywheel$v0();
		v1 = glyphReader.flywheel$v1();

		pose.translate(x0, y0, depth);
		pose.scale(x1 - x0, y1 - y0, 1.0f);

		return this;
	}

	public GlyphInstance colorArgb(int argb) {
		return color(FastColor.ARGB32.red(argb), FastColor.ARGB32.green(argb), FastColor.ARGB32.blue(argb), FastColor.ARGB32.alpha(argb));
	}

	public GlyphInstance colorRgb(int rgb) {
		return color(FastColor.ARGB32.red(rgb), FastColor.ARGB32.green(rgb), FastColor.ARGB32.blue(rgb));
	}

	public GlyphInstance color(int red, int green, int blue, int alpha) {
		return color((byte) red, (byte) green, (byte) blue, (byte) alpha);
	}

	public GlyphInstance color(int red, int green, int blue) {
		return color((byte) red, (byte) green, (byte) blue);
	}

	public GlyphInstance color(float red, float green, float blue, float alpha) {
		return color((byte) (red * 255f), (byte) (green * 255f), (byte) (blue * 255f), (byte) (alpha * 255f));
	}

	public GlyphInstance color(byte red, byte green, byte blue, byte alpha) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
		return this;
	}

	public GlyphInstance color(byte red, byte green, byte blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		return this;
	}
}
