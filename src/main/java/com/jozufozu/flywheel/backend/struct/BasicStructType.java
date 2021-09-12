package com.jozufozu.flywheel.backend.struct;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.backend.instancing.InstanceData;

import net.minecraftforge.common.util.NonNullSupplier;

public class BasicStructType<S extends InstanceData> implements StructType<S> {

	private final NonNullSupplier<S> factory;
	private final VertexFormat format;

	public BasicStructType(NonNullSupplier<S> factory, VertexFormat format) {
		this.factory = factory;
		this.format = format;
	}

	@Override
	public S create() {
		return factory.get();
	}

	@Override
	public VertexFormat format() {
		return format;
	}

	@Override
	public StructWriter<S> getWriter(VecBuffer backing) {
		return new BasicWriter(backing);
	}

	public class BasicWriter implements StructWriter<S> {
		private final VecBuffer buffer;

		public BasicWriter(VecBuffer buffer) {
			this.buffer = buffer;
		}

		@Override
		public void write(S struct) {
			struct.write(buffer);
		}

		@Override
		public void seek(int pos) {
			buffer.position(pos * format.getStride());
		}
	}
}
