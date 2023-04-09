package com.jozufozu.flywheel.api.instance;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.model.Model;

public interface InstancerProvider {
	/**
	 * Get an instancer for the given instance type, model, and render stage. Calling this method twice with the same arguments will return the same instancer.
	 *
	 * @return An instancer for the given instance type, model, and render stage.
	 */
	<I extends Instance> Instancer<I> instancer(InstanceType<I> type, Model model, RenderStage stage);
}
