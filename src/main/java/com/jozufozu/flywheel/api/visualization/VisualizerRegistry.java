package com.jozufozu.flywheel.api.visualization;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.impl.VisualizerRegistryImpl;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * A utility class for registering and retrieving {@code Visualizer}s.
 */
public final class VisualizerRegistry {
	/**
	 * Gets the visualizer for the given block entity type, if one exists.
	 * @param type The block entity type to get the visualizer for.
	 * @param <T> The type of the block entity.
	 * @return The visualizer for the given block entity type, or {@code null} if none exists.
	 */
	@Nullable
	public static <T extends BlockEntity> BlockEntityVisualizer<? super T> getVisualizer(BlockEntityType<T> type) {
		return VisualizerRegistryImpl.getVisualizer(type);
	}

	/**
	 * Gets the visualizer for the given entity type, if one exists.
	 * @param type The entity type to get the visualizer for.
	 * @param <T> The type of the entity.
	 * @return The visualizer for the given entity type, or {@code null} if none exists.
	 */
	@Nullable
	public static <T extends Entity> EntityVisualizer<? super T> getVisualizer(EntityType<T> type) {
		return VisualizerRegistryImpl.getVisualizer(type);
	}

	/**
	 * Sets the visualizer for the given block entity type.
	 * @param type The block entity type to set the visualizer for.
	 * @param visualizer The visualizer to set.
	 * @param <T> The type of the block entity.
	 */
	public static <T extends BlockEntity> void setVisualizer(BlockEntityType<T> type, BlockEntityVisualizer<? super T> visualizer) {
		VisualizerRegistryImpl.setVisualizer(type, visualizer);
	}

	/**
	 * Sets the visualizer for the given entity type.
	 * @param type The entity type to set the visualizer for.
	 * @param visualizer The visualizer to set.
	 * @param <T> The type of the entity.
	 */
	public static <T extends Entity> void setVisualizer(EntityType<T> type, EntityVisualizer<? super T> visualizer) {
		VisualizerRegistryImpl.setVisualizer(type, visualizer);
	}

	private VisualizerRegistry() {
	}
}
