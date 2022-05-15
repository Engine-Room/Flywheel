package com.jozufozu.flywheel.core.materials.model;

import com.jozufozu.flywheel.api.struct.Batched;
import com.jozufozu.flywheel.api.struct.Instanced;
import com.jozufozu.flywheel.api.struct.StructWriter;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.core.layout.CommonItems;
import com.jozufozu.flywheel.core.materials.InstanceShaders;
import com.jozufozu.flywheel.core.model.ModelTransformer;
import com.jozufozu.flywheel.core.source.FileResolution;

public class ModelType implements Instanced<ModelData>, Batched<ModelData> {

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
	public StructWriter<ModelData> getWriter(VecBuffer backing) {
		return new ModelWriterUnsafe(backing, this);
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
