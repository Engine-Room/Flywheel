package com.jozufozu.flywheel.lib.context;

import com.jozufozu.flywheel.api.context.Context;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.client.resources.model.ModelBakery;

public final class Contexts {
	public static final Context DEFAULT = SimpleContext.builder(ContextShaders.DEFAULT)
			.build();

	public static final Int2ObjectMap<Context> CRUMBLING;

	static {
		var map = new Int2ObjectArrayMap<Context>();

		for (int i = 0; i < ModelBakery.BREAKING_LOCATIONS.size(); i++) {
			var crumblingLocation = ModelBakery.BREAKING_LOCATIONS.get(i);
			map.put(i, SimpleContext.builder(ContextShaders.CRUMBLING)
					.preparation((material, shader, textureSource) -> {
						shader.setTexture("crumblingTex", textureSource.byName(crumblingLocation));
					})
					.build());
		}

		CRUMBLING = Int2ObjectMaps.unmodifiable(map);
	}

	private Contexts() {
	}
}
