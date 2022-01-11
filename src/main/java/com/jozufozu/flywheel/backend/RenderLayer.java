package com.jozufozu.flywheel.backend;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.RenderType;

/**
 * The 3 discrete stages the world is rendered in.
 */
public enum RenderLayer {
	/**
	 * Solid layer:<br>
	 *
	 * All polygons will entirely occlude everything behind them.
	 *
	 * <br><br>
	 * e.g. stone, dirt, solid blocks
	 */
	SOLID,
	/**
	 * Cutout layer:<br>
	 *
	 * <em>Fragments</em> will either occlude or not occlude depending on the texture/material.
	 *
	 * <br><br>
	 * e.g. leaves, cobwebs, tall grass, saplings, glass
	 */
	CUTOUT,
	/**
	 * Transparent layer:<br>
	 *
	 * Nothing is guaranteed to occlude and fragments blend their color with what's behind them.
	 *
	 * <br><br>
	 * e.g. stained glass, water
	 */
	TRANSPARENT,
	;

	@Nullable
	public static RenderLayer getPrimaryLayer(RenderType type) {
		if (type == RenderType.solid()) {
			return SOLID;
		} else if (type == RenderType.cutoutMipped()) {
			return CUTOUT;
		} else if (type == RenderType.translucent()) {
			return TRANSPARENT;
		}

		return null;
	}

	@Nullable
	public static RenderLayer getLayer(RenderType type) {
		if (type == RenderType.solid()) {
			return SOLID;
		} else if (type == RenderType.cutoutMipped() || type == RenderType.cutout()) {
			return CUTOUT;
		} else if (type == RenderType.translucent()) {
			return TRANSPARENT;
		}

		return null;
	}
}
