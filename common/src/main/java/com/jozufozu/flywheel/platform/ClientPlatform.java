package com.jozufozu.flywheel.platform;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.internal.DependencyInjection;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.lib.model.baked.BakedModelBuilder;
import com.jozufozu.flywheel.lib.model.baked.BlockModelBuilder;
import com.jozufozu.flywheel.lib.model.baked.MultiBlockModelBuilder;
import com.jozufozu.flywheel.lib.util.ShadersModHandler;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface ClientPlatform {
	ClientPlatform INSTANCE = DependencyInjection.load(ClientPlatform.class, "com.jozufozu.flywheel.platform.ClientPlatformImpl");

	void dispatchReloadLevelRenderer(ClientLevel level);

	void dispatchBeginFrame(RenderContext context);

	void dispatchRenderStage(RenderContext context, RenderStage stage);

	boolean isModLoaded(String modid);

	@Nullable
	ShadersModHandler.InternalHandler createIrisOculusHandlerIfPresent();

	int getLightEmission(BlockState state, BlockGetter level, BlockPos pos);

	FlwConfig getConfigInstance();

	BlockRenderDispatcher createVanillaRenderer();

	BakedModelBuilder bakedModelBuilder(BakedModel bakedModel);

	BlockModelBuilder blockModelBuilder(BlockState state);

	MultiBlockModelBuilder multiBlockModelBuilder(BlockAndTintGetter level, Iterable<BlockPos> positions);
}
