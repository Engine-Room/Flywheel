package com.jozufozu.flywheel.core.structs.transformed;

import com.jozufozu.flywheel.api.struct.StorageBufferWriter;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.struct.StructWriter;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.core.layout.CommonItems;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

public class TransformedType implements StructType<TransformedPart> {

	public static final BufferLayout FORMAT = BufferLayout.builder()
			.addItem(CommonItems.LIGHT_COORD, "light")
			.addItem(CommonItems.UNORM_4x8, "color")
			.addItem(CommonItems.MAT4, "pose")
			.addItem(CommonItems.MAT3, "normal")
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
	public StructWriter<TransformedPart> getWriter() {
		return TransformedWriter.INSTANCE;
	}

	@Override
	public StorageBufferWriter<TransformedPart> getStorageBufferWriter() {
		return TransformedStorageWriter.INSTANCE;
	}

	@Override
	public FileResolution getInstanceShader() {
		return Components.Files.TRANSFORMED;
	}

	@Override
	public VertexTransformer<TransformedPart> getVertexTransformer() {
		return (vertexList, struct, level) -> {
			Vector4f pos = new Vector4f();
			Vector3f normal = new Vector3f();

			int light = struct.getPackedLight();
			for (int i = 0; i < vertexList.getVertexCount(); i++) {
				pos.set(
						vertexList.x(i),
						vertexList.y(i),
						vertexList.z(i),
						1F
				);
				pos.transform(struct.model);
				vertexList.x(i, pos.x());
				vertexList.y(i, pos.y());
				vertexList.z(i, pos.z());

				normal.set(
						vertexList.normalX(i),
						vertexList.normalY(i),
						vertexList.normalZ(i)
				);
				normal.transform(struct.normal);
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
