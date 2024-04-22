package com.jozufozu.flywheel.platform;

import java.lang.reflect.Field;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.jozufozu.flywheel.api.event.BeginFrameEvent;
import com.jozufozu.flywheel.api.event.ReloadLevelRendererEvent;
import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.event.RenderStageEvent;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.config.FlwForgeConfig;
import com.jozufozu.flywheel.lib.model.baked.BakedModelBuilder;
import com.jozufozu.flywheel.lib.model.baked.BlockModelBuilder;
import com.jozufozu.flywheel.lib.model.baked.ForgeBakedModelBuilder;
import com.jozufozu.flywheel.lib.model.baked.ForgeBlockModelBuilder;
import com.jozufozu.flywheel.lib.model.baked.ForgeMultiBlockModelBuilder;
import com.jozufozu.flywheel.lib.model.baked.MultiBlockModelBuilder;
import com.jozufozu.flywheel.lib.util.ShadersModHandler;
import com.mojang.logging.LogUtils;

import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class ClientPlatformImpl implements ClientPlatform {
	private static final Logger LOGGER = LogUtils.getLogger();

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

	@Override
	@Nullable
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
	public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
		return state.getLightEmission(level, pos);
	}

	@Override
	public FlwConfig getConfigInstance() {
		return FlwForgeConfig.INSTANCE;
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
