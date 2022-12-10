package com.jozufozu.flywheel.core.materials.oriented;

import org.joml.Quaternionf;

import com.jozufozu.flywheel.api.struct.Batched;
import com.jozufozu.flywheel.api.struct.Instanced;
import com.jozufozu.flywheel.api.struct.StructWriter;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.core.Programs;
import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.core.layout.CommonItems;
import com.jozufozu.flywheel.core.model.ModelTransformer;

import net.minecraft.resources.ResourceLocation;

public class OrientedType implements Instanced<OrientedData>, Batched<OrientedData> {

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
	public StructWriter<OrientedData> getWriter(VecBuffer backing) {
		return new OrientedWriterUnsafe(backing, this);
	}

	@Override
	public ResourceLocation getProgramSpec() {
		return Programs.ORIENTED;
	}

	@Override
	public void transform(OrientedData d, ModelTransformer.Params b) {
		b.light(d.getPackedLight())
				.color(d.r, d.g, d.b, d.a)
				.translate(d.posX + d.pivotX, d.posY + d.pivotY, d.posZ + d.pivotZ)
				.multiply(new Quaternionf(d.qX, d.qY, d.qZ, d.qW))
				.translate(-d.pivotX, -d.pivotY, -d.pivotZ);
	}
}
