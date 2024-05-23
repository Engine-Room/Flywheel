package dev.engine_room.flywheel.api.visual;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.instance.Instance;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * A visual associated with a specific block entity.
 *
 * <p>BlockEntityVisuals exist for at most the lifetime of the block entity they are associated with.</p>
 *
 * <p>If the block state at your BlockEntityVisual's position changes without removing the block entity,
 * the visual will be deleted and recreated. Therefore, it is also safe to assume than the block state
 * is constant for the lifetime of the visual.</p>
 *
 * @param <T> The block entity type.
 */
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
