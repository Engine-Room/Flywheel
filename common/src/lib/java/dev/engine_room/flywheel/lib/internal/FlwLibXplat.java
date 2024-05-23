package com.jozufozu.flywheel.lib.internal;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.internal.DependencyInjection;
import com.jozufozu.flywheel.lib.model.baked.BakedModelBuilder;
import com.jozufozu.flywheel.lib.model.baked.BlockModelBuilder;
import com.jozufozu.flywheel.lib.model.baked.MultiBlockModelBuilder;
import com.jozufozu.flywheel.lib.util.ShadersModHandler;

import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface FlwLibXplat {
	FlwLibXplat INSTANCE = DependencyInjection.load(FlwLibXplat.class, "com.jozufozu.flywheel.impl.FlwLibXplatImpl");

	BlockRenderDispatcher createVanillaBlockRenderDispatcher();

	BakedModelBuilder createBakedModelBuilder(BakedModel bakedModel);

	BlockModelBuilder createBlockModelBuilder(BlockState state);

	MultiBlockModelBuilder createMultiBlockModelBuilder(BlockAndTintGetter level, Iterable<BlockPos> positions);

	@Nullable
	ShadersModHandler.InternalHandler createIrisHandler();
}
