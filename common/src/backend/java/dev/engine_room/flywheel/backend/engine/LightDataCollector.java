package dev.engine_room.flywheel.backend.engine;

import java.util.BitSet;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.backend.SkyLightSectionStorageExtension;
import dev.engine_room.flywheel.backend.mixin.light.LayerLightSectionStorageAccessor;
import dev.engine_room.flywheel.backend.mixin.light.LightEngineAccessor;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LayerLightEventListener;

public abstract class LightDataCollector {
	private static final ConstantDataLayer ALWAYS_0 = new ConstantDataLayer(0);
	private static final ConstantDataLayer ALWAYS_15 = new ConstantDataLayer(15);

	protected final LevelAccessor level;
	protected final LayerLightEventListener skyLayerListener;
	protected final LayerLightEventListener blockLayerListener;

	protected LightDataCollector(LevelAccessor level, LayerLightEventListener skyLayerListener, LayerLightEventListener blockLayerListener) {
		this.level = level;
		this.skyLayerListener = skyLayerListener;
		this.blockLayerListener = blockLayerListener;
	}

	public static LightDataCollector of(LevelAccessor level) {
		LayerLightEventListener skyLayerListener = level.getLightEngine().getLayerListener(LightLayer.SKY);
		LayerLightEventListener blockLayerListener = level.getLightEngine().getLayerListener(LightLayer.BLOCK);

		Long2ObjectFunction<DataLayer> fastSkyDataGetter = createFastSkyDataGetter(skyLayerListener);
		Long2ObjectFunction<DataLayer> fastBlockDataGetter = createFastBlockDataGetter(blockLayerListener);

		if (fastSkyDataGetter != null && fastBlockDataGetter != null) {
			return new Fast(level, skyLayerListener, blockLayerListener, fastSkyDataGetter, fastBlockDataGetter);
		} else {
			return new Slow(level, skyLayerListener, blockLayerListener);
		}
	}

	@Nullable
	private static Long2ObjectFunction<DataLayer> createFastSkyDataGetter(LayerLightEventListener layerListener) {
		if (layerListener == LayerLightEventListener.DummyLightLayerEventListener.INSTANCE) {
			// The dummy listener always returns 0.
			// In vanilla this happens in the nether and end,
			// and the light texture is simply updated
			// to be invariant on sky light.
			return section -> ALWAYS_0;
		}

		if (layerListener instanceof LightEngineAccessor<?, ?> accessor) {
			// Sky storage has a fancy way to get the sky light at a given block position, but the logic is not
			// implemented in vanilla for fetching data layers directly. We need to re-implement it here. The simplest
			// way to do it was to expose the same logic via an extension method. Re-implementing it external to the
			// SkyLightSectionStorage class would require many more accessors.
			if (accessor.flywheel$storage() instanceof SkyLightSectionStorageExtension skyStorage) {
				return section -> {
					var out = skyStorage.flywheel$skyDataLayer(section);

					// Null section here means there are no blocks above us to darken this section.
					return Objects.requireNonNullElse(out, ALWAYS_15);
				};
			}
		}

		return null;
	}

	@Nullable
	private static Long2ObjectFunction<DataLayer> createFastBlockDataGetter(LayerLightEventListener layerListener) {
		if (layerListener == LayerLightEventListener.DummyLightLayerEventListener.INSTANCE) {
			return section -> ALWAYS_0;
		}

		if (layerListener instanceof LightEngineAccessor<?, ?> accessor) {
			if (accessor.flywheel$storage() instanceof LayerLightSectionStorageAccessor storage) {
				return section -> {
					var out = storage.flywheel$callGetDataLayer(section, false);
					return Objects.requireNonNullElse(out, ALWAYS_0);
				};
			}
		}

		return null;
	}

	public void collectSection(long ptr, long section) {
		collectSolidData(ptr, section);
		collectLightData(ptr, section);
	}

