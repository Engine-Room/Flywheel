package com.jozufozu.flywheel.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.jozufozu.flywheel.api.visualization.BlockEntityVisualizer;
import com.jozufozu.flywheel.extension.BlockEntityTypeExtension;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

@Mixin(BlockEntityType.class)
abstract class BlockEntityTypeMixin<T extends BlockEntity> implements BlockEntityTypeExtension<T> {
	@Unique
	private BlockEntityVisualizer<? super T> flywheel$visualizer;

	@Override
	@Nullable
	public BlockEntityVisualizer<? super T> flywheel$getVisualizer() {
		return flywheel$visualizer;
	}

	@Override
	public void flywheel$setVisualizer(@Nullable BlockEntityVisualizer<? super T> visualizer) {
		this.flywheel$visualizer = visualizer;
	}
}
