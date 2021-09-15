package com.jozufozu.flywheel.event;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;

public class BeginFrameEvent extends Event {
	private final ClientLevel world;
	private final Camera info;
	private final Frustum clippingHelper;

	public BeginFrameEvent(ClientLevel world, Camera info, Frustum clippingHelper) {
		this.world = world;
		this.info = info;
		this.clippingHelper = clippingHelper;
	}

	public ClientLevel getWorld() {
		return world;
	}

	public Camera getInfo() {
		return info;
	}

	public Frustum getClippingHelper() {
		return clippingHelper;
	}

	public Vec3 getCameraPos() {
		return info.getPosition();
	}
}
