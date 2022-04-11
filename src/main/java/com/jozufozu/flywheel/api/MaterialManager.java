package com.jozufozu.flywheel.api;

import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.instancing.instancing.InstancedMaterial;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Vec3i;

public interface MaterialManager {

	<D extends InstanceData> InstancedMaterial<D> material(StructType<D> type);

	Vec3i getOriginCoordinate();

}
