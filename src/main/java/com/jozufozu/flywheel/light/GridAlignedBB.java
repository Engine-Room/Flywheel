package com.jozufozu.flywheel.light;

import com.jozufozu.flywheel.util.RenderUtil;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.math.vector.Vector3i;

public class GridAlignedBB implements ReadOnlyBox {
	private int minX;
	private int minY;
	private int minZ;
	private int maxX;
	private int maxY;
	private int maxZ;

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

	public static GridAlignedBB from(AxisAlignedBB aabb) {
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
		int minX = Math.min(this.getMinX(), this.getMaxX());
		int minY = Math.min(this.getMinY(), this.getMaxY());
		int minZ = Math.min(this.getMinZ(), this.getMaxZ());
		int maxX = Math.max(this.getMinX(), this.getMaxX());
		int maxY = Math.max(this.getMinY(), this.getMaxY());
		int maxZ = Math.max(this.getMinZ(), this.getMaxZ());

		this.setMinX(minX);
		this.setMinY(minY);
		this.setMinZ(minZ);
		this.setMaxX(maxX);
		this.setMaxY(maxY);
		this.setMaxZ(maxZ);
	}

	public void translate(Vector3i by) {
		translate(by.getX(), by.getY(), by.getZ());
	}

	public void translate(int x, int y, int z) {
		setMinX(getMinX() + x);
		setMaxX(getMaxX() + x);
		setMinY(getMinY() + y);
		setMaxY(getMaxY() + y);
		setMinZ(getMinZ() + z);
		setMaxZ(getMaxZ() + z);
	}

	public void mirrorAbout(Direction.Axis axis) {
		Vector3i axisVec = Direction.get(Direction.AxisDirection.POSITIVE, axis)
				.getNormal();
		int flipX = axisVec.getX() - 1;
		int flipY = axisVec.getY() - 1;
		int flipZ = axisVec.getZ() - 1;

		int maxX = this.getMaxX() * flipX;
		int maxY = this.getMaxY() * flipY;
		int maxZ = this.getMaxZ() * flipZ;
		this.setMaxX(this.getMinX() * flipX);
		this.setMaxY(this.getMinY() * flipY);
		this.setMaxZ(this.getMinZ() * flipZ);
		this.setMinX(maxX);
		this.setMinY(maxY);
		this.setMinZ(maxZ);
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

		setMinX(getMinX() - diffX / 2); // floor division for the minimums
		setMinY(getMinY() - diffY / 2);
		setMinZ(getMinZ() - diffZ / 2);
		setMaxX(getMaxX() + (diffX + 1) / 2); // ceiling divison for the maximums
		setMaxY(getMaxY() + (diffY + 1) / 2);
		setMaxZ(getMaxZ() + (diffZ + 1) / 2);
	}

	/**
	 * Grow this bounding box to have power of 2 side lengths, scaling from the minimum coords.
	 */
	public void nextPowerOf2() {
		int sizeX = RenderUtil.nextPowerOf2(sizeX());
		int sizeY = RenderUtil.nextPowerOf2(sizeY());
		int sizeZ = RenderUtil.nextPowerOf2(sizeZ());

		this.setMaxX(this.getMinX() + sizeX);
		this.setMaxY(this.getMinY() + sizeY);
		this.setMaxZ(this.getMinZ() + sizeZ);
	}

	public void grow(int s) {
		this.grow(s, s, s);
	}

	public void grow(int x, int y, int z) {
		setMinX(getMinX() - x);
		setMinY(getMinY() - y);
		setMinZ(getMinZ() - z);
		setMaxX(getMaxX() + x);
		setMaxY(getMaxY() + y);
		setMaxZ(getMaxZ() + z);
	}

	public void intersectAssign(ReadOnlyBox other) {
		this.setMinX(Math.max(this.getMinX(), other.getMinX()));
		this.setMinY(Math.max(this.getMinY(), other.getMinY()));
		this.setMinZ(Math.max(this.getMinZ(), other.getMinZ()));
		this.setMaxX(Math.min(this.getMaxX(), other.getMaxX()));
		this.setMaxY(Math.min(this.getMaxY(), other.getMaxY()));
		this.setMaxZ(Math.min(this.getMaxZ(), other.getMaxZ()));
	}

	public void unionAssign(ReadOnlyBox other) {
		this.setMinX(Math.min(this.getMinX(), other.getMinX()));
		this.setMinY(Math.min(this.getMinY(), other.getMinY()));
		this.setMinZ(Math.min(this.getMinZ(), other.getMinZ()));
		this.setMaxX(Math.max(this.getMaxX(), other.getMaxX()));
		this.setMaxY(Math.max(this.getMaxY(), other.getMaxY()));
		this.setMaxZ(Math.max(this.getMaxZ(), other.getMaxZ()));
	}

	public void unionAssign(AxisAlignedBB other) {
		this.setMinX(Math.min(this.getMinX(), (int) Math.floor(other.minX)));
		this.setMinY(Math.min(this.getMinY(), (int) Math.floor(other.minY)));
		this.setMinZ(Math.min(this.getMinZ(), (int) Math.floor(other.minZ)));
		this.setMaxX(Math.max(this.getMaxX(), (int) Math.ceil(other.maxX)));
		this.setMaxY(Math.max(this.getMaxY(), (int) Math.ceil(other.maxY)));
		this.setMaxZ(Math.max(this.getMaxZ(), (int) Math.ceil(other.maxZ)));
	}

	public void assign(AxisAlignedBB other) {
		this.setMinX((int) Math.floor(other.minX));
		this.setMinY((int) Math.floor(other.minY));
		this.setMinZ((int) Math.floor(other.minZ));
		this.setMaxX((int) Math.ceil(other.maxX));
		this.setMaxY((int) Math.ceil(other.maxY));
		this.setMaxZ((int) Math.ceil(other.maxZ));
	}

	public void assign(ReadOnlyBox other) {
		this.setMinX(other.getMinX());
		this.setMinY(other.getMinY());
		this.setMinZ(other.getMinZ());
		this.setMaxX(other.getMaxX());
		this.setMaxY(other.getMaxY());
		this.setMaxZ(other.getMaxZ());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ReadOnlyBox that = (ReadOnlyBox) o;

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
}
