package com.jozufozu.flywheel.lib.struct;

import com.jozufozu.flywheel.api.struct.InstancePart;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;

/**
 * An interface that implementors of {@link InstancePart} should also implement
 * if they wish to make use of Flywheel's provided light update methods.
 * <p>
 * This only covers flat lighting, smooth lighting is still TODO.
 *
 * @param <P> The name of the class that implements this interface.
 */
public interface FlatLit<P extends InstancePart & FlatLit<P>> {
	/**
	 * @param blockLight An integer in the range [0, 15] representing the
	 *                   amount of block light this instance should receive.
	 * @return {@code this}
	 */
	P setBlockLight(int blockLight);

	/**
	 * @param skyLight An integer in the range [0, 15] representing the
	 *                 amount of sky light this instance should receive.
	 * @return {@code this}
	 */
	P setSkyLight(int skyLight);

	default P setLight(int blockLight, int skyLight) {
		return setBlockLight(blockLight).setSkyLight(skyLight);
	}

	default P updateLight(BlockAndTintGetter level, BlockPos pos) {
		return setLight(level.getBrightness(LightLayer.BLOCK, pos), level.getBrightness(LightLayer.SKY, pos));
	}

	int getPackedLight();
}
