package dev.engine_room.flywheel.impl.visualization;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.visualization.BlockEntityVisualizer;
import dev.engine_room.flywheel.api.visualization.EntityVisualizer;
import dev.engine_room.flywheel.impl.extension.BlockEntityTypeExtension;
import dev.engine_room.flywheel.impl.extension.EntityTypeExtension;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

// TODO: Add freezing
@SuppressWarnings("unchecked")
public final class VisualizerRegistryImpl {
	@Nullable
	public static <T extends BlockEntity> BlockEntityVisualizer<? super T> getVisualizer(BlockEntityType<T> type) {
		return ((BlockEntityTypeExtension<T>) type).flywheel$getVisualizer();
	}

	@Nullable
	public static <T extends Entity> EntityVisualizer<? super T> getVisualizer(EntityType<T> type) {
		return ((EntityTypeExtension<T>) type).flywheel$getVisualizer();
	}

	public static <T extends BlockEntity> void setVisualizer(BlockEntityType<T> type, BlockEntityVisualizer<? super T> visualizer) {
		((BlockEntityTypeExtension<T>) type).flywheel$setVisualizer(visualizer);
	}

	public static <T extends Entity> void setVisualizer(EntityType<T> type, EntityVisualizer<? super T> visualizer) {
		((EntityTypeExtension<T>) type).flywheel$setVisualizer(visualizer);
	}

	private VisualizerRegistryImpl() {
	}
}
