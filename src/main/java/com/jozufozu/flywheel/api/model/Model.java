package com.jozufozu.flywheel.api.model;

import java.util.List;

import org.joml.Vector4fc;

import com.jozufozu.flywheel.api.material.Material;

public interface Model {
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
