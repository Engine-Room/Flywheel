package dev.engine_room.flywheel.impl;

import org.jetbrains.annotations.UnknownNullability;

import dev.engine_room.flywheel.lib.internal.FlwLibXplat;
import dev.engine_room.flywheel.lib.model.baked.BakedModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.BlockModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.FabricBakedModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.FabricBlockModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.FabricMultiBlockModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.MultiBlockModelBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class FlwLibXplatImpl implements FlwLibXplat {
	@Override
	@UnknownNullability
	public BakedModel getBakedModel(ModelManager modelManager, ResourceLocation location) {
		return modelManager.getModel(location);
	}

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
}
