package dev.engine_room.flywheel.lib.model.part;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;

public class ModelTree {
	private static final Map<ModelTreeKey, ModelTree> CACHE = new ConcurrentHashMap<>();

	@Nullable
	private final Model model;
	private final PartPose initialPose;
	private final ModelTree[] children;
	private final String[] childNames;

	private ModelTree(@Nullable Model model, PartPose initialPose, ModelTree[] children, String[] childNames) {
		this.model = model;
		this.initialPose = initialPose;
		this.children = children;
		this.childNames = childNames;
	}

	/**
	 * Create and memoize a ModelTree root.
	 *
	 * <p>This method is intended for use by Visuals.
	 *
	 * @param modelLayerLocation The model location to lower.
	 * @param loweringVisitor    The visitor to use to lower the model.
	 * @return The cached ModelTree root.
	 */
	public static ModelTree of(ModelLayerLocation modelLayerLocation, LoweringVisitor loweringVisitor) {
		return CACHE.computeIfAbsent(new ModelTreeKey(modelLayerLocation, loweringVisitor), k -> {
			var meshTree = MeshTree.of(k.modelLayerLocation());

			var out = k.loweringVisitor()
					.visit("", meshTree);

			if (out == null) {
				// Should this be an error, or a missing model?
				return ModelTree.create(null, PartPose.ZERO, new ModelTree[0], new String[0]);
			}

			return out;
		});
	}

	/**
	 * Create a new ModelTree node.
	 *
	 * <p>This method is intended for use by {@link LoweringVisitor} implementations.
	 *
	 * @param model       The model to associate with this node, or null if this node does not render.
	 * @param initialPose The initial pose of this node.
	 * @param children    The children of this node.
	 * @param childNames  The names of the children of this node.
	 * @return A new ModelTree node.
	 * @throws IllegalArgumentException if children and childNames have different lengths.
	 */
	public static ModelTree create(@Nullable Model model, PartPose initialPose, ModelTree[] children, String[] childNames) {
		if (children.length != childNames.length) {
			throw new IllegalArgumentException("children and childNames must have the same length (%s != %s)".formatted(children.length, childNames.length));
		}

		return new ModelTree(model, initialPose, children, childNames);
	}

	public int childCount() {
		return children.length;
	}

	public ModelTree child(int index) {
		return children[index];
	}

	public String childName(int index) {
		return childNames[index];
	}

	public PartPose initialPose() {
		return initialPose;
	}

	@Nullable
	public Model model() {
		return model;
	}

	public int childIndex(String name) {
		return Arrays.binarySearch(childNames, name);
	}

	@ApiStatus.Internal
	public static void onEndClientResourceReload() {
		CACHE.clear();
	}

	private record ModelTreeKey(ModelLayerLocation modelLayerLocation, LoweringVisitor loweringVisitor) {
	}
}
