package com.jozufozu.flywheel.backend.material;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.instancing.InstanceData;
import com.jozufozu.flywheel.backend.struct.StructType;

import net.minecraft.util.ResourceLocation;

public class MaterialSpec<D extends InstanceData> {

	public final ResourceLocation name;

	private final ResourceLocation programSpec;
	private final VertexFormat modelFormat;
	private final StructType<D> instanceType;

	public MaterialSpec(ResourceLocation name, ResourceLocation programSpec, VertexFormat modelFormat, StructType<D> type) {
		this.name = name;
		this.programSpec = programSpec;
		this.modelFormat = modelFormat;
		this.instanceType = type;
	}

	public ResourceLocation getProgramName() {
		return programSpec;
	}

	public VertexFormat getModelFormat() {
		return modelFormat;
	}

	public StructType<D> getInstanceType() {
		return instanceType;
	}

}
