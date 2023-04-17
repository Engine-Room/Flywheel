package com.jozufozu.flywheel.lib.instance;

import com.jozufozu.flywheel.api.instance.InstanceBoundingSphereTransformer;
import com.jozufozu.flywheel.api.instance.InstanceHandle;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.InstanceVertexTransformer;
import com.jozufozu.flywheel.api.instance.InstanceWriter;
import com.jozufozu.flywheel.api.layout.BufferLayout;
import com.jozufozu.flywheel.lib.layout.CommonItems;
import com.jozufozu.flywheel.lib.math.MatrixUtil;
import com.jozufozu.flywheel.lib.math.RenderMath;
import com.jozufozu.flywheel.lib.vertex.VertexTransformations;

import net.minecraft.resources.ResourceLocation;

public class TransformedType implements InstanceType<TransformedInstance> {
	public static final BufferLayout FORMAT = BufferLayout.builder()
			.addItem(CommonItems.LIGHT_COORD, "light")
			.addItem(CommonItems.UNORM_4x8, "color")
			.addItem(CommonItems.MAT4, "pose")
			.addItem(CommonItems.MAT3, "normal")
			.build();

	@Override
	public TransformedInstance create(InstanceHandle handle) {
		return new TransformedInstance(this, handle);
	}

	@Override
	public BufferLayout getLayout() {
		return FORMAT;
	}

	@Override
	public InstanceWriter<TransformedInstance> getWriter() {
		return TransformedWriter.INSTANCE;
	}

	@Override
	public ResourceLocation instanceShader() {
		return InstanceTypes.Files.TRANSFORMED;
	}

	@Override
	public InstanceVertexTransformer<TransformedInstance> getVertexTransformer() {
		return (vertexList, instance, level) -> {
			float r = RenderMath.uf(instance.r);
			float g = RenderMath.uf(instance.g);
			float b = RenderMath.uf(instance.b);
			float a = RenderMath.uf(instance.a);
			int light = instance.getPackedLight();

			for (int i = 0; i < vertexList.vertexCount(); i++) {
				VertexTransformations.transformPos(vertexList, i, instance.model);
				VertexTransformations.transformNormal(vertexList, i, instance.normal);

				vertexList.r(i, r);
				vertexList.g(i, g);
				vertexList.b(i, b);
				vertexList.a(i, a);
				vertexList.light(i, light);
			}
		};
	}

	@Override
	public InstanceBoundingSphereTransformer<TransformedInstance> getBoundingSphereTransformer() {
		return (boundingSphere, instance) -> {
			var radius = boundingSphere.w;
			boundingSphere.w = 1;
			boundingSphere.mul(MatrixUtil.toJoml(instance.model));
			boundingSphere.w = radius * MatrixUtil.extractScale(instance.model);
		};
	}
}
