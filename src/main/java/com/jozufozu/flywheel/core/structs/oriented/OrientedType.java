package com.jozufozu.flywheel.core.structs.oriented;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.api.struct.StorageBufferWriter;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.struct.StructWriter;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.core.layout.CommonItems;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

public class OrientedType implements StructType<OrientedPart> {

	public static final BufferLayout FORMAT = BufferLayout.builder()
			.addItems(CommonItems.LIGHT, CommonItems.RGBA)
			.addItems(CommonItems.VEC3, CommonItems.VEC3, CommonItems.QUATERNION)
			.build();

	@Override
	public OrientedPart create() {
		return new OrientedPart();
	}

	@Override
	public BufferLayout getLayout() {
		return FORMAT;
	}

	@Override
	public StructWriter<OrientedPart> getWriter(ByteBuffer backing) {
		return new OrientedWriterUnsafe(this, backing);
	}

	@Override
	public OrientedStorageWriter getStorageBufferWriter() {
		return OrientedStorageWriter.INSTANCE;
	}

	@Override
	public FileResolution getInstanceShader() {
		return Components.Files.ORIENTED;
	}

	@Override
	public VertexTransformer<? extends OrientedPart> getVertexTransformer() {
		return (vertexList, struct, level) -> {
			Vector4f pos = new Vector4f();
			Vector3f normal = new Vector3f();

			Quaternion q = new Quaternion(struct.qX, struct.qY, struct.qZ, struct.qW);

			Matrix4f modelMatrix = new Matrix4f();
			modelMatrix.setIdentity();
			modelMatrix.multiplyWithTranslation(struct.posX + struct.pivotX, struct.posY + struct.pivotY, struct.posZ + struct.pivotZ);
			modelMatrix.multiply(q);
			modelMatrix.multiplyWithTranslation(-struct.pivotX, -struct.pivotY, -struct.pivotZ);

			Matrix3f normalMatrix = new Matrix3f(q);

			int light = struct.getPackedLight();
			for (int i = 0; i < vertexList.getVertexCount(); i++) {
				pos.set(
						vertexList.x(i),
						vertexList.y(i),
						vertexList.z(i),
						1f
				);
				pos.transform(modelMatrix);
				vertexList.x(i, pos.x());
				vertexList.y(i, pos.y());
				vertexList.z(i, pos.z());

				normal.set(
						vertexList.normalX(i),
						vertexList.normalY(i),
						vertexList.normalZ(i)
				);
				normal.transform(normalMatrix);
				normal.normalize();
				vertexList.normalX(i, normal.x());
				vertexList.normalY(i, normal.y());
				vertexList.normalZ(i, normal.z());

				vertexList.r(i, struct.r);
				vertexList.g(i, struct.g);
				vertexList.b(i, struct.b);
				vertexList.a(i, struct.a);
				vertexList.light(i, light);
			}
		};
	}
}
