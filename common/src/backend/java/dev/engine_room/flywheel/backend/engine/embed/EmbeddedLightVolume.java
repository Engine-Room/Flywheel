package dev.engine_room.flywheel.backend.engine.embed;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.lib.memory.MemoryBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;

public class EmbeddedLightVolume {
	public static final long STRIDE = Short.BYTES;

	private int minX;
	private int minY;
	private int minZ;
	private int maxX;
	private int maxY;
	private int maxZ;

	private final BlockPos.MutableBlockPos scratchPos = new BlockPos.MutableBlockPos();

	@Nullable
	protected MemoryBlock memoryBlock;
	protected boolean empty = true;

	public boolean empty() {
		return empty;
	}

	public void collect(BlockAndTintGetter level, int minX, int minY, int minZ, int sizeX, int sizeY, int sizeZ) {
		maybeExpandForBox(minX, minY, minZ, sizeX, sizeY, sizeZ);

		empty = false;

		for (int z = minZ; z < minZ + sizeZ; z++) {
			for (int y = minY; y < minY + sizeY; y++) {
				for (int x = minX; x < minX + sizeX; x++) {
					paintLight(level, x, y, z);
				}
			}
		}
	}

	private void paintLight(BlockAndTintGetter level, int x, int y, int z) {
		scratchPos.set(x, y, z);

		int block = level.getBrightness(LightLayer.BLOCK, scratchPos);
		int sky = level.getBrightness(LightLayer.SKY, scratchPos);

		long ptr = this.memoryBlock.ptr() + offset(x - x(), y - y(), z - z(), sizeX(), sizeY());
		MemoryUtil.memPutShort(ptr, (short) ((block << 4) | sky << 12));
	}

	private void maybeExpandForBox(int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
		if (empty || memoryBlock == null) {
			// We're either brand new or recently #clear'd,
			// so none of the previous min/max values have any meaning.
			this.minX = x;
			this.minY = y;
			this.minZ = z;
			this.maxX = x + sizeX;
			this.maxY = y + sizeY;
			this.maxZ = z + sizeZ;

			int volume = sizeX * sizeY * sizeZ;
			long neededSize = volume * STRIDE;

			if (memoryBlock == null) {
				memoryBlock = MemoryBlock.malloc(neededSize);
			} else if (memoryBlock.size() < neededSize) {
				// There's some memory left over from before the last #clear,
				// but not enough to hold this initial box. Need to grow the block.
				memoryBlock.realloc(neededSize);
			}
			// else: we have enough memory left over to hold this box, nothing to do!
			return;
		}

		int oldMinX = this.minX;
		int oldMinY = this.minY;
		int oldMinZ = this.minZ;
		int oldSizeX = this.sizeX();
		int oldSizeY = this.sizeY();
		int oldSizeZ = this.sizeZ();
		boolean changed = false;

		if (x < this.minX) {
			this.minX = x;
			changed = true;
		}
		if (y < this.minY) {
			this.minY = y;
			changed = true;
		}
		if (z < this.minZ) {
			this.minZ = z;
			changed = true;
		}
		if (x + sizeX > this.maxX) {
			this.maxX = x + sizeX;
			changed = true;
		}
		if (y + sizeY > this.maxY) {
			this.maxY = y + sizeY;
			changed = true;
		}
		if (z + sizeZ > this.maxZ) {
			this.maxZ = z + sizeZ;
			changed = true;
		}

		if (!changed) {
			return;
		}

		int volume = volume();

		memoryBlock = memoryBlock.realloc(volume * STRIDE);

		int xOff = oldMinX - minX;
		int yOff = oldMinY - minY;
		int zOff = oldMinZ - minZ;

		blit(memoryBlock.ptr(), 0, 0, 0, oldSizeX, oldSizeY, memoryBlock.ptr(), xOff, yOff, zOff, sizeX(), sizeY(), oldSizeX, oldSizeY, oldSizeZ);
	}

	public static void blit(long src, int srcX, int srcY, int srcZ, int srcSizeX, int srcSizeY, long dst, int dstX, int dstY, int dstZ, int dstSizeX, int dstSizeY, int sizeX, int sizeY, int sizeZ) {
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					long srcPtr = src + offset(x + srcX, y + srcY, z + srcZ, srcSizeX, srcSizeY);
					long dstPtr = dst + offset(x + dstX, y + dstY, z + dstZ, dstSizeX, dstSizeY);

					MemoryUtil.memPutShort(dstPtr, MemoryUtil.memGetShort(srcPtr));
				}
			}
		}
	}

	public static long offset(int x, int y, int z, int sizeX, int sizeY) {
		return (x + sizeX * (y + sizeY * z)) * STRIDE;
	}

	public void clear() {
		empty = true;
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
		return minX;
	}

	public int y() {
		return minY;
	}

	public int z() {
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
}
