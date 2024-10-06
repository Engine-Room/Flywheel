package dev.engine_room.flywheel.lib.instance;

import dev.engine_room.flywheel.api.instance.InstanceHandle;
import dev.engine_room.flywheel.api.instance.InstanceType;
import net.minecraft.util.FastColor;

public abstract class ColoredLitInstance extends AbstractInstance implements FlatLit {
	public byte red = (byte) 0xFF;
	public byte green = (byte) 0xFF;
	public byte blue = (byte) 0xFF;
	public byte alpha = (byte) 0xFF;

	public int light = 0;

	public ColoredLitInstance(InstanceType<? extends ColoredLitInstance> type, InstanceHandle handle) {
		super(type, handle);
	}

	public ColoredLitInstance colorArgb(int argb) {
		return color(FastColor.ARGB32.red(argb), FastColor.ARGB32.green(argb), FastColor.ARGB32.blue(argb), FastColor.ARGB32.alpha(argb));
	}

	public ColoredLitInstance colorRgb(int rgb) {
		return color(FastColor.ARGB32.red(rgb), FastColor.ARGB32.green(rgb), FastColor.ARGB32.blue(rgb));
	}

	public ColoredLitInstance color(int red, int green, int blue, int alpha) {
		return color((byte) red, (byte) green, (byte) blue, (byte) alpha);
	}

	public ColoredLitInstance color(int red, int green, int blue) {
		return color((byte) red, (byte) green, (byte) blue);
	}

	public ColoredLitInstance color(byte red, byte green, byte blue, byte alpha) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
		return this;
	}

	public ColoredLitInstance color(byte red, byte green, byte blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		return this;
	}

	public ColoredLitInstance color(float red, float green, float blue, float alpha) {
		return color((byte) (red * 255f), (byte) (green * 255f), (byte) (blue * 255f), (byte) (alpha * 255f));
	}

	public ColoredLitInstance color(float red, float green, float blue) {
		return color((byte) (red * 255f), (byte) (green * 255f), (byte) (blue * 255f));
	}

	@Override
	public ColoredLitInstance light(int light) {
		this.light = light;
		return this;
	}
}
