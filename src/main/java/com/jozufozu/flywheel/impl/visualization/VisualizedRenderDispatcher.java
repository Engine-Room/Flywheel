package com.jozufozu.flywheel.impl.visualization;

import java.util.List;

import com.jozufozu.flywheel.api.event.BeginFrameEvent;
import com.jozufozu.flywheel.api.event.RenderStageEvent;
import com.jozufozu.flywheel.api.visual.Effect;
import com.jozufozu.flywheel.api.visual.Visual;
import com.jozufozu.flywheel.api.visualization.BlockEntityVisualizer;
import com.jozufozu.flywheel.extension.ClientLevelExtension;
import com.jozufozu.flywheel.impl.visualization.manager.VisualManager;
import com.jozufozu.flywheel.lib.util.AnimationTickHolder;
import com.jozufozu.flywheel.util.FlwUtil;
import com.jozufozu.flywheel.util.WorldAttached;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;

public class VisualizedRenderDispatcher {
	private static final WorldAttached<VisualWorld> VISUAL_WORLDS = new WorldAttached<>(VisualWorld::new);

	/**
	 * Call this when you want to run {@link Visual#update()}.
	 * @param blockEntity The block entity whose visual you want to update.
	 */
	public static void queueUpdate(BlockEntity blockEntity) {
		if (!(blockEntity.getLevel() instanceof ClientLevel level)) {
			return;
		}

		if (!FlwUtil.canUseVisualization(level)) {
			return;
		}

		VISUAL_WORLDS.get(level)
				.getBlockEntities()
				.queueUpdate(blockEntity);
	}

	/**
	 * Call this when you want to run {@link Visual#update()}.
	 * @param entity The entity whose visual you want to update.
	 */
	public static void queueUpdate(Entity entity) {
		Level level = entity.level;
		if (!FlwUtil.canUseVisualization(level)) {
			return;
		}

		VISUAL_WORLDS.get(level)
				.getEntities()
				.queueUpdate(entity);
	}

	/**
	 * Call this when you want to run {@link Visual#update()}.
	 * @param effect The effect whose visual you want to update.
	 */
	public static void queueUpdate(LevelAccessor level, Effect effect) {
		if (!FlwUtil.canUseVisualization(level)) {
			return;
		}

		VISUAL_WORLDS.get(level)
				.getEffects()
				.queueUpdate(effect);
	}

	/**
	 * Get or create the {@link VisualWorld} for the given world.
	 * @throws IllegalStateException if the backend is off
	 */
	private static VisualWorld getVisualWorld(LevelAccessor level) {
		if (!FlwUtil.canUseVisualization(level)) {
			throw new IllegalStateException("Cannot retrieve visual world when backend is off!");
		}
		return VISUAL_WORLDS.get(level);
	}

	public static VisualManager<BlockEntity> getBlockEntities(LevelAccessor level) {
		return getVisualWorld(level).getBlockEntities();
	}

	public static VisualManager<Entity> getEntities(LevelAccessor level) {
		return getVisualWorld(level).getEntities();
	}

	public static VisualManager<Effect> getEffects(LevelAccessor level) {
		return getVisualWorld(level).getEffects();
	}

	public static Vec3i getRenderOrigin(LevelAccessor level) {
		return getVisualWorld(level).getEngine().renderOrigin();
	}

	public static void tick(TickEvent.ClientTickEvent event) {
		if (!FlwUtil.isGameActive() || event.phase == TickEvent.Phase.START) {
			return;
		}

		AnimationTickHolder.tick();

		Minecraft mc = Minecraft.getInstance();
		if (mc.isPaused()) {
			return;
		}

		Entity cameraEntity = mc.getCameraEntity() == null ? mc.player : mc.getCameraEntity();
		if (cameraEntity == null) {
			return;
		}

		Level level = cameraEntity.level;
		if (!FlwUtil.canUseVisualization(level)) {
			return;
		}

		double cameraX = cameraEntity.getX();
		double cameraY = cameraEntity.getEyeY();
		double cameraZ = cameraEntity.getZ();

		VISUAL_WORLDS.get(level).tick(cameraX, cameraY, cameraZ);
	}

	public static void onBeginFrame(BeginFrameEvent event) {
		if (!FlwUtil.isGameActive()) {
			return;
		}

		ClientLevel level = event.getContext().level();
		if (!FlwUtil.canUseVisualization(level)) {
			return;
		}

		VISUAL_WORLDS.get(level).beginFrame(event.getContext());
	}

	public static void onRenderStage(RenderStageEvent event) {
		ClientLevel level = event.getContext().level();
		if (!FlwUtil.canUseVisualization(level)) {
			return;
		}

		VISUAL_WORLDS.get(level).renderStage(event.getContext(), event.getStage());
	}

	public static void resetVisualWorld(ClientLevel level) {
		VISUAL_WORLDS.remove(level, VisualWorld::delete);

		if (!FlwUtil.canUseVisualization(level)) {
			return;
		}

		VisualWorld world = VISUAL_WORLDS.get(level);
		// Block entities are loaded while chunks are baked.
		// Entities are loaded with the level, so when chunks are reloaded they need to be re-added.
		ClientLevelExtension.getAllLoadedEntities(level)
				.forEach(world.getEntities()::add);
	}

	public static <T extends BlockEntity> boolean tryAddBlockEntity(T blockEntity) {
		Level level = blockEntity.getLevel();
		if (!FlwUtil.canUseVisualization(level)) {
			return false;
		}

		BlockEntityVisualizer<? super T> visualizer = VisualizationHelper.getVisualizer(blockEntity);
		if (visualizer == null) {
			return false;
		}

		getBlockEntities(level).queueAdd(blockEntity);

		return visualizer.shouldSkipRender(blockEntity);
	}

	public static void addDebugInfo(List<String> info) {
		ClientLevel level = Minecraft.getInstance().level;
		if (FlwUtil.canUseVisualization(level)) {
			VISUAL_WORLDS.get(level).addDebugInfo(info);
		} else {
			info.add("Disabled");
		}
	}
}
