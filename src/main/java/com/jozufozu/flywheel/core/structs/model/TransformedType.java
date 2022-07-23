package com.jozufozu.flywheel.core.structs.model;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.struct.StructWriter;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.core.layout.CommonItems;
import com.jozufozu.flywheel.core.model.ModelTransformer;
import com.jozufozu.flywheel.core.source.FileResolution;

public class TransformedType implements StructType<TransformedPart> {

	public static final BufferLayout FORMAT = BufferLayout.builder()
			.addItems(CommonItems.LIGHT, CommonItems.RGBA)
			.addItems(CommonItems.MAT4, CommonItems.MAT3)
			.build();

	@Override
	public TransformedPart create() {
		return new TransformedPart();
	}

	@Override
	public BufferLayout getLayout() {
		return FORMAT;
	}

	@Override
	public StructWriter<TransformedPart> getWriter(ByteBuffer backing) {
		return new TransformedWriterUnsafe(this, backing);
	}

	@Override
	public FileResolution getInstanceShader() {
		return Components.Files.TRANSFORMED;
	}

	@Override
	public void transform(TransformedPart d, ModelTransformer.Params b) {
		b.transform(d.model, d.normal)
				.color(d.r, d.g, d.b, d.a)
				.light(d.getPackedLight());
	}
}
