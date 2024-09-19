package dev.engine_room.flywheel.lib.model.part;

import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.model.Model;
import net.minecraft.client.model.geom.PartPose;

public final class ModelTree {
	@Nullable
	private final Model model;
	private final PartPose initialPose;
	private final ModelTree[] children;
	private final String[] childNames;

	/**
	 * Create a new ModelTree node.
	 *
	 * @param model       The model to associate with this node, or null if this node does not render.
	 * @param initialPose The initial pose of this node.
	 * @param children    The children of this node.
	 */
	public ModelTree(@Nullable Model model, PartPose initialPose, Map<String, ModelTree> children) {
		this.model = model;
		this.initialPose = initialPose;

		String[] childNames = children.keySet().toArray(String[]::new);
		Arrays.sort(childNames);

		ModelTree[] childArray = new ModelTree[childNames.length];
		for (int i = 0; i < childNames.length; i++) {
			childArray[i] = children.get(childNames[i]);
		}

		this.children = childArray;
		this.childNames = childNames;
	}

	@Nullable
	public Model model() {
		return model;
	}

	public PartPose initialPose() {
		return initialPose;
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

	public int childIndex(String name) {
		return Arrays.binarySearch(childNames, name);
	}

	public boolean hasChild(String name) {
		return childIndex(name) >= 0;
	}

	@Nullable
	public ModelTree child(String name) {
		int index = childIndex(name);

		if (index < 0) {
			return null;
		}

		return child(index);
	}

	public ModelTree childOrThrow(String name) {
		ModelTree child = child(name);

		if (child == null) {
			throw new NoSuchElementException("Can't find part " + name);
		}

		return child;
	}
}
