package dev.engine_room.flywheel.lib.instance;

import org.joml.Matrix4f;

import dev.engine_room.flywheel.api.instance.InstanceHandle;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.lib.internal.FlwLibLink;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.util.FastColor;

public class GlyphInstance extends AbstractInstance {
	public final Matrix4f pose = new Matrix4f();

	public float u0;
	public float u1;
	public float v0;
	public float v1;

	public float x0;
	public float x1;
	public float x2;
	public float x3;

	public float y0;
	public float y1;

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

		float f = x + left;
		float g = x + right;
		float h = up - 3.0f;
		float j = down - 3.0f;
		float k = y + h;
		float l = y + j;
		float m = italic ? 1.0f - 0.25f * h : 0.0f;
		float n = italic ? 1.0f - 0.25f * j : 0.0f;

		y0 = k;
		y1 = l;

		x0 = f + m;
		x1 = f + n;
		x2 = g + n;
		x3 = g + m;

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
