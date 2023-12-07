package com.jozufozu.flywheel.api.model;

import java.util.Map;

import org.joml.Vector4fc;

import com.jozufozu.flywheel.api.material.Material;

public interface Model {
	Map<Material, Mesh> meshes();

	/**
	 * Get a vec4 representing this model's bounding sphere in the format (x, y, z, radius).
	 * It should encompass all meshes' bounding spheres.
	 *
	 * @return A vec4 view.
	 */
	Vector4fc boundingSphere();

	// TODO: unused. remove?
	@Deprecated
	int vertexCount();

	void delete();
}
