package com.jozufozu.flywheel.api;

import com.jozufozu.flywheel.backend.RenderLayer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Vec3i;

public interface MaterialManager {

	/**
	 * Get a material group that will render in the given layer with the given state.
	 *
	 * @param layer The {@link RenderLayer} you want to draw in.
	 * @param state The {@link RenderType} you need to draw with.
	 * @return A material group whose children will
	 */
	MaterialGroup state(RenderLayer layer, RenderType state);

	Vec3i getOriginCoordinate();

	default MaterialGroup solid(RenderType state) {
		return state(RenderLayer.SOLID, state);
	}

	default MaterialGroup cutout(RenderType state) {
		return state(RenderLayer.CUTOUT, state);
	}

	default MaterialGroup transparent(RenderType state) {
		return state(RenderLayer.TRANSPARENT, state);
	}

	default MaterialGroup defaultSolid() {
		return solid(RenderType.solid());
	}

	default MaterialGroup defaultCutout() {
		return cutout(RenderType.cutout());
	}

	default MaterialGroup defaultTransparent() {
		return transparent(RenderType.translucent());
	}
}
