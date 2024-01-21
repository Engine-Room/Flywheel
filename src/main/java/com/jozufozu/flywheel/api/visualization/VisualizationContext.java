package com.jozufozu.flywheel.api.visualization;

import com.jozufozu.flywheel.api.instance.InstancerProvider;

import net.minecraft.core.Vec3i;

/**
 * A context object passed on visual creation.
 *
 * @param instancerProvider The {@link InstancerProvider} that the visual can use to get instancers to render models.
 * @param renderOrigin      The origin of the renderer as a world position.
 *                          All models render as if this position is (0, 0, 0).
 */
public record VisualizationContext(InstancerProvider instancerProvider, LightUpdater lightUpdater, Vec3i renderOrigin) {
}
