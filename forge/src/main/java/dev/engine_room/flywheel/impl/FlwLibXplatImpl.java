package dev.engine_room.flywheel.impl;

import java.lang.reflect.Field;

import org.jetbrains.annotations.UnknownNullability;

import dev.engine_room.flywheel.lib.internal.FlwLibXplat;
import dev.engine_room.flywheel.lib.model.baked.BakedModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.BlockModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.ForgeBakedModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.ForgeBlockModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.ForgeMultiBlockModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.MultiBlockModelBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class FlwLibXplatImpl implements FlwLibXplat {
	@Override
	@UnknownNullability
	public BakedModel getBakedModel(ModelManager modelManager, ResourceLocation location) {
		return modelManager.getModel(location);
	}

	@Override
	public BlockRenderDispatcher createVanillaBlockRenderDispatcher() {
		BlockRenderDispatcher defaultDispatcher = Minecraft.getInstance().getBlockRenderer();
		BlockRenderDispatcher dispatcher = new BlockRenderDispatcher(null, null, null);
		try {
			for (Field field : BlockRenderDispatcher.class.getDeclaredFields()) {
				field.setAccessible(true);
				field.set(dispatcher, field.get(defaultDispatcher));
			}
			ObfuscationReflectionHelper.setPrivateValue(BlockRenderDispatcher.class, dispatcher, new ModelBlockRenderer(Minecraft.getInstance().getBlockColors()), "f_110900_");
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
}
