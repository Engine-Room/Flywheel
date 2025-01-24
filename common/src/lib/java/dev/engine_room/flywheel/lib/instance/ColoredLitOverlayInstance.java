package dev.engine_room.flywheel.lib.instance;

import dev.engine_room.flywheel.api.instance.InstanceHandle;
import dev.engine_room.flywheel.api.instance.InstanceType;
import net.minecraft.client.renderer.texture.OverlayTexture;

public abstract class ColoredLitOverlayInstance extends ColoredLitInstance {
	public int overlay = OverlayTexture.NO_OVERLAY;

	public ColoredLitOverlayInstance(InstanceType<? extends ColoredLitOverlayInstance> type, InstanceHandle handle) {
		super(type, handle);
	}

	public ColoredLitOverlayInstance overlay(int overlay) {
		this.overlay = overlay;
		return this;
	}
}