	private void collectSolidData(long ptr, long section) {
		var blockPos = new BlockPos.MutableBlockPos();
		int xMin = SectionPos.sectionToBlockCoord(SectionPos.x(section));
		int yMin = SectionPos.sectionToBlockCoord(SectionPos.y(section));
		int zMin = SectionPos.sectionToBlockCoord(SectionPos.z(section));

		var bitSet = new BitSet(LightStorage.BLOCKS_PER_SECTION);
		int index = 0;
		for (int y = -1; y < 17; y++) {
			for (int z = -1; z < 17; z++) {
				for (int x = -1; x < 17; x++) {
					blockPos.set(xMin + x, yMin + y, zMin + z);

					var blockState = level.getBlockState(blockPos);

					if (blockState.canOcclude() && blockState.isCollisionShapeFullBlock(level, blockPos)) {
						bitSet.set(index);
					}

					index++;
				}
			}
		}

		var longArray = bitSet.toLongArray();
		for (long l : longArray) {
			MemoryUtil.memPutLong(ptr, l);
			ptr += Long.BYTES;
		}
	}

	protected abstract void collectLightData(long ptr, long section);

	/**
	 * Write to the given section.
	 *
	 * @param ptr   Pointer to the base of a section's data.
	 * @param x     X coordinate in the section, from [-1, 16].
	 * @param y     Y coordinate in the section, from [-1, 16].
	 * @param z     Z coordinate in the section, from [-1, 16].
	 * @param block The block light level, from [0, 15].
	 * @param sky   The sky light level, from [0, 15].
	 */
	protected static void write(long ptr, int x, int y, int z, int block, int sky) {
		int x1 = x + 1;
		int y1 = y + 1;
		int z1 = z + 1;

		int offset = x1 + z1 * 18 + y1 * 18 * 18;

		long packedByte = (block & 0xF) | ((sky & 0xF) << 4);

		MemoryUtil.memPutByte(ptr + LightStorage.SOLID_SIZE_BYTES + offset, (byte) packedByte);
	}

	private static class Fast extends LightDataCollector {
		private final Long2ObjectFunction<DataLayer> skyDataGetter;
		private final Long2ObjectFunction<DataLayer> blockDataGetter;

		public Fast(LevelAccessor level, LayerLightEventListener skyLayerListener, LayerLightEventListener blockLayerListener, Long2ObjectFunction<DataLayer> skyDataGetter, Long2ObjectFunction<DataLayer> blockDataGetter) {
			super(level, skyLayerListener, blockLayerListener);
			this.skyDataGetter = skyDataGetter;
			this.blockDataGetter = blockDataGetter;
		}

		@Override
		protected void collectLightData(long ptr, long section) {
			collectCenter(ptr, section);

			for (SectionEdge i : SectionEdge.VALUES) {
				collectYZPlane(ptr, SectionPos.offset(section, i.sectionOffset, 0, 0), i);
				collectXZPlane(ptr, SectionPos.offset(section, 0, i.sectionOffset, 0), i);
				collectXYPlane(ptr, SectionPos.offset(section, 0, 0, i.sectionOffset), i);

				for (SectionEdge j : SectionEdge.VALUES) {
					collectXStrip(ptr, SectionPos.offset(section, 0, i.sectionOffset, j.sectionOffset), i, j);
					collectYStrip(ptr, SectionPos.offset(section, i.sectionOffset, 0, j.sectionOffset), i, j);
					collectZStrip(ptr, SectionPos.offset(section, i.sectionOffset, j.sectionOffset, 0), i, j);
				}
			}

			collectCorners(ptr, section);
		}

		private void collectXStrip(long ptr, long section, SectionEdge y, SectionEdge z) {
			var blockData = blockDataGetter.get(section);
			var skyData = skyDataGetter.get(section);
			for (int x = 0; x < 16; x++) {
				write(ptr, x, y.relative, z.relative, blockData.get(x, y.pos, z.pos), skyData.get(x, y.pos, z.pos));
			}
		}

		private void collectYStrip(long ptr, long section, SectionEdge x, SectionEdge z) {
			var blockData = blockDataGetter.get(section);
			var skyData = skyDataGetter.get(section);
			for (int y = 0; y < 16; y++) {
				write(ptr, x.relative, y, z.relative, blockData.get(x.pos, y, z.pos), skyData.get(x.pos, y, z.pos));
			}
		}

		private void collectZStrip(long ptr, long section, SectionEdge x, SectionEdge y) {
			var blockData = blockDataGetter.get(section);
			var skyData = skyDataGetter.get(section);
			for (int z = 0; z < 16; z++) {
				write(ptr, x.relative, y.relative, z, blockData.get(x.pos, y.pos, z), skyData.get(x.pos, y.pos, z));
			}
		}

