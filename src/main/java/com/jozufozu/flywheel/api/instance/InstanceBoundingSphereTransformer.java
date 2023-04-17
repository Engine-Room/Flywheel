package com.jozufozu.flywheel.api.instance;

import org.joml.Vector4f;

public interface InstanceBoundingSphereTransformer<I extends Instance> {
	/**
	 * Transform the bounding sphere of a mesh to match the location of the instance.
	 *
	 * @param boundingSphere The bounding sphere of the mesh formatted as < x, y, z, radius >
	 * @param instance       The instance to transform the bounding sphere for.
	 */
	void transform(Vector4f boundingSphere, I instance);
}
