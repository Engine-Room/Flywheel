package com.jozufozu.flywheel.light;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;

public class BasicProvider implements LightProvider {

	private static final Map<BlockAndTintGetter, BasicProvider> wrappers = new WeakHashMap<>();

	public static BasicProvider get(BlockAndTintGetter world) {
		return wrappers.computeIfAbsent(world, BasicProvider::new);
	}

	private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

	private final BlockAndTintGetter reader;

	public BasicProvider(BlockAndTintGetter reader) {
		this.reader = reader;
	}

	@Override
	public int getLight(LightLayer type, int x, int y, int z) {
		return reader.getBrightness(type, pos.set(x, y, z));
	}
}
