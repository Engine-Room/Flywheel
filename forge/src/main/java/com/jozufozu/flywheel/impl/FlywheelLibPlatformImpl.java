package com.jozufozu.flywheel.impl;

import java.lang.reflect.Field;

import org.slf4j.Logger;

import com.jozufozu.flywheel.impl.extension.PoseStackExtension;
import com.jozufozu.flywheel.lib.internal.FlywheelLibPlatform;
import com.jozufozu.flywheel.lib.model.baked.BakedModelBuilder;
import com.jozufozu.flywheel.lib.model.baked.BlockModelBuilder;
import com.jozufozu.flywheel.lib.model.baked.ForgeBakedModelBuilder;
import com.jozufozu.flywheel.lib.model.baked.ForgeBlockModelBuilder;
import com.jozufozu.flywheel.lib.model.baked.ForgeMultiBlockModelBuilder;
import com.jozufozu.flywheel.lib.model.baked.MultiBlockModelBuilder;
import com.jozufozu.flywheel.lib.transform.PoseTransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class FlywheelLibPlatformImpl implements FlywheelLibPlatform {
	private static final Logger LOGGER = LogUtils.getLogger();

	@Override
	public PoseTransformStack getPoseTransformStackOf(PoseStack stack) {
		return ((PoseStackExtension) stack).flywheel$transformStack();
	}

	@Override
	public BlockRenderDispatcher createVanillaRenderer() {
		BlockRenderDispatcher defaultDispatcher = Minecraft.getInstance().getBlockRenderer();
		BlockRenderDispatcher dispatcher = new BlockRenderDispatcher(null, null, null);
		try {
			for (Field field : BlockRenderDispatcher.class.getDeclaredFields()) {
				field.setAccessible(true);
				field.set(dispatcher, field.get(defaultDispatcher));
			}
			ObfuscationReflectionHelper.setPrivateValue(BlockRenderDispatcher.class, dispatcher, new ModelBlockRenderer(Minecraft.getInstance().getBlockColors()), "f_110900_");
		} catch (Exception e) {
			LOGGER.error("Failed to initialize vanilla BlockRenderDispatcher!", e);
			return defaultDispatcher;
		}
		return dispatcher;
	}

	@Override
	public BakedModelBuilder bakedModelBuilder(BakedModel bakedModel) {
		return new ForgeBakedModelBuilder(bakedModel);
	}

	@Override
	public BlockModelBuilder blockModelBuilder(BlockState state) {
		return new ForgeBlockModelBuilder(state);
	}

	@Override
	public MultiBlockModelBuilder multiBlockModelBuilder(BlockAndTintGetter level, Iterable<BlockPos> positions) {
		return new ForgeMultiBlockModelBuilder(level, positions);
	}
}
