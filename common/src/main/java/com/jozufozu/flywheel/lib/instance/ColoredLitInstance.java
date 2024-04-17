package com.jozufozu.flywheel.lib.instance;

import com.jozufozu.flywheel.api.instance.InstanceHandle;
import com.jozufozu.flywheel.api.instance.InstanceType;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;

public abstract class ColoredLitInstance extends AbstractInstance implements FlatLit {
	public byte r = (byte) 0xFF;
	public byte g = (byte) 0xFF;
	public byte b = (byte) 0xFF;
	public byte a = (byte) 0xFF;

	public int packedLight;

	public int overlay = OverlayTexture.NO_OVERLAY;

	public ColoredLitInstance(InstanceType<? extends ColoredLitInstance> type, InstanceHandle handle) {
		super(type, handle);
	}

	public ColoredLitInstance setColor(int color) {
		return setColor(color, false);
	}

	public ColoredLitInstance setColor(int color, boolean alpha) {
		byte r = (byte) ((color >> 16) & 0xFF);
		byte g = (byte) ((color >> 8) & 0xFF);
		byte b = (byte) (color & 0xFF);

		if (alpha) {
			byte a = (byte) ((color >> 24) & 0xFF);
			return setColor(r, g, b, a);
		} else {
			return setColor(r, g, b);
		}
	}

	public ColoredLitInstance setColor(int r, int g, int b) {
		return setColor((byte) r, (byte) g, (byte) b);
	}

	public ColoredLitInstance setColor(byte r, byte g, byte b) {
		this.r = r;
		this.g = g;
		this.b = b;
		return this;
	}

	public ColoredLitInstance setColor(byte r, byte g, byte b, byte a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		return this;
	}

	@Override
	public ColoredLitInstance light(int blockLight, int skyLight) {
		return light(LightTexture.pack(blockLight, skyLight));
	}

	@Override
	public ColoredLitInstance light(int packedLight) {
		this.packedLight = packedLight;
		return this;
	}

	public ColoredLitInstance setOverlay(int overlay) {
		this.overlay = overlay;
		return this;
	}
}
