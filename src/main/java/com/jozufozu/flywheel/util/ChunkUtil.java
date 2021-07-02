package com.jozufozu.flywheel.util;

import javax.annotation.Nullable;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;

public class ChunkUtil {

	public static boolean isValidSection(@Nullable Chunk chunk, int sectionY) {
		if (chunk == null) return false;

		// TODO: 1.17
		ChunkSection[] sections = chunk.getSections();

		return sectionY >= 0 && sectionY < sections.length;
	}
}
