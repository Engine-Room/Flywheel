package com.jozufozu.flywheel.util;

import net.minecraft.world.level.chunk.LevelChunk;

import net.minecraft.world.level.chunk.LevelChunkSection;

import org.jetbrains.annotations.Nullable;

public class ChunkUtil {

	public static boolean isValidSection(@Nullable LevelChunk chunk, int sectionY) {
		if (chunk == null) return false;

		// TODO: 1.17
		LevelChunkSection[] sections = chunk.getSections();

		return sectionY >= 0 && sectionY < sections.length;
	}
}
