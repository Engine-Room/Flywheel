package dev.engine_room.flywheel.api.model;

import java.util.List;

import org.joml.Vector4fc;

import dev.engine_room.flywheel.api.material.Material;

public interface Model {
	/**
	 * Get a list of all meshes in this model.
	 *
	 * <p>The contents of the returned list will be queried, but never modified.</p>
	 *
	 * <p>Meshes will be rendered in the order they appear in this list, though
	 * no render order guarantees are made for meshes between different models.</p>
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

	void delete();

	record ConfiguredMesh(Material material, Mesh mesh) {
	}
}
