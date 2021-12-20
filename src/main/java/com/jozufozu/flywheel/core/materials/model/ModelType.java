package com.jozufozu.flywheel.core.materials.model;

import com.jozufozu.flywheel.api.struct.Batched;
import com.jozufozu.flywheel.api.struct.BatchingTransformer;
import com.jozufozu.flywheel.api.struct.Instanced;
import com.jozufozu.flywheel.api.struct.StructWriter;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.Programs;
import com.jozufozu.flywheel.core.materials.model.writer.UnsafeModelWriter;
import com.jozufozu.flywheel.core.model.Model;

import net.minecraft.resources.ResourceLocation;

public class ModelType implements Instanced<ModelData>, Batched<ModelData> {

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

	@Override
	public ResourceLocation getProgramSpec() {
		return Programs.TRANSFORMED;
	}

	@Override
	public BatchingTransformer<ModelData> getTransformer(Model model) {
		return null;
	}
}
