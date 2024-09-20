package dev.engine_room.flywheel.lib.model.part;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.model.Mesh;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.model.RetexturedMesh;
import dev.engine_room.flywheel.lib.model.SingleMeshModel;
import dev.engine_room.flywheel.lib.util.ResourceReloadCache;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public final class ModelTrees {
	private static final ResourceReloadCache<ModelTreeKey, ModelTree> CACHE = new ResourceReloadCache<>(k -> {
		ModelTree tree = convert("", MeshTree.of(k.layer), k.pathsToPrune, k.texture != null ? k.texture.sprite() : null, k.material);

		if (tree == null) {
			throw new IllegalArgumentException("Cannot prune root node!");
		}

		return tree;
	});

	private ModelTrees() {
	}

	public static ModelTree of(ModelLayerLocation layer, Material material) {
		return CACHE.get(new ModelTreeKey(layer, Collections.emptySet(), null, material));
	}

	public static ModelTree of(ModelLayerLocation layer, net.minecraft.client.resources.model.Material texture, Material material) {
		return CACHE.get(new ModelTreeKey(layer, Collections.emptySet(), texture, material));
	}

	public static ModelTree of(ModelLayerLocation layer, Set<String> pathsToPrune, Material material) {
		return CACHE.get(new ModelTreeKey(layer, Set.copyOf(pathsToPrune), null, material));
	}

	public static ModelTree of(ModelLayerLocation layer, Set<String> pathsToPrune, net.minecraft.client.resources.model.Material texture, Material material) {
		return CACHE.get(new ModelTreeKey(layer, Set.copyOf(pathsToPrune), texture, material));
	}

	@Nullable
	private static ModelTree convert(String path, MeshTree meshTree, Set<String> pathsToPrune, @Nullable TextureAtlasSprite sprite, Material material) {
		if (pathsToPrune.contains(path)) {
			return null;
		}

		Model model = null;
		Mesh mesh = meshTree.mesh();

		if (mesh != null) {
			if (sprite != null) {
				mesh = new RetexturedMesh(mesh, sprite);
			}

			model = new SingleMeshModel(mesh, material);
		}

		Map<String, ModelTree> children = new HashMap<>();
		String pathSlash = path + "/";

		for (int i = 0; i < meshTree.childCount(); i++) {
			String childName = meshTree.childName(i);
			var child = convert(pathSlash + childName, meshTree.child(i), pathsToPrune, sprite, material);

			if (child != null) {
				children.put(childName, child);
			}
		}

		return new ModelTree(model, meshTree.initialPose(), children);
	}

	private record ModelTreeKey(ModelLayerLocation layer, Set<String> pathsToPrune, @Nullable net.minecraft.client.resources.model.Material texture, Material material) {
	}
}
