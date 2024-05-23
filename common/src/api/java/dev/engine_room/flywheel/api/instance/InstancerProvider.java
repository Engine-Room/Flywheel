package dev.engine_room.flywheel.api.instance;

import dev.engine_room.flywheel.api.BackendImplemented;
import dev.engine_room.flywheel.api.model.Model;

@BackendImplemented
public interface InstancerProvider {
	/**
	 * Get an instancer for the given instance type rendering the given model.
	 *
	 * <p>Calling this method twice with the same arguments in the
	 * same frame will return the same instancer.</p>
	 *
	 * <p>It is not safe to store instancers between frames. Each
	 * time you need an instancer, you should call this method.</p>
	 *
	 * @return An instancer for the given instance type rendering the given model.
	 */
	<I extends Instance> Instancer<I> instancer(InstanceType<I> type, Model model);
}
