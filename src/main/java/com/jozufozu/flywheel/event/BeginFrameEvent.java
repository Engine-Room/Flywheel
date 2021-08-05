package com.jozufozu.flywheel.event;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.eventbus.api.Event;

public class BeginFrameEvent extends Event {
	private final ClientWorld world;
	private final ActiveRenderInfo info;
	private final ClippingHelper clippingHelper;

	public BeginFrameEvent(ClientWorld world, ActiveRenderInfo info, ClippingHelper clippingHelper) {
		this.world = world;
		this.info = info;
		this.clippingHelper = clippingHelper;
	}

	public ClientWorld getWorld() {
		return world;
	}

	public ActiveRenderInfo getInfo() {
		return info;
	}

	public ClippingHelper getClippingHelper() {
		return clippingHelper;
	}

	public Vector3d getCameraPos() {
		return info.getPosition();
	}
}
