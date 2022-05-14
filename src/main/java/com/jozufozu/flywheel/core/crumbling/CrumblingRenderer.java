package com.jozufozu.flywheel.core.crumbling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.struct.Instanced;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlTextureUnit;
import com.jozufozu.flywheel.backend.instancing.InstanceManager;
import com.jozufozu.flywheel.backend.instancing.SerialTaskEngine;
import com.jozufozu.flywheel.backend.instancing.instancing.InstancedMaterial;
import com.jozufozu.flywheel.backend.instancing.instancing.InstancingEngine;
import com.jozufozu.flywheel.core.Contexts;
import com.jozufozu.flywheel.core.RenderContext;
import com.jozufozu.flywheel.core.CoreShaderInfoMap.CoreShaderInfo;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.mixin.LevelRendererAccessor;
import com.jozufozu.flywheel.util.Lazy;
import com.jozufozu.flywheel.util.Textures;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Responsible for rendering the block breaking overlay for instanced block entities.
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public class CrumblingRenderer {

	static RenderType _currentLayer;

	private static Lazy<State> STATE;

	static {
		_init();
	}

	public static void renderBreaking(ClientLevel level, PoseStack stack, double x, double y, double z, Matrix4f viewProjection) {

		if (!Backend.canUseInstancing(level)) return;

		Int2ObjectMap<List<BlockEntity>> activeStages = getActiveStageBlockEntities(level);

		if (activeStages.isEmpty()) return;

		State state = STATE.get();
		var instanceManager = state.instanceManager;
		var engine = state.materialManager;

		TextureManager textureManager = Minecraft.getInstance().getTextureManager();
		Camera info = Minecraft.getInstance().gameRenderer.getMainCamera();

		for (Int2ObjectMap.Entry<List<BlockEntity>> stage : activeStages.int2ObjectEntrySet()) {
			_currentLayer = ModelBakery.DESTROY_TYPES.get(stage.getIntKey());

			// something about when we call this means that the textures are not ready for use on the first frame they should appear
			if (_currentLayer != null) {
				stage.getValue().forEach(instanceManager::add);

				instanceManager.beginFrame(SerialTaskEngine.INSTANCE, info);
				engine.beginFrame(info);

				var ctx = new RenderContext(level, stack, viewProjection, null, x, y, z);

				engine.renderAllRemaining(SerialTaskEngine.INSTANCE, ctx);

				instanceManager.invalidate();
			}

		}

		GlTextureUnit.T0.makeActive();
		AbstractTexture breaking = textureManager.getTexture(ModelBakery.BREAKING_LOCATIONS.get(0));
		if (breaking != null) RenderSystem.bindTexture(breaking.getId());
	}

	/**
	 * Associate each breaking stage with a list of all block entities at that stage.
	 */
	private static Int2ObjectMap<List<BlockEntity>> getActiveStageBlockEntities(ClientLevel world) {

		Int2ObjectMap<List<BlockEntity>> breakingEntities = new Int2ObjectArrayMap<>();

		for (Long2ObjectMap.Entry<SortedSet<BlockDestructionProgress>> entry : ((LevelRendererAccessor) Minecraft.getInstance().levelRenderer).flywheel$getDestructionProgress()
				.long2ObjectEntrySet()) {
			BlockPos breakingPos = BlockPos.of(entry.getLongKey());

			SortedSet<BlockDestructionProgress> progresses = entry.getValue();
			if (progresses != null && !progresses.isEmpty()) {
				int blockDamage = progresses.last()
						.getProgress();

				BlockEntity blockEntity = world.getBlockEntity(breakingPos);

				if (blockEntity != null) {
					List<BlockEntity> blockEntities = breakingEntities.computeIfAbsent(blockDamage, $ -> new ArrayList<>());
					blockEntities.add(blockEntity);
				}
			}
		}

		return breakingEntities;
	}

	@SubscribeEvent
	public static void onReloadRenderers(ReloadRenderersEvent event) {
		ClientLevel world = event.getWorld();
        if (Backend.isOn() && world != null) {
			reset();
		}
	}

	public static void reset() {
		STATE.ifPresent(State::kill);
		_init();
	}

	private static void _init() {
		STATE = Lazy.of(State::new);
	}

	private static class State {
		private final CrumblingEngine materialManager;
		private final InstanceManager<BlockEntity> instanceManager;

		private State() {
			materialManager = new CrumblingEngine();
			instanceManager = new CrumblingInstanceManager(materialManager);
			materialManager.addListener(instanceManager);
		}

		private void kill() {
			materialManager.delete();
			instanceManager.invalidate();
		}
	}

	private static class CrumblingEngine extends InstancingEngine<CrumblingProgram> {

		public CrumblingEngine() {
			super(Contexts.CRUMBLING);
		}

		@Override
		protected void render(RenderType type, double camX, double camY, double camZ, Matrix4f viewProjection, ClientLevel level) {
			type.setupRenderState();

			int renderTex = RenderSystem.getShaderTexture(0);

			AtlasInfo.SheetSize sheetSize = AtlasInfo.getSheetSize(Textures.getShaderTexture(0));

			int width;
			int height;
			if (sheetSize != null) {
				width = sheetSize.width();
				height = sheetSize.height();
			} else {
				width = height = 256;
			}

			type.clearRenderState();

			CrumblingRenderer._currentLayer.setupRenderState();

			int breakingTex = RenderSystem.getShaderTexture(0);

			RenderSystem.setShaderTexture(0, renderTex);
			RenderSystem.setShaderTexture(4, breakingTex);

			Textures.bindActiveTextures();
			CoreShaderInfo coreShaderInfo = getCoreShaderInfo();

			for (Map.Entry<Instanced<? extends InstanceData>, InstancedMaterial<?>> entry : materials.entrySet()) {
				CrumblingProgram program = setup(entry.getKey().getProgramSpec(), coreShaderInfo, camX, camY, camZ, viewProjection, level);

				program.setAtlasSize(width, height);

				//entry.getValue().getAllRenderables().forEach(Renderable::draw);
			}

			CrumblingRenderer._currentLayer.clearRenderState();
		}
	}
}
