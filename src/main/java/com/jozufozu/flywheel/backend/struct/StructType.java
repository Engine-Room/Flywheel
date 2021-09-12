package com.jozufozu.flywheel.backend.struct;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;

public interface StructType<S> {

	S create();

	VertexFormat format();

	StructWriter<S> getWriter(VecBuffer backing);
}
