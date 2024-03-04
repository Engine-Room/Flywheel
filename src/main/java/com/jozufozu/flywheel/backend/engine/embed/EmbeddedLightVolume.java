package com.jozufozu.flywheel.backend.engine.embed;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.backend.util.BoxSet;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;

public class EmbeddedLightVolume {
	public static final long STRIDE = Short.BYTES;

	private final BoxSet wantedCoords = new BoxSet();

	private final BlockPos.MutableBlockPos scratchPos = new BlockPos.MutableBlockPos();

	@Nullable
	protected MemoryBlock memoryBlock;

	public boolean empty() {
		return memoryBlock == null;
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
	}

	public void invalidate(int minX, int minY, int minZ, int sizeX, int sizeY, int sizeZ) {
		if (memoryBlock == null) {
			return;
		}

		int oldMinX = wantedCoords.minX();
		int oldMinY = wantedCoords.minY();
		int oldMinZ = wantedCoords.minZ();
		int oldSizeX = wantedCoords.sizeX();
		int oldSizeY = wantedCoords.sizeY();

		var shrank = wantedCoords.clear(minX, minY, minZ, sizeX, sizeY, sizeZ);

		if (!shrank) {
			return;
		}

		int newVolume = wantedCoords.volume();

		MemoryBlock newBlock = MemoryBlock.malloc(newVolume * STRIDE);

		int xOff = wantedCoords.minX() - oldMinX;
		int yOff = wantedCoords.minY() - oldMinY;
		int zOff = wantedCoords.minZ() - oldMinZ;

		blit(memoryBlock, xOff, yOff, zOff, oldSizeX, oldSizeY, newBlock, 0, 0, 0, wantedCoords.sizeX(), wantedCoords.sizeY(), wantedCoords.sizeX(), wantedCoords.sizeY(), wantedCoords.sizeZ());

		memoryBlock.free();
		memoryBlock = newBlock;
	}

	private void paintLight(BlockAndTintGetter level, int x, int y, int z) {
		scratchPos.set(x, y, z);

		int block = level.getBrightness(LightLayer.BLOCK, scratchPos);
		int sky = level.getBrightness(LightLayer.SKY, scratchPos);

		long ptr = this.memoryBlock.ptr() + offset(x - x(), y - y(), z - z(), sizeX(), sizeY());
		MemoryUtil.memPutShort(ptr, (short) ((block << 4) | sky << 12));
	}

	private void maybeExpandForBox(int minX, int minY, int minZ, int sizeX, int sizeY, int sizeZ) {
		int oldMinX = wantedCoords.minX();
		int oldMinY = wantedCoords.minY();
		int oldMinZ = wantedCoords.minZ();
		int oldSizeX = wantedCoords.sizeX();
		int oldSizeY = wantedCoords.sizeY();
		int oldSizeZ = wantedCoords.sizeZ();

		var grew = wantedCoords.add(minX, minY, minZ, sizeX, sizeY, sizeZ);

		if (memoryBlock == null) {
			int volume = sizeX * sizeY * sizeZ;

			memoryBlock = MemoryBlock.malloc(volume * STRIDE);
			return;
		}

		if (!grew) {
			return;
		}

		int newVolume = wantedCoords.volume();

		MemoryBlock newBlock = MemoryBlock.malloc(newVolume * STRIDE);

		int xOff = oldMinX - wantedCoords.minX();
		int yOff = oldMinY - wantedCoords.minY();
		int zOff = oldMinZ - wantedCoords.minZ();

		blit(memoryBlock, 0, 0, 0, oldSizeX, oldSizeY, newBlock, xOff, yOff, zOff, wantedCoords.sizeX(), wantedCoords.sizeY(), oldSizeX, oldSizeY, oldSizeZ);

		memoryBlock.free();
		memoryBlock = newBlock;
	}

	public static void blit(MemoryBlock src, int srcX, int srcY, int srcZ, int srcSizeX, int srcSizeY, MemoryBlock dst, int dstX, int dstY, int dstZ, int dstSizeX, int dstSizeY, int sizeX, int sizeY, int sizeZ) {
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					long srcPtr = src.ptr() + offset(x + srcX, y + srcY, z + srcZ, srcSizeX, srcSizeY);
					long dstPtr = dst.ptr() + offset(x + dstX, y + dstY, z + dstZ, dstSizeX, dstSizeY);

					MemoryUtil.memPutShort(dstPtr, MemoryUtil.memGetShort(srcPtr));
				}
			}
		}
	}

	public static long offset(int x, int y, int z, int sizeX, int sizeY) {
		return (x + sizeX * (y + z * sizeY)) * STRIDE;
	}

	public void delete() {
		if (memoryBlock != null) {
			memoryBlock.free();
			memoryBlock = null;
		}
	}

	public long ptr() {
		return memoryBlock.ptr();
	}

	public int x() {
		return wantedCoords.minX();
	}

	public int y() {
		return wantedCoords.minY();
	}

	public int z() {
		return wantedCoords.minZ();
	}

	public int sizeX() {
		return wantedCoords.sizeX();
	}

	public int sizeY() {
		return wantedCoords.sizeY();
	}

	public int sizeZ() {
		return wantedCoords.sizeZ();
	}
}
