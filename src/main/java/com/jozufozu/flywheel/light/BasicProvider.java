package com.jozufozu.flywheel.light;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;

/**
 * Wraps a world and minimally lowers the interface.
 */
public class BasicProvider implements LightProvider {

	private final BlockAndTintGetter reader;
	private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

	public BasicProvider(BlockAndTintGetter reader) {
		this.reader = reader;
	}

	@Override
	public int getLight(LightLayer type, int x, int y, int z) {
		return reader.getBrightness(type, pos.set(x, y, z));
	}
}
