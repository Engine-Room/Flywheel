package dev.engine_room.flywheel.impl;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.lib.internal.FlwLibXplat;
import dev.engine_room.flywheel.lib.model.baked.BakedModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.BlockModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.FabricBakedModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.FabricBlockModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.FabricItemModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.FabricMultiBlockModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.ItemModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.MultiBlockModelBuilder;
import dev.engine_room.flywheel.lib.util.ShadersModHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class FlwLibXplatImpl implements FlwLibXplat {
	@Override
	public BlockRenderDispatcher createVanillaBlockRenderDispatcher() {
		return Minecraft.getInstance().getBlockRenderer();
	}

	@Override
	public BakedModelBuilder createBakedModelBuilder(BakedModel bakedModel) {
		return new FabricBakedModelBuilder(bakedModel);
	}

	@Override
	public BlockModelBuilder createBlockModelBuilder(BlockState state) {
		return new FabricBlockModelBuilder(state);
	}

	@Override
	public MultiBlockModelBuilder createMultiBlockModelBuilder(BlockAndTintGetter level, Iterable<BlockPos> positions) {
		return new FabricMultiBlockModelBuilder(level, positions);
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

	@Override
	public ItemModelBuilder createItemModelBuilder(ItemStack stack, BakedModel model) {
		return new FabricItemModelBuilder(stack, model);
	}
}
