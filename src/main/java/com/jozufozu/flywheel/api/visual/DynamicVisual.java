package com.jozufozu.flywheel.api.visual;

import org.joml.FrustumIntersection;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.Instancer;

/**
 * An interface giving {@link Visual}s a hook to have a function called at
 * the start of a frame. By implementing {@link DynamicVisual}, an {@link Visual}
 * can animate its models in ways that could not be easily achieved by shader attribute
 * parameterization.
 *
 * <br><br> If your goal is offloading work to shaders, but you're unsure exactly how you need
 * to parameterize the instances, you're encouraged to implement this for prototyping.
 */
public interface DynamicVisual extends Visual {
	/**
	 * Called every frame, and after initialization.
	 * <br>
	 * <em>DISPATCHED IN PARALLEL</em>, don't attempt to mutate anything outside this visual.
	 * <br>
	 * {@link Instancer}/{@link Instance} creation/acquisition is safe here.
	 */
	void beginFrame();

	/**
	 * As a further optimization, dynamic visuals that are far away are updated less often.
	 * This behavior can be disabled by returning false.
	 *
	 * <br> You might want to opt out of this if you want your animations to remain smooth
	 * even when far away from the camera. It is recommended to keep this as is, however.
	 *
	 * @return {@code true} if your visual should be slow updated.
	 */
	default boolean decreaseFramerateWithDistance() {
		return true;
	}

	/**
	 * Check this visual against a frustum.<p>
	 * An implementor may choose to return a constant to skip the frustum check.
	 *
	 * @param frustum A frustum intersection tester for the current frame.
	 * @return {@code true} if this visual should be considered for updates.
	 */
	boolean isVisible(FrustumIntersection frustum);
}
