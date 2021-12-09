package com.jozufozu.flywheel.core.materials.oriented;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.backend.struct.Batched;
import com.jozufozu.flywheel.backend.struct.BatchingTransformer;
import com.jozufozu.flywheel.backend.struct.StructWriter;
import com.jozufozu.flywheel.backend.struct.Writeable;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.materials.oriented.writer.UnsafeOrientedWriter;
import com.jozufozu.flywheel.core.model.Model;

public class OrientedType implements Writeable<OrientedData>, Batched<OrientedData> {

	@Override
	public OrientedData create() {
		return new OrientedData();
	}

	@Override
	public VertexFormat format() {
		return Formats.ORIENTED;
	}

	@Override
	public StructWriter<OrientedData> getWriter(VecBuffer backing) {
		return new UnsafeOrientedWriter(backing, this);
	}

	@Override
	public BatchingTransformer<OrientedData> getTransformer(Model model) {
		return null;
	}
}
