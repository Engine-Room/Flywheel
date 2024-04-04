package com.jozufozu.flywheel.api.visual;

import org.jetbrains.annotations.ApiStatus;
import org.joml.FrustumIntersection;

import com.jozufozu.flywheel.api.task.Plan;

import net.minecraft.client.Camera;

/**
 * An interface giving {@link Visual}s a hook to have a function called at
 * the start of a frame. By implementing {@link DynamicVisual}, an {@link Visual}
 * can animate its models in ways that could not be easily achieved by shader attribute
 * parameterization.
 */
public interface DynamicVisual extends Visual {
	Plan<Context> planFrame();

	/**
	 * The context passed to the frame plan.
	 */
	@ApiStatus.NonExtendable
	interface Context {
		Camera camera();

		FrustumIntersection frustum();

		float partialTick();

		DistanceUpdateLimiter limiter();
	}
}
