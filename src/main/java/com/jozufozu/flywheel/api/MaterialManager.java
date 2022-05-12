package com.jozufozu.flywheel.api;

import com.jozufozu.flywheel.api.struct.StructType;

import net.minecraft.core.Vec3i;

public interface MaterialManager {

	<D extends InstanceData> Material<D> material(StructType<D> type);

	Vec3i getOriginCoordinate();

}
