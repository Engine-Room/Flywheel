package com.jozufozu.flywheel.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public enum VirtualEmptyBlockGetter implements BlockAndTintGetter {
	INSTANCE;

	private final LevelLightEngine lightEngine = new LevelLightEngine(new LightChunkGetter() {
		@Override
		public BlockGetter getChunkForLighting(int p_63023_, int p_63024_) {
			return VirtualEmptyBlockGetter.this;
		}

		@Override
		public BlockGetter getLevel() {
			return null;
		}
	}, false, false) {
		private static final LayerLightEventListener SKY_DUMMY_LISTENER = new LayerLightEventListener() {
			@Override
			public void checkBlock(BlockPos pos) {
			}

			@Override
			public void onBlockEmissionIncrease(BlockPos pos, int p_164456_) {
			}

			@Override
			public boolean hasLightWork() {
				return false;
			}

			@Override
			public int runUpdates(int p_164449_, boolean p_164450_, boolean p_164451_) {
				return p_164449_;
			}

			@Override
			public void updateSectionStatus(SectionPos pos, boolean p_75838_) {
			}

			@Override
			public void enableLightSources(ChunkPos pos, boolean p_164453_) {
			}

			@Override
			public DataLayer getDataLayerData(SectionPos pos) {
				return null;
			}

			@Override
			public int getLightValue(BlockPos pos) {
				return 15;
			}
		};

		@Override
		public LayerLightEventListener getLayerListener(LightLayer layer) {
			if (layer == LightLayer.BLOCK) {
				return LayerLightEventListener.DummyLightLayerEventListener.INSTANCE;
			} else {
				return SKY_DUMMY_LISTENER;
			}
		}

		@Override
		public int getRawBrightness(BlockPos pos, int skyDarken) {
			return 15 - skyDarken;
		}
	};

	public static boolean is(BlockAndTintGetter blockGetter) {
		return blockGetter == INSTANCE;
	}

	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
		return null;
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return Blocks.AIR.defaultBlockState();
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return Fluids.EMPTY.defaultFluidState();
	}

	@Override
	public int getHeight() {
		return 1;
	}

	@Override
	public int getMinBuildHeight() {
		return 0;
	}

	@Override
	public float getShade(Direction direction, boolean bool) {
		return Minecraft.getInstance().level.getShade(direction, bool);
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return lightEngine;
	}

	@Override
	public int getBlockTint(BlockPos pos, ColorResolver resolver) {
		Biome plainsBiome = Minecraft.getInstance().getConnection().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getOrThrow(Biomes.PLAINS);
		return resolver.getColor(plainsBiome, pos.getX(), pos.getZ());
	}
}
