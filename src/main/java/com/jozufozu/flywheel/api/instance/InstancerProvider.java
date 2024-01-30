package com.jozufozu.flywheel.api.instance;

import com.jozufozu.flywheel.api.model.Model;

public interface InstancerProvider {
	/**
	 * Get an instancer for the given instance type rendering the given model.
	 *
	 * <p>Calling this method twice with the same arguments will return the same instancer.</p>
	 *
	 * @return An instancer for the given instance type rendering the given model.
	 */
	<I extends Instance> Instancer<I> instancer(InstanceType<I> type, Model model);
}
