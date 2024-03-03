package com.jozufozu.flywheel.backend.engine.embed;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.lib.memory.MemoryBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;

public class EmbeddedLightVolume {
	public static final long STRIDE = Short.BYTES;
	public int minX;
	public int minY;
	public int minZ;
	public int sizeX;
	public int sizeY;
	public int sizeZ;
	private final BlockPos.MutableBlockPos scratchPos = new BlockPos.MutableBlockPos();

	@Nullable
	protected MemoryBlock block;
	protected boolean dirty;

	public boolean empty() {
		return block == null;
	}

	public void collect(BlockAndTintGetter level, int minX, int minY, int minZ, int sizeX, int sizeY, int sizeZ) {
		maybeExpandForBox(minX, minY, minZ, sizeX, sizeY, sizeZ);

		for (int z = minZ; z < minZ + sizeZ; z++) {
			for (int y = minY; y < minY + sizeY; y++) {
				for (int x = minX; x < minX + sizeX; x++) {
					paintLight(level, x, y, z);
				}
			}
		}

		markDirty();
	}

	public void invalidate(int minX, int minY, int minZ, int sizeX, int sizeY, int sizeZ) {
		// TODO: shrink the volume
	}

	private void paintLight(BlockAndTintGetter level, int x, int y, int z) {
		scratchPos.set(x, y, z);

		int block = level.getBrightness(LightLayer.BLOCK, scratchPos);
		int sky = level.getBrightness(LightLayer.SKY, scratchPos);

		long ptr = worldPosToPtr(x, y, z);
		MemoryUtil.memPutShort(ptr, (short) ((block << 4) | sky << 12));
	}

	private void maybeExpandForBox(int minX, int minY, int minZ, int sizeX, int sizeY, int sizeZ) {
		if (block == null) {
			this.minX = minX;
			this.minY = minY;
			this.minZ = minZ;
			this.sizeX = sizeX;
			this.sizeY = sizeY;
			this.sizeZ = sizeZ;

			int volume = sizeX * sizeY * sizeZ;

			block = MemoryBlock.malloc(volume * STRIDE);
			block.clear();
			return;
		}

		int newMinX = Math.min(this.minX, minX);
		int newMinY = Math.min(this.minY, minY);
		int newMinZ = Math.min(this.minZ, minZ);

		int newSizeX = Math.max(this.minX + this.sizeX, minX + sizeX) - newMinX;
		int newSizeY = Math.max(this.minY + this.sizeY, minY + sizeY) - newMinY;
		int newSizeZ = Math.max(this.minZ + this.sizeZ, minZ + sizeZ) - newMinZ;

		if (newMinX == this.minX && newMinY == this.minY && newMinZ == this.minZ && newSizeX == this.sizeX && newSizeY == this.sizeY && newSizeZ == this.sizeZ) {
			return;
		}

		int newVolume = newSizeX * newSizeY * newSizeZ;

		MemoryBlock newBlock = MemoryBlock.malloc(newVolume * STRIDE);
		newBlock.clear();

		int xOff = newMinX - this.minX;
		int yOff = newMinY - this.minY;
		int zOff = newMinZ - this.minZ;

		for (int z = 0; z < this.sizeZ; z++) {
			for (int y = 0; y < this.sizeY; y++) {
				for (int x = 0; x < this.sizeX; x++) {
					long oldPtr = boxPosToPtr(x, y, z);
					long newPtr = newBlock.ptr() + x + xOff + (newSizeX * (y + yOff + (z + zOff) * newSizeY)) * STRIDE;

					MemoryUtil.memPutShort(newPtr, MemoryUtil.memGetShort(oldPtr));
				}
			}
		}

		this.minX = newMinX;
		this.minY = newMinY;
		this.minZ = newMinZ;
		this.sizeX = newSizeX;
		this.sizeY = newSizeY;
		this.sizeZ = newSizeZ;

		block.free();
		block = newBlock;
	}

	protected long worldPosToPtr(int x, int y, int z) {
		return block.ptr() + worldPosToPtrOffset(x, y, z);
	}

	protected long boxPosToPtr(int x, int y, int z) {
		return block.ptr() + boxPosToPtrOffset(x, y, z);
	}

	protected long worldPosToPtrOffset(int x, int y, int z) {
		return boxPosToPtrOffset(x - minX, y - minY, z - minZ);
	}

	protected long boxPosToPtrOffset(int x, int y, int z) {
		return (x + sizeX * (y + z * sizeY)) * STRIDE;
	}

	public void delete() {
		if (block != null) {
			block.free();
			block = null;
		}
	}

	protected void markDirty() {
		this.dirty = true;
	}

	public long ptr() {
		return block.ptr();
	}
}
