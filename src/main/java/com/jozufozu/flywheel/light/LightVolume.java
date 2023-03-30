package com.jozufozu.flywheel.light;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.lib.memory.MemoryBlock;
import com.jozufozu.flywheel.util.box.GridAlignedBB;
import com.jozufozu.flywheel.util.box.ImmutableBox;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;

public class LightVolume implements ImmutableBox, LightListener {

	protected final BlockAndTintGetter level;
	protected final GridAlignedBB box = new GridAlignedBB();
	protected MemoryBlock lightData;

	public LightVolume(BlockAndTintGetter level, ImmutableBox sampleVolume) {
		this.level = level;
		this.setBox(sampleVolume);

		this.lightData = MemoryBlock.malloc(this.box.volume() * 2);
	}

	@Override
	public ImmutableBox getVolume() {
		return box;
	}

	@Override
	public int getMinX() {
		return box.getMinX();
	}

	@Override
	public int getMinY() {
		return box.getMinY();
	}

	@Override
	public int getMinZ() {
		return box.getMinZ();
	}

	@Override
	public int getMaxX() {
		return box.getMaxX();
	}

	@Override
	public int getMaxY() {
		return box.getMaxY();
	}

	@Override
	public int getMaxZ() {
		return box.getMaxZ();
	}

	@Override
	public boolean isListenerInvalid() {
		return lightData == null;
	}

	protected void setBox(ImmutableBox box) {
		this.box.assign(box);
	}

	public short getPackedLight(int x, int y, int z) {
		if (box.contains(x, y, z)) {
			return MemoryUtil.memGetShort(worldPosToPtr(x, y, z));
		} else {
			return 0;
		}
	}

	public void move(ImmutableBox newSampleVolume) {
		if (lightData == null) return;

		setBox(newSampleVolume);
		int neededCapacity = box.volume() * 2;
		if (neededCapacity > lightData.size()) {
			lightData = lightData.realloc(neededCapacity);
		}
		initialize();
	}

	/**
	 * Completely (re)populate this volume with block and sky lighting data.
	 * This is expensive and should be avoided.
	 */
	public void initialize() {
		if (lightData == null) return;

		copyLight(getVolume());
		markDirty();
	}

	protected void markDirty() {
		// noop
	}

	public void delete() {
		lightData.free();
		lightData = null;
	}

	/**
	 * Copy all light from the world into this volume.
	 *
	 * @param worldVolume the region in the world to copy data from.
	 */
	public void copyLight(ImmutableBox worldVolume) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		int xShift = box.getMinX();
		int yShift = box.getMinY();
		int zShift = box.getMinZ();

		worldVolume.forEachContained((x, y, z) -> {
			pos.set(x, y, z);

			int block = this.level.getBrightness(LightLayer.BLOCK, pos);
			int sky = this.level.getBrightness(LightLayer.SKY, pos);

			writeLight(x - xShift, y - yShift, z - zShift, block, sky);
		});
	}

	protected void writeLight(int x, int y, int z, int block, int sky) {
		byte b = (byte) ((block & 0xF) << 4);
		byte s = (byte) ((sky & 0xF) << 4);

		long ptr = boxPosToPtr(x, y, z);
		MemoryUtil.memPutByte(ptr, b);
		MemoryUtil.memPutByte(ptr + 1, s);
	}

	/**
	 * Copy block light from the world into this volume.
	 *
	 * @param worldVolume the region in the world to copy data from.
	 */
	public void copyBlock(ImmutableBox worldVolume) {
		var pos = new BlockPos.MutableBlockPos();

		int xShift = box.getMinX();
		int yShift = box.getMinY();
		int zShift = box.getMinZ();

		worldVolume.forEachContained((x, y, z) -> {
			int light = this.level.getBrightness(LightLayer.BLOCK, pos.set(x, y, z));

			writeBlock(x - xShift, y - yShift, z - zShift, light);
		});
	}

	protected void writeBlock(int x, int y, int z, int block) {
		byte b = (byte) ((block & 0xF) << 4);

		MemoryUtil.memPutByte(boxPosToPtr(x, y, z), b);
	}

	/**
	 * Copy sky light from the world into this volume.
	 *
	 * @param worldVolume the region in the world to copy data from.
	 */
	public void copySky(ImmutableBox worldVolume) {
		var pos = new BlockPos.MutableBlockPos();

		int xShift = box.getMinX();
		int yShift = box.getMinY();
		int zShift = box.getMinZ();

		worldVolume.forEachContained((x, y, z) -> {
			int light = this.level.getBrightness(LightLayer.SKY, pos.set(x, y, z));

			writeSky(x - xShift, y - yShift, z - zShift, light);
		});
	}

	protected void writeSky(int x, int y, int z, int sky) {
		byte s = (byte) ((sky & 0xF) << 4);

		MemoryUtil.memPutByte(boxPosToPtr(x, y, z) + 1, s);
	}

	protected long worldPosToPtr(int x, int y, int z) {
		return lightData.ptr() + worldPosToPtrOffset(x, y, z);
	}

	protected long boxPosToPtr(int x, int y, int z) {
		return lightData.ptr() + boxPosToPtrOffset(x, y, z);
	}

	protected int worldPosToPtrOffset(int x, int y, int z) {
		x -= box.getMinX();
		y -= box.getMinY();
		z -= box.getMinZ();
		return boxPosToPtrOffset(x, y, z);
	}

	protected int boxPosToPtrOffset(int x, int y, int z) {
		return (x + box.sizeX() * (y + z * box.sizeY())) * 2;
	}

	@Override
	public void onLightUpdate(LightLayer type, ImmutableBox changedVolume) {
		if (lightData == null) return;

		GridAlignedBB vol = changedVolume.copy();
		if (!vol.intersects(getVolume())) return;
		vol.intersectAssign(getVolume()); // compute the region contained by us that has dirty lighting data.

		if (type == LightLayer.BLOCK) copyBlock(vol);
		else if (type == LightLayer.SKY) copySky(vol);
		markDirty();
	}

	@Override
	public void onLightPacket(int chunkX, int chunkZ) {
		if (lightData == null) return;

		GridAlignedBB changedVolume = GridAlignedBB.from(chunkX, chunkZ);
		if (!changedVolume.intersects(getVolume())) return;
		changedVolume.intersectAssign(getVolume()); // compute the region contained by us that has dirty lighting data.

		copyLight(changedVolume);
		markDirty();
	}

}
