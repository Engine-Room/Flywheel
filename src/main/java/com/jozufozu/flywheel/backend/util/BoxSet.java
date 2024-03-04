package com.jozufozu.flywheel.backend.util;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;

public class BoxSet {
	private final LongSet occupied = new LongOpenHashSet();

	private int minX;
	private int minY;
	private int minZ;
	private int maxX;
	private int maxY;
	private int maxZ;

	/**
	 * Add a box to the set.
	 *
	 * @return {@code true} if the position or size of the set changed.
	 */
	public boolean add(int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
		boolean wasEmpty = occupied.isEmpty();
		for (int i = x; i < x + sizeX; i++) {
			for (int j = y; j < y + sizeY; j++) {
				for (int k = z; k < z + sizeZ; k++) {
					occupied.add(BlockPos.asLong(i, j, k));
				}
			}
		}

		if (wasEmpty) {
			this.minX = x;
			this.minY = y;
			this.minZ = z;
			this.maxX = x + sizeX;
			this.maxY = y + sizeY;
			this.maxZ = z + sizeZ;
			return true;
		} else {
			boolean changed = false;
			if (x < minX) {
				minX = x;
				changed = true;
			}
			if (y < minY) {
				minY = y;
				changed = true;
			}
			if (z < minZ) {
				minZ = z;
				changed = true;
			}
			if (x + sizeX > maxX) {
				maxX = x + sizeX;
				changed = true;
			}
			if (y + sizeY > maxY) {
				maxY = y + sizeY;
				changed = true;
			}
			if (z + sizeZ > maxZ) {
				maxZ = z + sizeZ;
				changed = true;
			}
			return changed;
		}
	}

	/**
	 * Remove a box from the set.
	 *
	 * @return {@code true} if the position or size of the set changed.
	 */
	public boolean clear(int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
		for (int i = x; i < x + sizeX; i++) {
			for (int j = y; j < y + sizeY; j++) {
				for (int k = z; k < z + sizeZ; k++) {
					occupied.remove(BlockPos.asLong(i, j, k));
				}
			}
		}

		if (occupied.isEmpty()) {
			minX = 0;
			minY = 0;
			minZ = 0;
			maxX = 0;
			maxY = 0;
			maxZ = 0;
			return true;
		} else {
			ExtremaFinder finder = new ExtremaFinder();
			occupied.forEach(finder::accept);

			if (finder.volume() != volume()) {
				minX = finder.minX;
				minY = finder.minY;
				minZ = finder.minZ;
				maxX = finder.maxX;
				maxY = finder.maxY;
				maxZ = finder.maxZ;
				return true;
			} else {
				return false;
			}
		}
	}

	public int minX() {
		return minX;
	}

	public int minY() {
		return minY;
	}

	public int minZ() {
		return minZ;
	}

	public int sizeX() {
		return maxX - minX;
	}

	public int sizeY() {
		return maxY - minY;
	}

	public int sizeZ() {
		return maxZ - minZ;
	}

	public int volume() {
		return sizeX() * sizeY() * sizeZ();
	}

	private static class ExtremaFinder {
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;

		public void accept(long l) {
			int x = BlockPos.getX(l);
			int y = BlockPos.getY(l);
			int z = BlockPos.getZ(l);
			if (x < minX) {
				minX = x;
			}
			if (y < minY) {
				minY = y;
			}
			if (z < minZ) {
				minZ = z;
			}
			if (x > maxX) {
				maxX = x;
			}
			if (y > maxY) {
				maxY = y;
			}
			if (z > maxZ) {
				maxZ = z;
			}
		}

		public int volume() {
			return (maxX - minX) * (maxY - minY) * (maxZ - minZ);
		}
	}
}
