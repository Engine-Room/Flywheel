package com.jozufozu.flywheel.core.crumbling;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.backend.instancing.InstanceManager;
import com.jozufozu.flywheel.backend.instancing.SerialTaskEngine;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstanceManager;
import com.jozufozu.flywheel.backend.instancing.instancing.DrawCall;
import com.jozufozu.flywheel.backend.instancing.instancing.InstancingEngine;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.RenderContext;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.mixin.LevelRendererAccessor;
import com.jozufozu.flywheel.util.Lazy;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.block.entity.BlockEntity;

// TODO: merge directly into InstancingEngine for efficiency
/**
 * Responsible for rendering the crumbling overlay for instanced block entities.
 */
public class CrumblingRenderer {

	private static Lazy<State> STATE;

	static {
		_init();
	}

	public static void renderCrumbling(RenderContext context) {
		// TODO: one pass base/crumbling
		if (true) return;

		Int2ObjectMap<List<BlockEntity>> activeStages = getActiveStageBlockEntities(context.renderer(), context.level());
		if (activeStages.isEmpty()) return;

		try (var restoreState = GlStateTracker.getRestoreState()) {
			State state = STATE.get();
			var instanceManager = state.instanceManager;
			var engine = state.instancerManager;

			renderCrumblingInner(activeStages, instanceManager, engine, context);
		}
	}

	private static void renderCrumblingInner(Int2ObjectMap<List<BlockEntity>> activeStages, InstanceManager<BlockEntity> instanceManager, CrumblingEngine engine, RenderContext ctx) {
		for (Int2ObjectMap.Entry<List<BlockEntity>> stage : activeStages.int2ObjectEntrySet()) {
			RenderType currentLayer = ModelBakery.DESTROY_TYPES.get(stage.getIntKey());

			// something about when we call this means that the textures are not ready for use on the first frame they should appear
			if (currentLayer != null) {
				stage.getValue().forEach(instanceManager::add);

				instanceManager.beginFrame(SerialTaskEngine.INSTANCE, ctx);
				engine.beginFrame(SerialTaskEngine.INSTANCE, ctx);

				engine.renderAll(SerialTaskEngine.INSTANCE, ctx);

				instanceManager.invalidate();
			}
		}
	}

	/**
	 * Associate each breaking stage with a list of all block entities at that stage.
	 */
	private static Int2ObjectMap<List<BlockEntity>> getActiveStageBlockEntities(LevelRenderer levelRenderer, ClientLevel level) {
		Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress = ((LevelRendererAccessor) levelRenderer).flywheel$getDestructionProgress();
		if (destructionProgress.isEmpty()) {
			return Int2ObjectMaps.emptyMap();
		}

		Int2ObjectMap<List<BlockEntity>> breakingEntities = new Int2ObjectArrayMap<>();
		BlockPos.MutableBlockPos breakingPos = new BlockPos.MutableBlockPos();

		for (Long2ObjectMap.Entry<SortedSet<BlockDestructionProgress>> entry : destructionProgress.long2ObjectEntrySet()) {
			breakingPos.set(entry.getLongKey());

			SortedSet<BlockDestructionProgress> progresses = entry.getValue();
			if (progresses != null && !progresses.isEmpty()) {
				int progress = progresses.last()
						.getProgress();
				if (progress >= 0) {
					BlockEntity blockEntity = level.getBlockEntity(breakingPos);

					if (blockEntity != null) {
						List<BlockEntity> blockEntities = breakingEntities.computeIfAbsent(progress, $ -> new ArrayList<>());
						blockEntities.add(blockEntity);
					}
				}
			}
		}

		return breakingEntities;
	}

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
		private final CrumblingEngine instancerManager;
		private final InstanceManager<BlockEntity> instanceManager;

		private State() {
			instancerManager = new CrumblingEngine();
			instanceManager = new BlockEntityInstanceManager(instancerManager);
			instancerManager.attachManagers(instanceManager);
		}

		private void kill() {
			instancerManager.delete();
			instanceManager.invalidate();
		}
	}

	private static class CrumblingEngine extends InstancingEngine {
		public CrumblingEngine() {
			super(Components.CRUMBLING);
		}
	}
}
