package com.jozufozu.flywheel.api.visual;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.instance.Instance;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface BlockEntityVisual<T extends BlockEntity> extends Visual {
	/**
	 * Collect all instances that should render with a crumbling overlay
	 * when the block corresponding to this visual is being broken.
	 * <br>
	 * Passing {@code null} to the consumer has no effect.
	 *
	 * @param consumer A consumer to provide instances to.
	 */
	void collectCrumblingInstances(Consumer<@Nullable Instance> consumer);
}
