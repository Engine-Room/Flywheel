package com.jozufozu.flywheel.api.instance.controller;

import com.jozufozu.flywheel.api.instancer.InstancerProvider;

import net.minecraft.core.Vec3i;

/**
 * A context object passed on Instance creation.
 *
 * @param instancerProvider The {@link InstancerProvider} that the instance can use to get instancers to render models.
 * @param renderOrigin      The origin of the renderer as a world position.
 *                          All models render as if this position is (0, 0, 0).
 */
public record InstanceContext(InstancerProvider instancerProvider, Vec3i renderOrigin) {
}
