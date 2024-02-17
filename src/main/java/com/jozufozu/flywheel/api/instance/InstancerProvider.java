package com.jozufozu.flywheel.api.instance;

import org.jetbrains.annotations.ApiStatus;

import com.jozufozu.flywheel.api.backend.DirectInstancerProvider;
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

	/**
	 * Get the {@link DirectInstancerProvider} this provider is built on top of.
	 *
	 * <p>The direct provider allows for explicit control over the
	 * {@link com.jozufozu.flywheel.api.context.Context Context} and
	 * {@link com.jozufozu.flywheel.api.event.RenderStage RenderStage}.
	 * Generally this is a safe operation, though compatibility issues basically guaranteed
	 * if you mess with the Context <em>and</em> nest visuals.</p>
	 * @return A DirectInstancerProvider.
	 */
	@ApiStatus.Experimental
	DirectInstancerProvider _directProvider();
}
