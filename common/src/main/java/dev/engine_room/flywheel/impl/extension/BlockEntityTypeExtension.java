package dev.engine_room.flywheel.impl.extension;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.visualization.BlockEntityVisualizer;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface BlockEntityTypeExtension<T extends BlockEntity> {
	@Nullable
	BlockEntityVisualizer<? super T> flywheel$getVisualizer();

	void flywheel$setVisualizer(@Nullable BlockEntityVisualizer<? super T> visualizer);
}
