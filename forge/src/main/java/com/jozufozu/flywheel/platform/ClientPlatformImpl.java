package com.jozufozu.flywheel.platform;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.event.BeginFrameEvent;
import com.jozufozu.flywheel.api.event.ReloadLevelRendererEvent;
import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.event.RenderStageEvent;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.config.FlwForgeConfig;
import com.jozufozu.flywheel.lib.util.ShadersModHandler;

import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;

public class ClientPlatformImpl extends ClientPlatform {
	@Override
	public void dispatchReloadLevelRenderer(ClientLevel level) {
		MinecraftForge.EVENT_BUS.post(new ReloadLevelRendererEvent(level));
	}

	@Override
	public void dispatchBeginFrame(RenderContext context) {
		MinecraftForge.EVENT_BUS.post(new BeginFrameEvent(context));
	}

	@Override
	public void dispatchRenderStage(RenderContext context, RenderStage stage) {
		MinecraftForge.EVENT_BUS.post(new RenderStageEvent(context, stage));
	}

	@Override
	public boolean isModLoaded(String modid) {
		return ModList.get()
				.isLoaded(modid);
	}

	@Nullable
	@Override
	public ShadersModHandler.InternalHandler createIrisOculusHandlerIfPresent() {
		if (isModLoaded("oculus")) {
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
	public int getLightEmission(BlockState state, ClientLevel level, BlockPos pos) {
		return state.getLightEmission(level, pos);
	}

	@Override
	public FlwConfig getConfigInstance() {
		return FlwForgeConfig.INSTANCE;
	}
}
