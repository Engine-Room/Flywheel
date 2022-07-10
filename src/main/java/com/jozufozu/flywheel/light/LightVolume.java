package com.jozufozu.flywheel.light;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.util.box.GridAlignedBB;
import com.jozufozu.flywheel.util.box.ImmutableBox;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;

public class LightVolume implements ImmutableBox, LightListener {

	protected final BlockAndTintGetter level;
	protected final GridAlignedBB box = new GridAlignedBB();
	protected ByteBuffer lightData;

	public LightVolume(BlockAndTintGetter level, ImmutableBox sampleVolume) {
		this.level = level;
		this.setBox(sampleVolume);

		this.lightData = MemoryUtil.memAlloc(this.box.volume() * 2);
	}

	protected void setBox(ImmutableBox box) {
		this.box.assign(box);
	}

	public short getPackedLight(int x, int y, int z) {
		if (box.contains(x, y, z)) {
			return lightData.getShort(worldPosToBufferIndex(x, y, z));
		} else {
			return 0;
		}
	}

	public int getMinX() {
		return box.getMinX();
	}

	public int getMinY() {
		return box.getMinY();
	}

	public int getMinZ() {
		return box.getMinZ();
	}

	public int getMaxX() {
		return box.getMaxX();
	}

	public int getMaxY() {
		return box.getMaxY();
	}

	public int getMaxZ() {
		return box.getMaxZ();
	}

	public void move(ImmutableBox newSampleVolume) {
		if (lightData == null) return;

		setBox(newSampleVolume);
		int neededCapacity = box.volume() * 2;
		if (neededCapacity > lightData.capacity()) {
			lightData = MemoryUtil.memRealloc(lightData, neededCapacity);
		}
		initialize();
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

	/**
	 * Completely (re)populate this volume with block and sky lighting data.
	 * This is expensive and should be avoided.
	 */
	public void initialize() {
		if (lightData == null) return;

		copyLight(getVolume());
		markDirty();
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

	public void delete() {
		MemoryUtil.memFree(lightData);
		lightData = null;
	}

	protected void markDirty() {
		// noop
	}

	protected void writeLight(int x, int y, int z, int block, int sky) {
		byte b = (byte) ((block & 0xF) << 4);
		byte s = (byte) ((sky & 0xF) << 4);

		int i = boxPosToBufferIndex(x, y, z);
		lightData.put(i, b);
		lightData.put(i + 1, s);
	}

	protected void writeBlock(int x, int y, int z, int block) {
		byte b = (byte) ((block & 0xF) << 4);

		lightData.put(boxPosToBufferIndex(x, y, z), b);
	}

	protected void writeSky(int x, int y, int z, int sky) {
		byte b = (byte) ((sky & 0xF) << 4);

		lightData.put(boxPosToBufferIndex(x, y, z) + 1, b);
	}

	protected int worldPosToBufferIndex(int x, int y, int z) {
		x -= box.getMinX();
		y -= box.getMinY();
		z -= box.getMinZ();
		return boxPosToBufferIndex(x, y, z);
	}

	protected int boxPosToBufferIndex(int x, int y, int z) {
		return (x + box.sizeX() * (y + z * box.sizeY())) * 2;
	}

	@Override
	public ImmutableBox getVolume() {
		return box;
	}

	@Override
	public boolean isListenerInvalid() {
		return lightData == null;
	}

}
