package com.jozufozu.flywheel.lib.model.baked;

import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LevelLightEngine;

public final class VirtualLightEngine extends LevelLightEngine {
	private final LayerLightEventListener blockListener;
	private final LayerLightEventListener skyListener;

	public VirtualLightEngine(ToIntFunction<BlockPos> blockLightFunc, ToIntFunction<BlockPos> skyLightFunc, BlockGetter level) {
		super(new LightChunkGetter() {
					@Override
					@Nullable
					public LightChunk getChunkForLighting(int x, int z) {
						return null;
					}

					@Override
					public BlockGetter getLevel() {
						return level;
					}
				}, false, false);

		blockListener = new VirtualLayerLightEventListener(blockLightFunc);
		skyListener = new VirtualLayerLightEventListener(skyLightFunc);
	}

	@Override
	public LayerLightEventListener getLayerListener(LightLayer layer) {
		return layer == LightLayer.BLOCK ? blockListener : skyListener;
	}

	@Override
	public int getRawBrightness(BlockPos pos, int amount) {
		int i = skyListener.getLightValue(pos) - amount;
		int j = blockListener.getLightValue(pos);
		return Math.max(j, i);
	}

	private static class VirtualLayerLightEventListener implements LayerLightEventListener {
		private final ToIntFunction<BlockPos> lightFunc;

		public VirtualLayerLightEventListener(ToIntFunction<BlockPos> lightFunc) {
			this.lightFunc = lightFunc;
		}

		@Override
		public void checkBlock(BlockPos pos) {
		}

		@Override
		public boolean hasLightWork() {
			return false;
		}

		@Override
		public int runLightUpdates() {
			return 0;
		}

		@Override
		public void updateSectionStatus(SectionPos pos, boolean isSectionEmpty) {
		}

		@Override
		public void setLightEnabled(ChunkPos pos, boolean lightEnabled) {
		}

		@Override
		public void propagateLightSources(ChunkPos pos) {
		}

		@Override
		public DataLayer getDataLayerData(SectionPos pos) {
			return null;
		}

		@Override
		public int getLightValue(BlockPos pos) {
			return lightFunc.applyAsInt(pos);
		}
	}
}