		private void collectYZPlane(long ptr, long section, SectionEdge x) {
			var blockData = blockDataGetter.get(section);
			var skyData = skyDataGetter.get(section);
			for (int y = 0; y < 16; y++) {
				for (int z = 0; z < 16; z++) {
					write(ptr, x.relative, y, z, blockData.get(x.pos, y, z), skyData.get(x.pos, y, z));
				}
			}
		}

		private void collectXZPlane(long ptr, long section, SectionEdge y) {
			var blockData = blockDataGetter.get(section);
			var skyData = skyDataGetter.get(section);
			for (int z = 0; z < 16; z++) {
				for (int x = 0; x < 16; x++) {
					write(ptr, x, y.relative, z, blockData.get(x, y.pos, z), skyData.get(x, y.pos, z));
				}
			}
		}

		private void collectXYPlane(long ptr, long section, SectionEdge z) {
			var blockData = blockDataGetter.get(section);
			var skyData = skyDataGetter.get(section);
			for (int y = 0; y < 16; y++) {
				for (int x = 0; x < 16; x++) {
					write(ptr, x, y, z.relative, blockData.get(x, y, z.pos), skyData.get(x, y, z.pos));
				}
			}
		}

		private void collectCenter(long ptr, long section) {
			var blockData = blockDataGetter.get(section);
			var skyData = skyDataGetter.get(section);
			for (int y = 0; y < 16; y++) {
				for (int z = 0; z < 16; z++) {
					for (int x = 0; x < 16; x++) {
						write(ptr, x, y, z, blockData.get(x, y, z), skyData.get(x, y, z));
					}
				}
			}
		}

		private void collectCorners(long ptr, long section) {
			var blockLayerListener = this.blockLayerListener;
			var skyLayerListener = this.skyLayerListener;

			var blockPos = new BlockPos.MutableBlockPos();
			int xMin = SectionPos.sectionToBlockCoord(SectionPos.x(section));
			int yMin = SectionPos.sectionToBlockCoord(SectionPos.y(section));
			int zMin = SectionPos.sectionToBlockCoord(SectionPos.z(section));

			for (SectionEdge y : SectionEdge.VALUES) {
				for (SectionEdge z : SectionEdge.VALUES) {
					for (SectionEdge x : SectionEdge.VALUES) {
						blockPos.set(x.relative + xMin, y.relative + yMin, z.relative + zMin);
						write(ptr, x.relative, y.relative, z.relative, blockLayerListener.getLightValue(blockPos), skyLayerListener.getLightValue(blockPos));
					}
				}
			}
		}

		private enum SectionEdge {
			LOW(15, -1, -1),
			HIGH(0, 16, 1),
			;

			public static final SectionEdge[] VALUES = values();

			/**
			 * The position in the section to collect.
			 */
			private final int pos;
			/**
			 * The position relative to the main section.
			 */
			private final int relative;
			/**
			 * The offset to the neighboring section.
			 */
			private final int sectionOffset;

			SectionEdge(int pos, int relative, int sectionOffset) {
				this.pos = pos;
				this.relative = relative;
				this.sectionOffset = sectionOffset;
			}
		}
	}

	private static class Slow extends LightDataCollector {
		public Slow(LevelAccessor level, LayerLightEventListener skyLayerListener, LayerLightEventListener blockLayerListener) {
			super(level, skyLayerListener, blockLayerListener);
		}

		@Override
		protected void collectLightData(long ptr, long section) {
			var blockLayerListener = this.blockLayerListener;
			var skyLayerListener = this.skyLayerListener;

			var blockPos = new BlockPos.MutableBlockPos();
			int xMin = SectionPos.sectionToBlockCoord(SectionPos.x(section));
			int yMin = SectionPos.sectionToBlockCoord(SectionPos.y(section));
			int zMin = SectionPos.sectionToBlockCoord(SectionPos.z(section));

			for (int y = -1; y < 17; y++) {
				for (int z = -1; z < 17; z++) {
					for (int x = -1; x < 17; x++) {
						blockPos.set(xMin + x, yMin + y, zMin + z);
						write(ptr, x, y, z, blockLayerListener.getLightValue(blockPos), skyLayerListener.getLightValue(blockPos));
					}
				}
			}
		}
	}

	private static class ConstantDataLayer extends DataLayer {
		private final int value;

		private ConstantDataLayer(int value) {
			this.value = value;
		}

		@Override
		public int get(int x, int y, int z) {
			return value;
		}
	}
}
