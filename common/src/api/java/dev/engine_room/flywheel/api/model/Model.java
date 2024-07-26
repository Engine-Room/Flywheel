package dev.engine_room.flywheel.api.model;

import java.util.List;

import dev.engine_room.flywheel.api.instance.InstanceType;

import dev.engine_room.flywheel.api.instance.InstancerProvider;

import org.joml.Vector4fc;

import dev.engine_room.flywheel.api.material.Material;

public interface Model {
	/**
	 * Get a list of all meshes in this model.
	 *
	 * <p>The contents of the returned list will be queried, but never modified.</p>
	 *
	 * <p>Meshes will be rendered in the order they appear in this list. See
	 * {@link InstancerProvider#instancer(InstanceType, Model, int)} for a complete explanation</p>
	 *
	 * @return A list of meshes.
	 */
	List<ConfiguredMesh> meshes();

	/**
	 * Get a vec4 representing this model's bounding sphere in the format (x, y, z, radius).
	 * It should encompass all meshes' bounding spheres.
	 *
	 * @return A vec4 view.
	 */
	Vector4fc boundingSphere();

	record ConfiguredMesh(Material material, Mesh mesh) {
	}
}
