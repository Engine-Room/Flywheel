package com.jozufozu.flywheel.api.instancer;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.struct.StructType;

import net.minecraft.core.Vec3i;

public interface InstancerManager {

	/**
	 * Get an instancer for the given struct type and model. Calling this method twice with the same arguments will return the same instancer.
	 *
	 * @return An instancer for the given struct type and model.
	 */
	<D extends InstancedPart> Instancer<D> instancer(StructType<D> type, Model model, RenderStage stage);

	Vec3i getOriginCoordinate();

}
