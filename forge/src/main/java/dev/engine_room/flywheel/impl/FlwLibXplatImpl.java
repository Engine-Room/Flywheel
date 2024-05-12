package dev.engine_room.flywheel.impl;

import java.lang.reflect.Field;

import net.neoforged.fml.ModList;
import net.neoforged.fml.util.ObfuscationReflectionHelper;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.lib.internal.FlwLibXplat;
import dev.engine_room.flywheel.lib.model.baked.BakedModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.BlockModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.ForgeBakedModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.ForgeBlockModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.ForgeMultiBlockModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.MultiBlockModelBuilder;
import dev.engine_room.flywheel.lib.util.ShadersModHandler;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class FlwLibXplatImpl implements FlwLibXplat {
	@Override
	public BlockRenderDispatcher createVanillaBlockRenderDispatcher() {
		BlockRenderDispatcher defaultDispatcher = Minecraft.getInstance().getBlockRenderer();
		BlockRenderDispatcher dispatcher = new BlockRenderDispatcher(null, null, null);
		try {
			for (Field field : BlockRenderDispatcher.class.getDeclaredFields()) {
				field.setAccessible(true);
				field.set(dispatcher, field.get(defaultDispatcher));
			}
			ObfuscationReflectionHelper.setPrivateValue(BlockRenderDispatcher.class, dispatcher, new ModelBlockRenderer(Minecraft.getInstance().getBlockColors()), "modelRenderer");
		} catch (Exception e) {
			FlwImpl.LOGGER.error("Failed to initialize vanilla BlockRenderDispatcher!", e);
			return defaultDispatcher;
		}
		return dispatcher;
	}

	@Override
	public BakedModelBuilder createBakedModelBuilder(BakedModel bakedModel) {
		return new ForgeBakedModelBuilder(bakedModel);
	}

	@Override
	public BlockModelBuilder createBlockModelBuilder(BlockState state) {
		return new ForgeBlockModelBuilder(state);
	}

	@Override
	public MultiBlockModelBuilder createMultiBlockModelBuilder(BlockAndTintGetter level, Iterable<BlockPos> positions) {
		return new ForgeMultiBlockModelBuilder(level, positions);
	}

	@Override
	@Nullable
	public ShadersModHandler.InternalHandler createIrisHandler() {
		if (!ModList.get()
				.isLoaded("oculus")) {
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
