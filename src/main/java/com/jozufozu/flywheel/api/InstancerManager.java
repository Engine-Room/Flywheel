package com.jozufozu.flywheel.api;

import com.jozufozu.flywheel.api.struct.StructType;

import net.minecraft.core.Vec3i;

public interface InstancerManager {

	<D extends InstanceData> InstancerFactory<D> factory(StructType<D> type);

	Vec3i getOriginCoordinate();

}
