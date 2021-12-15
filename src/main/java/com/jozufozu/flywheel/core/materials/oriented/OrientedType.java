package com.jozufozu.flywheel.core.materials.oriented;

import com.jozufozu.flywheel.api.struct.Batched;
import com.jozufozu.flywheel.api.struct.BatchingTransformer;
import com.jozufozu.flywheel.api.struct.Instanced;
import com.jozufozu.flywheel.api.struct.StructWriter;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.Programs;
import com.mojang.math.Quaternion;

import net.minecraft.resources.ResourceLocation;

public class OrientedType implements Instanced<OrientedData>, Batched<OrientedData> {

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
	public ResourceLocation getProgramSpec() {
		return Programs.ORIENTED;
	}

	@Override
	public BatchingTransformer<OrientedData> getTransformer() {
		return (d, sbb) -> {
			sbb.light(d.getPackedLight())
					.color(d.r, d.g, d.b, d.a)
					.translate(d.posX + d.pivotX, d.posY + d.pivotY, d.posZ + d.pivotZ)
					.multiply(new Quaternion(d.qX, d.qY, d.qZ, d.qW))
					.translate(-d.pivotX, -d.pivotY, -d.pivotZ);
		};
	}
}
