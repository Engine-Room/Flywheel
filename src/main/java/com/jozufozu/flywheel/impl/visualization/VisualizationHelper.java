package com.jozufozu.flywheel.impl.visualization;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.visualization.BlockEntityVisualizer;
import com.jozufozu.flywheel.api.visualization.EntityVisualizer;
import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.jozufozu.flywheel.api.visualization.VisualizerRegistry;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class VisualizationHelper {
	private VisualizationHelper() {
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
	 * @param type The block entity to check.
	 * @param <T> The block entity.
	 * @return {@code true} if the block entity can be visualized.
	 */
	public static <T extends BlockEntity> boolean canVisualize(T blockEntity) {
		return getVisualizer(blockEntity) != null;
	}

	/**
	 * Checks if the given entity can be visualized.
	 * @param type The entity to check.
	 * @param <T> The entity.
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
	public static <T extends BlockEntity> boolean shouldSkipRender(T blockEntity) {
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
	public static <T extends Entity> boolean shouldSkipRender(T entity) {
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

		manager.getBlockEntities().queueAdd(blockEntity);

		return visualizer.skipVanillaRender(blockEntity);
	}
}
