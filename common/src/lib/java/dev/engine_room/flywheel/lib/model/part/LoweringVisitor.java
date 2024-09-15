package dev.engine_room.flywheel.lib.model.part;

import java.util.ArrayList;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.model.Mesh;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.model.RetexturedMesh;
import dev.engine_room.flywheel.lib.model.SingleMeshModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * A tree walking visitor that lowers a MeshTree to a ModelTree.
 */
public interface LoweringVisitor {
	static ModelTree leaf(Model model) {
		return leaf(model, PartPose.ZERO);
	}

	static ModelTree leaf(Model model, PartPose initialPose) {
		return ModelTree.create(model, initialPose, new ModelTree[0], new String[0]);
	}

	static LoweringVisitor materialApplyingVisitor(Material material) {
		return (path, mesh) -> new SingleMeshModel(mesh, material);
	}

	static LoweringVisitor retexturingVisitor(Material material, TextureAtlasSprite sprite) {
		return (path, mesh) -> new SingleMeshModel(new RetexturedMesh(mesh, sprite), material);
	}

	static String append(String path, String child) {
		if (path.isEmpty()) {
			return child;
		}

		return path + "/" + child;
	}

	/**
	 * Walk the given MeshTree, lowering its Mesh to a Model, and lowering all children using the given visitor.
	 *
	 * @param path            The absolute path to the MeshTree node.
	 * @param meshTree        The MeshTree to walk.
	 * @param loweringVisitor The visitor to use to lower the Mesh and MeshTree nodes.
	 * @return The lowered ModelTree.
	 */
	static ModelTree walk(String path, MeshTree meshTree, LoweringVisitor loweringVisitor) {
		Model out = null;

		if (meshTree.mesh() != null) {
			out = loweringVisitor.visit(path, meshTree.mesh());
		}

		ArrayList<ModelTree> children = new ArrayList<>();
		ArrayList<String> childNames = new ArrayList<>();

		for (int i = 0; i < meshTree.childCount(); i++) {
			var child = loweringVisitor.visit(append(path, meshTree.childName(i)), meshTree.child(i));

			if (child != null) {
				children.add(child);
				childNames.add(meshTree.childName(i));
			}
		}

		return ModelTree.create(out, meshTree.initialPose(), children.toArray(new ModelTree[0]), childNames.toArray(new String[0]));
	}

	/**
	 * Visit the given Mesh, converting it to a Model.
	 *
	 * @param path The absolute path to the MeshTree node containing the Mesh.
	 * @param mesh The Mesh to lower.
	 * @return The lowered Model, or null if the Model should be omitted.
	 */
	@Nullable Model visit(String path, Mesh mesh);

	/**
	 * Visit the given MeshTree, converting it to a ModelTree.
	 *
	 * @param path     The absolute path to the MeshTree node.
	 * @param meshTree The MeshTree to lower.
	 * @return The lowered ModelTree, or null if the ModelTree should be omitted.
	 */
	@Nullable
	default ModelTree visit(String path, MeshTree meshTree) {
		return walk(path, meshTree, this);
	}
}
