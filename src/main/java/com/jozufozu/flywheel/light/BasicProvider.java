package com.jozufozu.flywheel.light;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.LightType;

public class BasicProvider implements LightProvider {

	private static final Map<IBlockDisplayReader, BasicProvider> wrappers = new WeakHashMap<>();

	public static BasicProvider get(IBlockDisplayReader world) {
		return wrappers.computeIfAbsent(world, BasicProvider::new);
	}

	private final BlockPos.Mutable pos = new BlockPos.Mutable();

	private final IBlockDisplayReader reader;

	public BasicProvider(IBlockDisplayReader reader) {
		this.reader = reader;
	}

	@Override
	public int getLight(LightType type, int x, int y, int z) {
		return reader.getBrightness(type, pos.set(x, y, z));
	}
}
