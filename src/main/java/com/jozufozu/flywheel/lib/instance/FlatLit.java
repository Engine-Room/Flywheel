package com.jozufozu.flywheel.lib.instance;

import com.jozufozu.flywheel.api.instance.Instance;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;

/**
 * An interface that implementors of {@link Instance} should also implement
 * if they wish to make use of Flywheel's provided light update methods.
 * <p>
 * This only covers flat lighting, smooth lighting is still TODO.
 */
public interface FlatLit extends Instance {
	/**
	 * @param blockLight An integer in the range [0, 15] representing the
	 *                   amount of block light this instance should receive.
	 * @return {@code this}
	 */
	FlatLit setBlockLight(int blockLight);

	/**
	 * @param skyLight An integer in the range [0, 15] representing the
	 *                 amount of sky light this instance should receive.
	 * @return {@code this}
	 */
	FlatLit setSkyLight(int skyLight);

	default FlatLit setLight(int blockLight, int skyLight) {
		return setBlockLight(blockLight).setSkyLight(skyLight);
	}

	default FlatLit updateLight(BlockAndTintGetter level, BlockPos pos) {
		return setLight(level.getBrightness(LightLayer.BLOCK, pos), level.getBrightness(LightLayer.SKY, pos));
	}

	int getPackedLight();
}
