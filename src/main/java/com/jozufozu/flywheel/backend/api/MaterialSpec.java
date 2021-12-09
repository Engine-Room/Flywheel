package com.jozufozu.flywheel.backend.api;

import com.jozufozu.flywheel.backend.struct.StructType;

import net.minecraft.resources.ResourceLocation;

public class MaterialSpec<D extends InstanceData> {

	public final ResourceLocation name;

	private final ResourceLocation programSpec;
	private final StructType<D> instanceType;

	public MaterialSpec(ResourceLocation name, ResourceLocation programSpec, StructType<D> type) {
		this.name = name;
		this.programSpec = programSpec;
		this.instanceType = type;
	}

	public ResourceLocation getProgramName() {
		return programSpec;
	}

	public StructType<D> getInstanceType() {
		return instanceType;
	}

}
