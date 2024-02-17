package com.jozufozu.flywheel.api.backend;

import org.jetbrains.annotations.ApiStatus;

import com.jozufozu.flywheel.api.BackendImplemented;
import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.instance.InstancerProvider;
import com.jozufozu.flywheel.api.model.Model;

@BackendImplemented
@ApiStatus.Experimental
public interface DirectInstancerProvider {
	/**
	 * Get an instancer for the given instance type, model, and render stage.
	 *
	 * <p>Calling this method twice with the same arguments will return the same instancer.</p>
	 *
	 * <p>If you are writing a visual you should probably be using
	 * {@link InstancerProvider#instancer(InstanceType, Model)}, which will decide the {@code RenderStage}
	 * based on what type of visual is getting the instancer as well as hide the Context.</p>
	 *
	 * @return An instancer for the given instance type, model, and render stage.
	 * @see InstancerProvider
	 */
	<I extends Instance> Instancer<I> instancer(InstanceType<I> type, Context context, Model model, RenderStage stage);
}
