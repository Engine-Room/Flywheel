package dev.engine_room.flywheel.api.instance;

import dev.engine_room.flywheel.api.backend.BackendImplemented;
import dev.engine_room.flywheel.api.model.Model;

@BackendImplemented
public interface InstancerProvider {
	/**
	 * Get an instancer for the given instance type rendering the given model.
	 *
	 * <p>Calling this method twice with the same arguments in the
	 * same frame will return the same instancer.</p>
	 *
	 * <p>It is NOT safe to store instancers between frames. Each
	 * time you need an instancer, you should call this method.</p>
	 *
	 * <h2>Render Order</h2>
	 * <p>In general, you can assume all instances in the same instancer will be rendered in a single draw call.
	 * Backends are free to optimize the ordering of draw calls to a certain extent, but utilities are provided to let
	 * you exert control over the ordering.</p>
	 * <h4>Mesh Order</h4>
	 * <p>For one, Meshes within a Model are guaranteed to render in the order they appear in their containing list.
	 * This lets you e.g. preserve (or break!) vanilla's chunk RenderType order guarantees or control which Meshes of
	 * your Model render over others.</p>
	 * <h4>Bias Order</h4>
	 * <p>The other method is via the {@code bias} parameter to this method. An instancer with a lower bias will have
	 * its instances draw BEFORE an instancer with a higher bias. This allows you to control the render order between
	 * your instances to e.g. create an "overlay" instance to selectively color or apply decals to another instance.</p>
	 *
	 * @param type The instance type to parameterize your instances by.
	 * @param model The Model to instance.
	 * @param bias A weight to control render order between instancers.
	 *                Instancers are rendered in ascending order by bias.
	 * @return An instancer.
	 */
	<I extends Instance> Instancer<I> instancer(InstanceType<I> type, Model model, int bias);

	/**
	 * Get an instancer with no bias for the given instance type rendering the given model with.
	 *
	 * @param type The instance type to parameterize your instances by.
	 * @param model The model to instance.
	 * @return An instancer with {@code bias == 0}.
	 */
	default <I extends Instance> Instancer<I> instancer(InstanceType<I> type, Model model) {
		return instancer(type, model, 0);
	}
}
