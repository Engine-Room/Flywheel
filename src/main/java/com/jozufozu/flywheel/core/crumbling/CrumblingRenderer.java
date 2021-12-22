package com.jozufozu.flywheel.core.crumbling;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlTextureUnit;
import com.jozufozu.flywheel.backend.instancing.InstanceManager;
import com.jozufozu.flywheel.backend.instancing.instancing.InstancingEngine;
import com.jozufozu.flywheel.core.Contexts;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.jozufozu.flywheel.mixin.LevelRendererAccessor;
import com.jozufozu.flywheel.util.Lazy;
import com.jozufozu.flywheel.util.Pair;
import com.mojang.blaze3d.systems.RenderSystem;

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
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Responsible for rendering the block breaking overlay for instanced tiles.
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(Dist.CLIENT)
public class CrumblingRenderer {

	static RenderType _currentLayer;

	private static final Lazy<State> STATE;
	private static final Lazy.KillSwitch<State> INVALIDATOR;

	static {
		Pair<Lazy<State>, Lazy.KillSwitch<State>> state = Lazy.ofKillable(State::new, State::kill);

        STATE = state.first();
		INVALIDATOR = state.second();
	}

	public static void renderBreaking(RenderLayerEvent event) {
		if (!Backend.getInstance()
				.canUseInstancing(event.getWorld())) return;

		Int2ObjectMap<List<BlockEntity>> activeStages = getActiveStageTiles(event.getWorld());

		if (activeStages.isEmpty()) return;

		State state = STATE.get();
		InstanceManager<BlockEntity> instanceManager = state.instanceManager;
		InstancingEngine<CrumblingProgram> materials = state.materialManager;

		TextureManager textureManager = Minecraft.getInstance().getTextureManager();
		Camera info = Minecraft.getInstance().gameRenderer.getMainCamera();

		for (Int2ObjectMap.Entry<List<BlockEntity>> stage : activeStages.int2ObjectEntrySet()) {
			_currentLayer = ModelBakery.DESTROY_TYPES.get(stage.getIntKey());

			// something about when we call this means that the textures are not ready for use on the first frame they should appear
			if (_currentLayer != null) {
				stage.getValue().forEach(instanceManager::add);

				instanceManager.beginFrame(info);

				materials.render(event);

				instanceManager.invalidate();
			}

		}

		GlTextureUnit.T0.makeActive();
		AbstractTexture breaking = textureManager.getTexture(ModelBakery.BREAKING_LOCATIONS.get(0));
		if (breaking != null) RenderSystem.bindTexture(breaking.getId());
	}

	/**
	 * Associate each breaking stage with a list of all tile entities at that stage.
	 */
	private static Int2ObjectMap<List<BlockEntity>> getActiveStageTiles(ClientLevel world) {

		Int2ObjectMap<List<BlockEntity>> breakingEntities = new Int2ObjectArrayMap<>();

		for (Long2ObjectMap.Entry<SortedSet<BlockDestructionProgress>> entry : ((LevelRendererAccessor) Minecraft.getInstance().levelRenderer).getDestructionProgress()
				.long2ObjectEntrySet()) {
			BlockPos breakingPos = BlockPos.of(entry.getLongKey());

			SortedSet<BlockDestructionProgress> progresses = entry.getValue();
			if (progresses != null && !progresses.isEmpty()) {
				int blockDamage = progresses.last()
						.getProgress();

				BlockEntity tileEntity = world.getBlockEntity(breakingPos);

				if (tileEntity != null) {
					List<BlockEntity> tileEntities = breakingEntities.computeIfAbsent(blockDamage, $ -> new ArrayList<>());
					tileEntities.add(tileEntity);
				}
			}
		}

		return breakingEntities;
	}

	@SubscribeEvent
	public static void onReloadRenderers(ReloadRenderersEvent event) {
		ClientLevel world = event.getWorld();
		if (Backend.getInstance()
				.canUseInstancing() && world != null) {
			reset();
		}
	}

	public static void reset() {
		INVALIDATOR.killValue();
	}

	private static class State {
		private final InstancingEngine<CrumblingProgram> materialManager;
		private final InstanceManager<BlockEntity> instanceManager;

		private State() {
			materialManager = InstancingEngine.builder(Contexts.CRUMBLING)
					.setGroupFactory(CrumblingGroup::new)
					.build();
			instanceManager = new CrumblingInstanceManager(materialManager);
			materialManager.addListener(instanceManager);
		}

		private void kill() {
			materialManager.delete();
			instanceManager.invalidate();
		}
	}
}
