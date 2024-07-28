package dev.engine_room.flywheel.lib.visualization;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.visual.Effect;
import dev.engine_room.flywheel.api.visual.Visual;
import dev.engine_room.flywheel.api.visualization.BlockEntityVisualizer;
import dev.engine_room.flywheel.api.visualization.EntityVisualizer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.api.visualization.VisualizerRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class VisualizationHelper {
	private VisualizationHelper() {
	}

	public static void queueAdd(Effect effect) {
		VisualizationManager manager = VisualizationManager.get(effect.level());
		if (manager == null) {
			return;
		}

		manager.effects().queueAdd(effect);
	}

	public static void queueRemove(Effect effect) {
		VisualizationManager manager = VisualizationManager.get(effect.level());
		if (manager == null) {
			return;
		}

		manager.effects().queueRemove(effect);
	}

	/**
	 * Call this when you want to run {@link Visual#update}.
	 * @param blockEntity The block entity whose visual you want to update.
	 */
	public static void queueUpdate(BlockEntity blockEntity) {
		Level level = blockEntity.getLevel();
		VisualizationManager manager = VisualizationManager.get(level);
		if (manager == null) {
			return;
		}

		manager.blockEntities().queueUpdate(blockEntity);
	}

	/**
	 * Call this when you want to run {@link Visual#update}.
	 * @param entity The entity whose visual you want to update.
	 */
	public static void queueUpdate(Entity entity) {
		Level level = entity.level();
		VisualizationManager manager = VisualizationManager.get(level);
		if (manager == null) {
			return;
		}

		manager.entities().queueUpdate(entity);
	}

	/**
	 * Call this when you want to run {@link Visual#update}.
	 * @param effect The effect whose visual you want to update.
	 */
	public static void queueUpdate(Effect effect) {
		VisualizationManager manager = VisualizationManager.get(effect.level());
		if (manager == null) {
			return;
		}

		manager.effects().queueUpdate(effect);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public static <T extends BlockEntity> BlockEntityVisualizer<? super T> getVisualizer(T blockEntity) {
		return VisualizerRegistry.getVisualizer((BlockEntityType<? super T>) blockEntity.getType());
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public static <T extends Entity> EntityVisualizer<? super T> getVisualizer(T entity) {
		return VisualizerRegistry.getVisualizer((EntityType<? super T>) entity.getType());
	}

	/**
	 * Checks if the given block entity can be visualized.
	 * @param blockEntity The block entity to check.
	 * @param <T> The type of the block entity.
	 * @return {@code true} if the block entity can be visualized.
	 */
	public static <T extends BlockEntity> boolean canVisualize(T blockEntity) {
		return getVisualizer(blockEntity) != null;
	}

	/**
	 * Checks if the given entity can be visualized.
	 * @param entity The entity to check.
	 * @param <T> The type of the entity.
	 * @return {@code true} if the entity can be visualized.
	 */
	public static <T extends Entity> boolean canVisualize(T entity) {
		return getVisualizer(entity) != null;
	}

	/**
	 * Checks if the given block entity is visualized and should not be rendered normally.
	 * @param blockEntity The block entity to check.
	 * @param <T> The type of the block entity.
	 * @return {@code true} if the block entity is visualized and should not be rendered normally.
	 */
	public static <T extends BlockEntity> boolean skipVanillaRender(T blockEntity) {
		BlockEntityVisualizer<? super T> visualizer = getVisualizer(blockEntity);
		if (visualizer == null) {
			return false;
		}
		return visualizer.skipVanillaRender(blockEntity);
	}

	/**
	 * Checks if the given entity is visualized and should not be rendered normally.
	 * @param entity The entity to check.
	 * @param <T> The type of the entity.
	 * @return {@code true} if the entity is visualized and should not be rendered normally.
	 */
	public static <T extends Entity> boolean skipVanillaRender(T entity) {
		EntityVisualizer<? super T> visualizer = getVisualizer(entity);
		if (visualizer == null) {
			return false;
		}
		return visualizer.skipVanillaRender(entity);
	}

	public static <T extends BlockEntity> boolean tryAddBlockEntity(T blockEntity) {
		Level level = blockEntity.getLevel();
		VisualizationManager manager = VisualizationManager.get(level);
		if (manager == null) {
			return false;
		}

		BlockEntityVisualizer<? super T> visualizer = getVisualizer(blockEntity);
		if (visualizer == null) {
			return false;
		}

		manager.blockEntities().queueAdd(blockEntity);
		return visualizer.skipVanillaRender(blockEntity);
	}
}
