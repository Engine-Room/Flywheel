package com.jozufozu.flywheel.core.structs.model;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.api.struct.BatchedStructType;
import com.jozufozu.flywheel.api.struct.InstancedStructType;
import com.jozufozu.flywheel.api.struct.StructWriter;
import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.core.layout.CommonItems;
import com.jozufozu.flywheel.core.model.ModelTransformer;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.structs.InstanceShaders;

public class ModelType implements InstancedStructType<ModelData>, BatchedStructType<ModelData> {

	public static final BufferLayout FORMAT = BufferLayout.builder()
			.addItems(CommonItems.LIGHT, CommonItems.RGBA)
			.addItems(CommonItems.MAT4, CommonItems.MAT3)
			.build();

	@Override
	public ModelData create() {
		return new ModelData();
	}

	@Override
	public BufferLayout getLayout() {
		return FORMAT;
	}

	@Override
	public StructWriter<ModelData> getWriter(ByteBuffer backing) {
		return new ModelWriterUnsafe(this, backing);
	}

	@Override
	public FileResolution getInstanceShader() {
		return InstanceShaders.MODEL;
	}

	@Override
	public void transform(ModelData d, ModelTransformer.Params b) {
		b.transform(d.model, d.normal)
				.color(d.r, d.g, d.b, d.a)
				.light(d.getPackedLight());
	}
}
