package com.jozufozu.flywheel.backend.state;

import com.mojang.blaze3d.platform.GlStateManager;

public class NoCullRenderState implements IRenderState {

	public static final NoCullRenderState INSTANCE = new NoCullRenderState();

	protected NoCullRenderState() { }

	@Override
	public void bind() {
		GlStateManager._disableCull();
	}

	@Override
	public void unbind() {
		GlStateManager._enableCull();
	}
}
