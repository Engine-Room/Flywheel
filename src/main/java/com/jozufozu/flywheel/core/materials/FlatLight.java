package com.jozufozu.flywheel.core.materials;

import com.jozufozu.flywheel.backend.instancing.InstanceData;

/**
 * An interface that implementors of {@link InstanceData} should also implement
 * if they wish to make use of Flywheel's provided light update methods.
 * <p>
 * This only covers flat lighting, smooth lighting is still TODO.
 */
public interface FlatLight {
	/**
	 * @param blockLight An integer in the range [0, 15] representing the
	 *                   amount of block light this instance should receive.
	 * @return <code>this</code>
	 */
	FlatLight setBlockLight(int blockLight);

	/**
	 * @param skyLight An integer in the range [0, 15] representing the
	 *                 amount of sky light this instance should receive.
	 * @return <code>this</code>
	 */
	FlatLight setSkyLight(int skyLight);
}
