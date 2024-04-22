package com.jozufozu.flywheel.platform;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.event.BeginFrameCallback;
import com.jozufozu.flywheel.api.event.ReloadLevelRendererCallback;
import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.event.RenderStageCallback;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.lib.model.baked.BakedModelBuilder;
import com.jozufozu.flywheel.lib.model.baked.BlockModelBuilder;
import com.jozufozu.flywheel.lib.model.baked.MultiBlockModelBuilder;
import com.jozufozu.flywheel.lib.util.ShadersModHandler;

import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public class ClientPlatformImpl implements ClientPlatform {
	@Override
	public void dispatchReloadLevelRenderer(ClientLevel level) {
		ReloadLevelRendererCallback.EVENT.invoker().onReloadLevelRenderer(level);
	}

	@Override
	public void dispatchBeginFrame(RenderContext context) {
		BeginFrameCallback.EVENT.invoker().onBeginFrame(context);
	}

	@Override
	public void dispatchRenderStage(RenderContext context, RenderStage stage) {
		RenderStageCallback.EVENT.invoker().onRenderStage(context, stage);
	}

	@Override
	public boolean isModLoaded(String modid) {
		return FabricLoader.getInstance()
				.isModLoaded(modid);
	}

	@Nullable
	@Override
	public ShadersModHandler.InternalHandler createIrisOculusHandlerIfPresent() {
		if (isModLoaded("iris")) {
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
		} else {
			return null;
		}
	}

	@Override
	public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
		return state.getLightEmission();
	}

	@Override
	public FlwConfig getConfigInstance() {
		// TODO: fabric config
		return null;
	}

	@Override
	public BlockRenderDispatcher createVanillaRenderer() {
		return Minecraft.getInstance().getBlockRenderer();
	}

	@Override
	public BakedModelBuilder bakedModelBuilder(BakedModel bakedModel) {
		return null;
	}

	@Override
	public BlockModelBuilder blockModelBuilder(BlockState state) {
		return null;
	}

	@Override
	public MultiBlockModelBuilder multiBlockModelBuilder(BlockAndTintGetter level, Iterable<BlockPos> positions) {
		return null;
	}
}
