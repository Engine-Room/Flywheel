package com.jozufozu.flywheel.extension;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.visualization.BlockEntityVisualizer;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface BlockEntityTypeExtension<T extends BlockEntity> {
	@Nullable
	BlockEntityVisualizer<? super T> flywheel$getVisualizer();

	void flywheel$setVisualizer(@Nullable BlockEntityVisualizer<? super T> visualizer);
}
