package com.jozufozu.flywheel.lib.util;

import java.util.Collection;
import java.util.function.LongConsumer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

public class SectionUtil {
	public static void containingAll(Collection<BlockPos> blocks, LongConsumer consumer) {
		if (blocks.isEmpty()) {
			return;
		}

		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		for (BlockPos pos : blocks) {
			minX = Math.min(minX, pos.getX());
			minY = Math.min(minY, pos.getY());
			minZ = Math.min(minZ, pos.getZ());
			maxX = Math.max(maxX, pos.getX());
			maxY = Math.max(maxY, pos.getY());
			maxZ = Math.max(maxZ, pos.getZ());
		}

		betweenClosedBlocks(minX, minY, minZ, maxX, maxY, maxZ, consumer);
	}

	public static void betweenClosedBox(int x, int y, int z, int sizeX, int sizeY, int sizeZ, LongConsumer consumer) {
        betweenClosedBlocks(x, y, z, x + sizeX, y + sizeY, z + sizeZ, consumer);
	}

	public static void betweenClosedBlocks(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, LongConsumer consumer) {
		int minSectionX = SectionPos.blockToSectionCoord(minX);
		int minSectionY = SectionPos.blockToSectionCoord(minY);
		int minSectionZ = SectionPos.blockToSectionCoord(minZ);
		int maxSectionX = SectionPos.blockToSectionCoord(maxX);
		int maxSectionY = SectionPos.blockToSectionCoord(maxY);
		int maxSectionZ = SectionPos.blockToSectionCoord(maxZ);

		for (int sectionX = minSectionX; sectionX <= maxSectionX; sectionX++) {
			for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
				for (int sectionZ = minSectionZ; sectionZ <= maxSectionZ; sectionZ++) {
					consumer.accept(SectionPos.asLong(sectionX, sectionY, sectionZ));
				}
			}
		}
	}
}
