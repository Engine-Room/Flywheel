package dev.engine_room.flywheel.lib.instance;

import dev.engine_room.flywheel.api.instance.InstanceHandle;
import dev.engine_room.flywheel.api.instance.InstanceType;
import net.minecraft.client.renderer.texture.OverlayTexture;

public abstract class ColoredLitInstance extends AbstractInstance implements FlatLit {
	public byte r = (byte) 0xFF;
	public byte g = (byte) 0xFF;
	public byte b = (byte) 0xFF;
	public byte a = (byte) 0xFF;

	public int overlay = OverlayTexture.NO_OVERLAY;
	public int light = 0;

	public ColoredLitInstance(InstanceType<? extends ColoredLitInstance> type, InstanceHandle handle) {
		super(type, handle);
	}

	public ColoredLitInstance color(int color) {
		return color(color, false);
	}

	public ColoredLitInstance color(int color, boolean alpha) {
		byte r = (byte) ((color >> 16) & 0xFF);
		byte g = (byte) ((color >> 8) & 0xFF);
		byte b = (byte) (color & 0xFF);

		if (alpha) {
			byte a = (byte) ((color >> 24) & 0xFF);
			return color(r, g, b, a);
		} else {
			return color(r, g, b);
		}
	}

	public ColoredLitInstance color(int r, int g, int b) {
		return color((byte) r, (byte) g, (byte) b);
	}

	public ColoredLitInstance color(byte r, byte g, byte b) {
		this.r = r;
		this.g = g;
		this.b = b;
		return this;
	}

	public ColoredLitInstance color(byte r, byte g, byte b, byte a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		return this;
	}

	public ColoredLitInstance overlay(int overlay) {
		this.overlay = overlay;
		return this;
	}

	@Override
	public ColoredLitInstance light(int light) {
		this.light = light;
		return this;
	}
}
