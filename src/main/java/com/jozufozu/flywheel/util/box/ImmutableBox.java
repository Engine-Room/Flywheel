package com.jozufozu.flywheel.util.box;

import static com.jozufozu.flywheel.util.RenderMath.isPowerOf2;

import net.minecraft.world.phys.AABB;

public interface ImmutableBox {
	int getMinX();

	int getMinY();

	int getMinZ();

	int getMaxX();

	int getMaxY();

	int getMaxZ();

	default int sizeX() {
		return getMaxX() - getMinX();
	}

	default int sizeY() {
		return getMaxY() - getMinY();
	}

	default int sizeZ() {
		return getMaxZ() - getMinZ();
	}

	default int volume() {
		return sizeX() * sizeY() * sizeZ();
	}

	default boolean empty() {
		// if any dimension has side length 0 this box contains no volume
		return getMinX() == getMaxX() || getMinY() == getMaxY() || getMinZ() == getMaxZ();
	}

	default boolean sameAs(ImmutableBox other) {
		return getMinX() == other.getMinX() && getMinY() == other.getMinY() && getMinZ() == other.getMinZ() && getMaxX() == other.getMaxX() && getMaxY() == other.getMaxY() && getMaxZ() == other.getMaxZ();
	}

	default boolean sameAs(ImmutableBox other, int margin) {
		return getMinX() == other.getMinX() - margin &&
				getMinY() == other.getMinY() - margin &&
				getMinZ() == other.getMinZ() - margin &&
				getMaxX() == other.getMaxX() + margin &&
				getMaxY() == other.getMaxY() + margin &&
				getMaxZ() == other.getMaxZ() + margin;
	}

	default boolean sameAs(AABB other) {
		return getMinX() == Math.floor(other.minX)
				&& getMinY() == Math.floor(other.minY)
				&& getMinZ() == Math.floor(other.minZ)
				&& getMaxX() == Math.ceil(other.maxX)
				&& getMaxY() == Math.ceil(other.maxY)
				&& getMaxZ() == Math.ceil(other.maxZ);
	}

	default boolean hasPowerOf2Sides() {
		// this is only true if all individual side lengths are powers of 2
		return isPowerOf2(volume());
	}

	default GridAlignedBB intersect(ImmutableBox other) {
		int minX = Math.max(this.getMinX(), other.getMinX());
		int minY = Math.max(this.getMinY(), other.getMinY());
		int minZ = Math.max(this.getMinZ(), other.getMinZ());
		int maxX = Math.min(this.getMaxX(), other.getMaxX());
		int maxY = Math.min(this.getMaxY(), other.getMaxY());
		int maxZ = Math.min(this.getMaxZ(), other.getMaxZ());
		return new GridAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	default ImmutableBox union(ImmutableBox other) {
		int minX = Math.min(this.getMinX(), other.getMinX());
		int minY = Math.min(this.getMinY(), other.getMinY());
		int minZ = Math.min(this.getMinZ(), other.getMinZ());
		int maxX = Math.max(this.getMaxX(), other.getMaxX());
		int maxY = Math.max(this.getMaxY(), other.getMaxY());
		int maxZ = Math.max(this.getMaxZ(), other.getMaxZ());
		return new GridAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
	}


	default boolean intersects(ImmutableBox other) {
		return this.intersects(other.getMinX(), other.getMinY(), other.getMinZ(), other.getMaxX(), other.getMaxY(), other.getMaxZ());
	}

	default boolean contains(int x, int y, int z) {
		return x >= getMinX()
				&& x <= getMaxX()
				&& y >= getMinY()
				&& y <= getMaxY()
				&& z >= getMinZ()
				&& z <= getMaxZ();
	}

	default boolean contains(ImmutableBox other) {
		return other.getMinX() >= this.getMinX()
				&& other.getMaxX() <= this.getMaxX()
				&& other.getMinY() >= this.getMinY()
				&& other.getMaxY() <= this.getMaxY()
				&& other.getMinZ() >= this.getMinZ()
				&& other.getMaxZ() <= this.getMaxZ();
	}

	default boolean isContainedBy(GridAlignedBB other) {
		return other.contains(this);
	}

	default boolean intersects(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		return this.getMinX() < maxX && this.getMaxX() > minX && this.getMinY() < maxY && this.getMaxY() > minY && this.getMinZ() < maxZ && this.getMaxZ() > minZ;
	}

	default void forEachContained(CoordinateConsumer func) {
		if (empty()) return;

		for (int x = getMinX(); x < getMaxX(); x++) {
			for (int y = getMinY(); y < getMaxY(); y++) {
				for (int z = getMinZ(); z < getMaxZ(); z++) {
					func.consume(x, y, z);
				}
			}
		}
	}

	default AABB toAABB() {
		return new AABB(getMinX(), getMinY(), getMinZ(), getMaxX(), getMaxY(), getMaxZ());
	}

	default GridAlignedBB copy() {
		return new GridAlignedBB(getMinX(), getMinY(), getMinZ(), getMaxX(), getMaxY(), getMaxZ());
	}
}
