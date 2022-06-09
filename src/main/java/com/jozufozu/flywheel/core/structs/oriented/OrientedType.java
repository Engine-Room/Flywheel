package com.jozufozu.flywheel.core.structs.oriented;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.api.struct.BatchedStructType;
import com.jozufozu.flywheel.api.struct.InstancedStructType;
import com.jozufozu.flywheel.api.struct.StructWriter;
import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.core.layout.CommonItems;
import com.jozufozu.flywheel.core.model.ModelTransformer;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.structs.InstanceShaders;
import com.mojang.math.Quaternion;

public class OrientedType implements InstancedStructType<OrientedData>, BatchedStructType<OrientedData> {

	public static final BufferLayout FORMAT = BufferLayout.builder()
			.addItems(CommonItems.LIGHT, CommonItems.RGBA)
			.addItems(CommonItems.VEC3, CommonItems.VEC3, CommonItems.QUATERNION)
			.build();

	@Override
	public OrientedData create() {
		return new OrientedData();
	}

	@Override
	public BufferLayout getLayout() {
		return FORMAT;
	}

	@Override
	public StructWriter<OrientedData> getWriter(ByteBuffer backing) {
		return new OrientedWriterUnsafe(this, backing);
	}

	@Override
	public FileResolution getInstanceShader() {
		return InstanceShaders.ORIENTED;
	}

	@Override
	public void transform(OrientedData d, ModelTransformer.Params b) {
		b.light(d.getPackedLight())
				.color(d.r, d.g, d.b, d.a)
				.translate(d.posX + d.pivotX, d.posY + d.pivotY, d.posZ + d.pivotZ)
				.multiply(new Quaternion(d.qX, d.qY, d.qZ, d.qW))
				.translate(-d.pivotX, -d.pivotY, -d.pivotZ);
	}
}
