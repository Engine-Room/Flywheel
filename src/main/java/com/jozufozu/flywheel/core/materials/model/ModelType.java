package com.jozufozu.flywheel.core.materials.model;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.backend.struct.StructType;
import com.jozufozu.flywheel.backend.struct.StructWriter;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.materials.model.writer.UnsafeModelWriter;

public class ModelType implements StructType<ModelData> {

	@Override
	public ModelData create() {
		return new ModelData();
	}

	@Override
	public VertexFormat format() {
		return Formats.TRANSFORMED;
	}

	@Override
	public StructWriter<ModelData> getWriter(VecBuffer backing) {
		return new UnsafeModelWriter(backing, this);
	}
}
