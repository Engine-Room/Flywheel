package com.jozufozu.flywheel.api;

import com.jozufozu.flywheel.api.struct.StructType;

public interface MaterialGroup {
	/**
	 * Get the material as defined by the given {@link StructType type}.
	 *
	 * @param spec The material you want to create instances with.
	 * @param <D>  The type representing the per instance data.
	 * @return A material you can use to render models.
	 */
	<D extends InstanceData> Material<D> material(StructType<D> spec);
}
