package com.jozufozu.flywheel.event;

import com.jozufozu.flywheel.fabric.event.EventContext;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;

public class BeginFrameEvent extends EventContext {
	private final ClientLevel world;
	private final PoseStack stack;
	private final Camera info;
	private final GameRenderer gameRenderer;
	private final LightTexture lightTexture;
	private final Frustum clippingHelper;

	public BeginFrameEvent(ClientLevel world, PoseStack stack, Camera info, GameRenderer gameRenderer, LightTexture lightTexture, Frustum clippingHelper) {
		this.world = world;
		this.stack = stack;
		this.info = info;
		this.gameRenderer = gameRenderer;
		this.lightTexture = lightTexture;
		this.clippingHelper = clippingHelper;
	}

	public ClientLevel getWorld() {
		return world;
	}

	public PoseStack getStack() {
		return stack;
	}

	public Camera getInfo() {
		return info;
	}

	public GameRenderer getGameRenderer() {
		return gameRenderer;
	}

	public LightTexture getLightTexture() {
		return lightTexture;
	}

	public Frustum getClippingHelper() {
		return clippingHelper;
	}
}
