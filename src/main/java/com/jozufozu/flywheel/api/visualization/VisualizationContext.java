package com.jozufozu.flywheel.api.visualization;

import org.jetbrains.annotations.ApiStatus;

import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.api.instance.InstancerProvider;

import net.minecraft.core.Vec3i;

/**
 * A context object passed on visual creation.
 */
@ApiStatus.NonExtendable
public interface VisualizationContext {
	/**
	 * @return The {@link InstancerProvider} that the visual can use to get instancers to render models.
	 */
	InstancerProvider instancerProvider();

	/**
	 * All models render as if this position is (0, 0, 0).
	 *
	 * @return The origin of the renderer as a world position.
	 */
	Vec3i renderOrigin();

	/**
	 * Create a new {@link VisualizationContext} with the given {@link Context} and render origin.
	 *
	 * @param context The new context.
	 * @param renderOrigin The new render origin.
	 * @return A new {@link VisualizationContext} for use with child visuals.
	 */
	@ApiStatus.Experimental
	VisualizationContext withContext(Context context, Vec3i renderOrigin);
}
