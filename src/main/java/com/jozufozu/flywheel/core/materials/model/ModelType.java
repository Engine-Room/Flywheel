package com.jozufozu.flywheel.core.materials.model;

import com.jozufozu.flywheel.api.struct.Batched;
import com.jozufozu.flywheel.api.struct.Instanced;
import com.jozufozu.flywheel.api.struct.StructWriter;
import com.jozufozu.flywheel.backend.gl.attrib.CommonAttributes;
import com.jozufozu.flywheel.backend.gl.attrib.MatrixAttributes;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.core.Programs;
import com.jozufozu.flywheel.core.model.ModelTransformer;

import net.minecraft.resources.ResourceLocation;

public class ModelType implements Instanced<ModelData>, Batched<ModelData> {

	public static final VertexFormat FORMAT = VertexFormat.builder()
			.addAttributes(CommonAttributes.LIGHT, CommonAttributes.RGBA)
			.addAttributes(MatrixAttributes.MAT4, MatrixAttributes.MAT3)
			.build();

	@Override
	public ModelData create() {
		return new ModelData();
	}

	@Override
	public VertexFormat format() {
		return FORMAT;
	}

	@Override
	public StructWriter<ModelData> getWriter(VecBuffer backing) {
		return new UnsafeModelWriter(backing, this);
	}

	@Override
	public ResourceLocation getProgramSpec() {
		return Programs.TRANSFORMED;
	}

	@Override
	public void transform(ModelData d, ModelTransformer.Params b) {
		b.transform(d.model, d.normal)
				.color(d.r, d.g, d.b, d.a)
				.light(d.getPackedLight());
	}
}
