package com.jozufozu.flywheel.light;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;

public class LightVolume implements ImmutableBox, ILightUpdateListener {

	protected final GridAlignedBB box = new GridAlignedBB();
	protected ByteBuffer lightData;

	public LightVolume(ImmutableBox sampleVolume) {
		this.setBox(sampleVolume);

		this.lightData = MemoryUtil.memAlloc(this.box.volume() * getStride());
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

	public void move(LightProvider world, ImmutableBox newSampleVolume) {
		if (lightData == null) return;

		setBox(newSampleVolume);
		int volume = box.volume();
		if (volume * 2 > lightData.capacity()) {
			lightData = MemoryUtil.memRealloc(lightData, volume * 2);
		}
		initialize(world);
	}

	@Override
	public void onLightUpdate(LightProvider world, LightLayer type, ImmutableBox changedVolume) {
		if (lightData == null) return;

		GridAlignedBB vol = changedVolume.copy();
		if (!vol.intersects(getVolume())) return;
		vol.intersectAssign(getVolume()); // compute the region contained by us that has dirty lighting data.

		if (type == LightLayer.BLOCK) copyBlock(world, vol);
		else if (type == LightLayer.SKY) copySky(world, vol);
	}

	@Override
	public void onLightPacket(LightProvider world, int chunkX, int chunkZ) {
		if (lightData == null) return;

		GridAlignedBB changedVolume = GridAlignedBB.from(chunkX, chunkZ);
		if (!changedVolume.intersects(getVolume())) return;
		changedVolume.intersectAssign(getVolume()); // compute the region contained by us that has dirty lighting data.

		copyLight(world, changedVolume);
	}

	/**
	 * Completely (re)populate this volume with block and sky lighting data.
	 * This is expensive and should be avoided.
	 */
	public void initialize(LightProvider world) {
		if (lightData == null) return;

		ImmutableBox box = getVolume();
		int shiftX = box.getMinX();
		int shiftY = box.getMinY();
		int shiftZ = box.getMinZ();

		box.forEachContained((x, y, z) -> {
			int blockLight = world.getLight(LightLayer.BLOCK, x, y, z);
			int skyLight = world.getLight(LightLayer.SKY, x, y, z);

			writeLight(x - shiftX, y - shiftY, z - shiftZ, blockLight, skyLight);
		});
	}

	/**
	 * Copy block light from the world into this volume.
	 *
	 * @param worldVolume the region in the world to copy data from.
	 */
	public void copyBlock(LightProvider world, ImmutableBox worldVolume) {
		int xShift = box.getMinX();
		int yShift = box.getMinY();
		int zShift = box.getMinZ();

		worldVolume.forEachContained((x, y, z) -> {
			int light = world.getLight(LightLayer.BLOCK, x, y, z);

			writeBlock(x - xShift, y - yShift, z - zShift, light);
		});
	}

	/**
	 * Copy sky light from the world into this volume.
	 *
	 * @param worldVolume the region in the world to copy data from.
	 */
	public void copySky(LightProvider world, ImmutableBox worldVolume) {
		int xShift = box.getMinX();
		int yShift = box.getMinY();
		int zShift = box.getMinZ();

		worldVolume.forEachContained((x, y, z) -> {
			int light = world.getLight(LightLayer.SKY, x, y, z);

			writeSky(x - xShift, y - yShift, z - zShift, light);
		});
	}

	/**
	 * Copy all light from the world into this volume.
	 *
	 * @param worldVolume the region in the world to copy data from.
	 */
	public void copyLight(LightProvider world, ImmutableBox worldVolume) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		int xShift = box.getMinX();
		int yShift = box.getMinY();
		int zShift = box.getMinZ();

		worldVolume.forEachContained((x, y, z) -> {
			pos.set(x, y, z);

			int block = world.getLight(LightLayer.BLOCK, x, y, z);
			int sky = world.getLight(LightLayer.SKY, x, y, z);

			writeLight(x - xShift, y - yShift, z - zShift, block, sky);
		});
	}

	public void delete() {
		MemoryUtil.memFree(lightData);
		lightData = null;
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
		return (x + box.sizeX() * (y + z * box.sizeY())) * getStride();
	}

	/**
	 * @return The stride of the texels, in bytes.
	 */
	protected int getStride() {
		return 2;
	}

	@Override
	public ImmutableBox getVolume() {
		return box;
	}

	@Override
	public ListenerStatus status() {
		return ListenerStatus.OKAY;
	}

}
