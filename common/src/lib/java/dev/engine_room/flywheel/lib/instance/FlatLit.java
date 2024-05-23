package dev.engine_room.flywheel.lib.instance;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.lib.visual.AbstractVisual;
import net.minecraft.client.renderer.LightTexture;

/**
 * An interface that implementors of {@link Instance} should also implement
 * if they wish to make use of the relighting utilities in {@link AbstractVisual}.
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
