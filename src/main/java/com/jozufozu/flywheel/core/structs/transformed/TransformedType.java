package com.jozufozu.flywheel.core.structs.transformed;

import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.struct.StructWriter;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.core.layout.CommonItems;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.util.RenderMath;
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
	public FileResolution getInstanceShader() {
		return Components.Files.TRANSFORMED;
	}

	@Override
	public VertexTransformer<TransformedPart> getVertexTransformer() {
		return (vertexList, struct, level) -> {
			Vector4f pos = new Vector4f();
			Vector3f normal = new Vector3f();

			float r = RenderMath.uf(struct.r);
			float g = RenderMath.uf(struct.g);
			float b = RenderMath.uf(struct.b);
			float a = RenderMath.uf(struct.a);
			int light = struct.getPackedLight();

			for (int i = 0; i < vertexList.vertexCount(); i++) {
				pos.set(
						vertexList.x(i),
						vertexList.y(i),
						vertexList.z(i),
						1f
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

				vertexList.r(i, r);
				vertexList.g(i, g);
				vertexList.b(i, b);
				vertexList.a(i, a);
				vertexList.light(i, light);
			}
		};
	}
}
