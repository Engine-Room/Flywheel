package com.jozufozu.flywheel.light;

import com.jozufozu.flywheel.util.RenderUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;

public class GridAlignedBB implements ImmutableBox {
	private int minX;
	private int minY;
	private int minZ;
	private int maxX;
	private int maxY;
	private int maxZ;

	public GridAlignedBB() {

	}

	public GridAlignedBB(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}

	public static GridAlignedBB ofRadius(int radius) {
		return new GridAlignedBB(-radius, -radius, -radius, radius + 1, radius + 1, radius + 1);
	}

	public static GridAlignedBB from(AABB aabb) {
		int minX = (int) Math.floor(aabb.minX);
		int minY = (int) Math.floor(aabb.minY);
		int minZ = (int) Math.floor(aabb.minZ);
		int maxX = (int) Math.ceil(aabb.maxX);
		int maxY = (int) Math.ceil(aabb.maxY);
		int maxZ = (int) Math.ceil(aabb.maxZ);
		return new GridAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public static GridAlignedBB from(SectionPos pos) {
		return new GridAlignedBB(pos.minBlockX(), pos.minBlockY(), pos.minBlockZ(), pos.maxBlockX() + 1, pos.maxBlockY() + 1, pos.maxBlockZ() + 1);
	}

	public static GridAlignedBB from(BlockPos start, BlockPos end) {
		return new GridAlignedBB(start.getX(), start.getY(), start.getZ(), end.getX() + 1, end.getY() + 1, end.getZ() + 1);
	}

	public static GridAlignedBB from(BlockPos pos) {
		return new GridAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
	}

	public static GridAlignedBB from(int sectionX, int sectionZ) {
		int startX = sectionX << 4;
		int startZ = sectionZ << 4;
		return new GridAlignedBB(startX, 0, startZ, startX + 16, 256, startZ + 16);
	}

	public void fixMinMax() {
		int minX = Math.min(this.minX, this.maxX);
		int minY = Math.min(this.minY, this.maxY);
		int minZ = Math.min(this.minZ, this.maxZ);
		int maxX = Math.max(this.minX, this.maxX);
		int maxY = Math.max(this.minY, this.maxY);
		int maxZ = Math.max(this.minZ, this.maxZ);

		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}

	public void translate(Vec3i by) {
		translate(by.getX(), by.getY(), by.getZ());
	}

	public void translate(int x, int y, int z) {
		minX = minX + x;
		maxX = maxX + x;
		minY = minY + y;
		maxY = maxY + y;
		minZ = minZ + z;
		maxZ = maxZ + z;
	}

	public void mirrorAbout(Direction.Axis axis) {
		Vec3i axisVec = Direction.get(Direction.AxisDirection.POSITIVE, axis)
				.getNormal();
		int flipX = axisVec.getX() - 1;
		int flipY = axisVec.getY() - 1;
		int flipZ = axisVec.getZ() - 1;

		int maxX = this.maxX * flipX;
		int maxY = this.maxY * flipY;
		int maxZ = this.maxZ * flipZ;
		this.maxX = this.minX * flipX;
		this.maxY = this.minY * flipY;
		this.maxZ = this.minZ * flipZ;
		this.minX = maxX;
		this.minY = maxY;
		this.minZ = maxZ;
	}

	/**
	 * Grow this bounding box to have power of 2 side length, scaling from the center.
	 */
	public void nextPowerOf2Centered() {
		int sizeX = sizeX();
		int sizeY = sizeY();
		int sizeZ = sizeZ();

		int newSizeX = RenderUtil.nextPowerOf2(sizeX);
		int newSizeY = RenderUtil.nextPowerOf2(sizeY);
		int newSizeZ = RenderUtil.nextPowerOf2(sizeZ);

		int diffX = newSizeX - sizeX;
		int diffY = newSizeY - sizeY;
		int diffZ = newSizeZ - sizeZ;

		minX = minX - diffX / 2; // floor division for the minimums
		minY = minY - diffY / 2;
		minZ = minZ - diffZ / 2;
		maxX = maxX + (diffX + 1) / 2; // ceiling divison for the maximums
		maxY = maxY + (diffY + 1) / 2;
		maxZ = maxZ + (diffZ + 1) / 2;
	}

	/**
	 * Grow this bounding box to have power of 2 side lengths, scaling from the minimum coords.
	 */
	public void nextPowerOf2() {
		int sizeX = RenderUtil.nextPowerOf2(sizeX());
		int sizeY = RenderUtil.nextPowerOf2(sizeY());
		int sizeZ = RenderUtil.nextPowerOf2(sizeZ());

		maxX = minX + sizeX;
		maxY = minY + sizeY;
		maxZ = minZ + sizeZ;
	}

	public void grow(int s) {
		this.grow(s, s, s);
	}

	public void grow(int x, int y, int z) {
		minX = minX - x;
		minY = minY - y;
		minZ = minZ - z;
		maxX = maxX + x;
		maxY = maxY + y;
		maxZ = maxZ + z;
	}

	public void intersectAssign(ImmutableBox other) {
		minX = Math.max(this.minX, other.getMinX());
		minY = Math.max(this.minY, other.getMinY());
		minZ = Math.max(this.minZ, other.getMinZ());
		maxX = Math.min(this.maxX, other.getMaxX());
		maxY = Math.min(this.maxY, other.getMaxY());
		maxZ = Math.min(this.maxZ, other.getMaxZ());
	}

	public void unionAssign(ImmutableBox other) {
		minX = Math.min(this.minX, other.getMinX());
		minY = Math.min(this.minY, other.getMinY());
		minZ = Math.min(this.minZ, other.getMinZ());
		maxX = Math.max(this.maxX, other.getMaxX());
		maxY = Math.max(this.maxY, other.getMaxY());
		maxZ = Math.max(this.maxZ, other.getMaxZ());
	}

	public void unionAssign(AABB other) {
		minX = Math.min(this.minX, (int) Math.floor(other.minX));
		minY = Math.min(this.minY, (int) Math.floor(other.minY));
		minZ = Math.min(this.minZ, (int) Math.floor(other.minZ));
		maxX = Math.max(this.maxX, (int) Math.ceil(other.maxX));
		maxY = Math.max(this.maxY, (int) Math.ceil(other.maxY));
		maxZ = Math.max(this.maxZ, (int) Math.ceil(other.maxZ));
	}

	public void assign(AABB other) {
		minX = (int) Math.floor(other.minX);
		minY = (int) Math.floor(other.minY);
		minZ = (int) Math.floor(other.minZ);
		maxX = (int) Math.ceil(other.maxX);
		maxY = (int) Math.ceil(other.maxY);
		maxZ = (int) Math.ceil(other.maxZ);
	}

	public void assign(ImmutableBox other) {
		minX = other.getMinX();
		minY = other.getMinY();
		minZ = other.getMinZ();
		maxX = other.getMaxX();
		maxY = other.getMaxY();
		maxZ = other.getMaxZ();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ImmutableBox that = (ImmutableBox) o;

		return this.sameAs(that);
	}

	@Override
	public int hashCode() {
		int result = getMinX();
		result = 31 * result + getMinY();
		result = 31 * result + getMinZ();
		result = 31 * result + getMaxX();
		result = 31 * result + getMaxY();
		result = 31 * result + getMaxZ();
		return result;
	}

	@Override
	public int getMinX() {
		return minX;
	}

	@Override
	public int getMinY() {
		return minY;
	}

	@Override
	public int getMinZ() {
		return minZ;
	}

	@Override
	public int getMaxX() {
		return maxX;
	}

	@Override
	public int getMaxY() {
		return maxY;
	}

	@Override
	public int getMaxZ() {
		return maxZ;
	}

	public GridAlignedBB setMinX(int minX) {
		this.minX = minX;
		return this;
	}

	public GridAlignedBB setMinY(int minY) {
		this.minY = minY;
		return this;
	}

	public GridAlignedBB setMinZ(int minZ) {
		this.minZ = minZ;
		return this;
	}

	public GridAlignedBB setMaxX(int maxX) {
		this.maxX = maxX;
		return this;
	}

	public GridAlignedBB setMaxY(int maxY) {
		this.maxY = maxY;
		return this;
	}

	public GridAlignedBB setMaxZ(int maxZ) {
		this.maxZ = maxZ;
		return this;
	}

	public GridAlignedBB assign(BlockPos start, BlockPos end) {
		minX = start.getX();
		minY = start.getY();
		minZ = start.getZ();
		maxX = end.getX() + 1;
		maxY = end.getY() + 1;
		maxZ = end.getZ() + 1;
		return this;
	}

	public GridAlignedBB setMax(Vec3i v) {
		return setMax(v.getX(), v.getY(), v.getZ());
	}

	public GridAlignedBB setMin(Vec3i v) {
		return setMin(v.getX(), v.getY(), v.getZ());
	}

	public GridAlignedBB setMax(int x, int y, int z) {
		maxX = x;
		maxY = y;
		maxZ = z;
		return this;
	}

	public GridAlignedBB setMin(int x, int y, int z) {
		minX = x;
		minY = y;
		minZ = z;
		return this;
	}

	@Override
	public int sizeX() {
		return maxX - minX;
	}

	@Override
	public int sizeY() {
		return maxY - minY;
	}

	@Override
	public int sizeZ() {
		return maxZ - minZ;
	}

	@Override
	public boolean empty() {
		// if any dimension has side length 0 this box contains no volume
		return minX == maxX || minY == maxY || minZ == maxZ;
	}

	@Override
	public boolean sameAs(ImmutableBox other) {
		return minX == other.getMinX() && minY == other.getMinY() && minZ == other.getMinZ() && maxX == other.getMaxX() && maxY == other.getMaxY() && maxZ == other.getMaxZ();
	}

	@Override
	public boolean sameAs(AABB other) {
		return minX == Math.floor(other.minX)
				&& minY == Math.floor(other.minY)
				&& minZ == Math.floor(other.minZ)
				&& maxX == Math.ceil(other.maxX)
				&& maxY == Math.ceil(other.maxY)
				&& maxZ == Math.ceil(other.maxZ);
	}

	@Override
	public GridAlignedBB intersect(ImmutableBox other) {
		int minX = Math.max(this.minX, other.getMinX());
		int minY = Math.max(this.minY, other.getMinY());
		int minZ = Math.max(this.minZ, other.getMinZ());
		int maxX = Math.min(this.maxX, other.getMaxX());
		int maxY = Math.min(this.maxY, other.getMaxY());
		int maxZ = Math.min(this.maxZ, other.getMaxZ());
		return new GridAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public ImmutableBox union(ImmutableBox other) {
		int minX = Math.min(this.minX, other.getMinX());
		int minY = Math.min(this.minY, other.getMinY());
		int minZ = Math.min(this.minZ, other.getMinZ());
		int maxX = Math.max(this.maxX, other.getMaxX());
		int maxY = Math.max(this.maxY, other.getMaxY());
		int maxZ = Math.max(this.maxZ, other.getMaxZ());
		return new GridAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public boolean contains(ImmutableBox other) {
		return other.getMinX() >= this.minX && other.getMaxX() <= this.maxX && other.getMinY() >= this.minY && other.getMaxY() <= this.maxY && other.getMinZ() >= this.minZ && other.getMaxZ() <= this.maxZ;
	}

	@Override
	public boolean intersects(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		return this.minX < maxX && this.maxX > minX && this.minY < maxY && this.maxY > minY && this.minZ < maxZ && this.maxZ > minZ;
	}

	@Override
	public void forEachContained(ICoordinateConsumer func) {
		if (empty()) return;

		for (int x = minX; x < maxX; x++) {
			for (int y = Math.max(minY, 0); y < Math.min(maxY, 255); y++) { // clamp to world height limits
				for (int z = minZ; z < maxZ; z++) {
					func.consume(x, y, z);
				}
			}
		}
	}

	@Override
	public AABB toAABB() {
		return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public GridAlignedBB copy() {
		return new GridAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public String toString() {
		return "(" + minX + ", " + minY + ", " + minZ + ")->(" + maxX + ", " + maxY + ", " + maxZ + ')';
	}
}
