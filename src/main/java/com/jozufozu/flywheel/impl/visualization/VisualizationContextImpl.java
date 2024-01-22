package com.jozufozu.flywheel.impl.visualization;

import com.jozufozu.flywheel.api.instance.InstancerProvider;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;

import net.minecraft.core.Vec3i;

/**
 * A context object passed on visual creation.
 *
 * @param instancerProvider The {@link InstancerProvider} that the visual can use to get instancers to render models.
 * @param renderOrigin      The origin of the renderer as a world position.
 *                          All models render as if this position is (0, 0, 0).
 */
public record VisualizationContextImpl(InstancerProvider instancerProvider,
									   Vec3i renderOrigin) implements VisualizationContext {
}
