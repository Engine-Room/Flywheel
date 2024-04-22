package com.jozufozu.flywheel.impl;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.lib.internal.FlwLibXplat;
import com.jozufozu.flywheel.lib.model.baked.BakedModelBuilder;
import com.jozufozu.flywheel.lib.model.baked.BlockModelBuilder;
import com.jozufozu.flywheel.lib.model.baked.MultiBlockModelBuilder;
import com.jozufozu.flywheel.lib.util.ShadersModHandler;

import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class FlwLibXplatImpl implements FlwLibXplat {
	@Override
	public BlockRenderDispatcher createVanillaBlockRenderDispatcher() {
		return Minecraft.getInstance().getBlockRenderer();
	}

	@Override
	public BakedModelBuilder createBakedModelBuilder(BakedModel bakedModel) {
		return null;
	}

	@Override
	public BlockModelBuilder createBlockModelBuilder(BlockState state) {
		return null;
	}

	@Override
	public MultiBlockModelBuilder createMultiBlockModelBuilder(BlockAndTintGetter level, Iterable<BlockPos> positions) {
		return null;
	}

	@Override
	@Nullable
	public ShadersModHandler.InternalHandler createIrisHandler() {
		if (!FabricLoader.getInstance()
				.isModLoaded("iris")) {
			return null;
		}

		return new ShadersModHandler.InternalHandler() {
			@Override
			public boolean isShaderPackInUse() {
				return IrisApi.getInstance()
						.isShaderPackInUse();
			}

			@Override
			public boolean isRenderingShadowPass() {
				return IrisApi.getInstance()
						.isRenderingShadowPass();
			}
		};
	}
}
