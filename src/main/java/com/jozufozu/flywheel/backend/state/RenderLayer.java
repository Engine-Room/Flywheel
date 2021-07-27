package com.jozufozu.flywheel.backend.state;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.RenderType;

public enum RenderLayer {
	SOLID,
	CUTOUT,
	TRANSPARENT,
	;

	@Nullable
	public static RenderLayer fromRenderType(RenderType type) {
		if (type == RenderType.solid()) {
			return SOLID;
		} else if (type == RenderType.cutoutMipped()) {
			return CUTOUT;
		} else if (type == RenderType.translucent()) {
			return TRANSPARENT;
		}

		return null;
	}
}
