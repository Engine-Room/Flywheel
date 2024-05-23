package dev.engine_room.flywheel.lib.internal;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.internal.DependencyInjection;
import dev.engine_room.flywheel.lib.model.baked.BakedModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.BlockModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.MultiBlockModelBuilder;
import dev.engine_room.flywheel.lib.util.ShadersModHandler;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface FlwLibXplat {
	FlwLibXplat INSTANCE = DependencyInjection.load(FlwLibXplat.class, "dev.engine_room.flywheel.impl.FlwLibXplatImpl");

	BlockRenderDispatcher createVanillaBlockRenderDispatcher();

	BakedModelBuilder createBakedModelBuilder(BakedModel bakedModel);

	BlockModelBuilder createBlockModelBuilder(BlockState state);

	MultiBlockModelBuilder createMultiBlockModelBuilder(BlockAndTintGetter level, Iterable<BlockPos> positions);

	@Nullable
	ShadersModHandler.InternalHandler createIrisHandler();
}
