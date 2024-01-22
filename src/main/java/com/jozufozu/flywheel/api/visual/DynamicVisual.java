package com.jozufozu.flywheel.api.visual;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.Instancer;

/**
 * An interface giving {@link Visual}s a hook to have a function called at
 * the start of a frame. By implementing {@link DynamicVisual}, an {@link Visual}
 * can animate its models in ways that could not be easily achieved by shader attribute
 * parameterization.
 * <p>
 * If your goal is offloading work to shaders, but you're unsure exactly how you need
 * to parameterize the instances, you're encouraged to implement this for prototyping.
 */
public interface DynamicVisual extends Visual {
	/**
	 * Called every frame.
	 * <br>
	 * The implementation is free to parallelize calls to this method.
	 * You must ensure proper synchronization if you need to mutate anything outside this visual.
	 * <br>
	 * This method and {@link TickableVisual#tick} will never be called simultaneously.
	 * <br>
	 * {@link Instancer}/{@link Instance} creation/acquisition is safe here.
	 */
	void beginFrame(VisualFrameContext ctx);
}
