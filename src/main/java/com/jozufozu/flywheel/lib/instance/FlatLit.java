package com.jozufozu.flywheel.lib.instance;

import com.jozufozu.flywheel.api.instance.Instance;

import net.minecraft.client.renderer.LightTexture;

/**
 * An interface that implementors of {@link Instance} should also implement
 * if they wish to make use of Flywheel's provided light update methods.
 */
public interface FlatLit extends Instance {
	/**
	 * Set the block and sky light values for this instance.
	 * @param blockLight Block light value
	 * @param skyLight Sky light value
	 * @return {@code this} for chaining
	 */
	FlatLit light(int blockLight, int skyLight);

	/**
	 * Set the packed light value for this instance.
	 * @param packedLight Packed block and sky light per {@link LightTexture#pack(int, int)}
	 * @return {@code this} for chaining
	 */
	FlatLit light(int packedLight);
}
