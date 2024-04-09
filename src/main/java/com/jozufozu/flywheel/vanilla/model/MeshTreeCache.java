package com.jozufozu.flywheel.vanilla.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.ApiStatus;

import com.jozufozu.flywheel.api.event.EndClientResourceReloadEvent;

import net.minecraft.client.model.geom.ModelLayerLocation;

public class MeshTreeCache {
	private static final Map<ModelLayerLocation, MeshTree> MESH_TREES = new ConcurrentHashMap<>();

	public static MeshTree get(ModelLayerLocation key) {
		return MESH_TREES.computeIfAbsent(key, MeshTree::convert);
	}

	@ApiStatus.Internal
	public static void onEndClientResourceReload(EndClientResourceReloadEvent event) {
		MESH_TREES.values()
				.forEach(MeshTree::delete);
		MESH_TREES.clear();
	}

	private MeshTreeCache() {
	}
}
