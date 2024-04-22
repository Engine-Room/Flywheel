package com.jozufozu.flywheel.api.visualization;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.internal.FlwApiLink;
import com.jozufozu.flywheel.api.visual.Effect;
import com.jozufozu.flywheel.api.visual.Visual;

import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

@ApiStatus.NonExtendable
public interface VisualizationManager {
	static boolean supportsVisualization(@Nullable LevelAccessor level) {
		return FlwApiLink.INSTANCE.supportsVisualization(level);
	}

	@Nullable
	static VisualizationManager get(@Nullable LevelAccessor level) {
		return FlwApiLink.INSTANCE.getVisualizationManager(level);
	}

	static VisualizationManager getOrThrow(@Nullable LevelAccessor level) {
		return FlwApiLink.INSTANCE.getVisualizationManagerOrThrow(level);
	}

	/**
	 * Call this when you want to run {@link Visual#update}.
	 * @param blockEntity The block entity whose visual you want to update.
	 */
	static void queueUpdate(BlockEntity blockEntity) {
		Level level = blockEntity.getLevel();
		VisualizationManager manager = get(level);
		if (manager == null) {
			return;
		}

		manager.getBlockEntities().queueUpdate(blockEntity);
	}

	/**
	 * Call this when you want to run {@link Visual#update}.
	 * @param entity The entity whose visual you want to update.
	 */
	static void queueUpdate(Entity entity) {
		Level level = entity.level();
		VisualizationManager manager = get(level);
		if (manager == null) {
			return;
		}

		manager.getEntities().queueUpdate(entity);
	}

	/**
	 * Call this when you want to run {@link Visual#update}.
	 * @param effect The effect whose visual you want to update.
	 */
	static void queueUpdate(LevelAccessor level, Effect effect) {
		VisualizationManager manager = get(level);
		if (manager == null) {
			return;
		}

		manager.getEffects().queueUpdate(effect);
	}

	Vec3i getRenderOrigin();

	VisualManager<BlockEntity> getBlockEntities();

	VisualManager<Entity> getEntities();

	VisualManager<Effect> getEffects();
}
