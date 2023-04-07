package com.jozufozu.flywheel.lib.struct;

import com.jozufozu.flywheel.api.layout.BufferLayout;
import com.jozufozu.flywheel.api.struct.Handle;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.struct.StructVertexTransformer;
import com.jozufozu.flywheel.api.struct.StructWriter;
import com.jozufozu.flywheel.lib.layout.CommonItems;
import com.jozufozu.flywheel.lib.math.RenderMath;
import com.jozufozu.flywheel.lib.vertex.VertexTransformations;

import net.minecraft.resources.ResourceLocation;

public class TransformedType implements StructType<TransformedPart> {
	public static final BufferLayout FORMAT = BufferLayout.builder()
			.addItem(CommonItems.LIGHT_COORD, "light")
			.addItem(CommonItems.UNORM_4x8, "color")
			.addItem(CommonItems.MAT4, "pose")
			.addItem(CommonItems.MAT3, "normal")
			.build();

	@Override
	public TransformedPart create(Handle handle) {
		return new TransformedPart(this, handle);
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
	public ResourceLocation instanceShader() {
		return StructTypes.Files.TRANSFORMED;
	}

	@Override
	public StructVertexTransformer<TransformedPart> getVertexTransformer() {
		return (vertexList, struct, level) -> {
			float r = RenderMath.uf(struct.r);
			float g = RenderMath.uf(struct.g);
			float b = RenderMath.uf(struct.b);
			float a = RenderMath.uf(struct.a);
			int light = struct.getPackedLight();

			for (int i = 0; i < vertexList.vertexCount(); i++) {
				VertexTransformations.transformPos(vertexList, i, struct.model);
				VertexTransformations.transformNormal(vertexList, i, struct.normal);

				vertexList.r(i, r);
				vertexList.g(i, g);
				vertexList.b(i, b);
				vertexList.a(i, a);
				vertexList.light(i, light);
			}
		};
	}
}
