package com.jozufozu.flywheel.lib.instance;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.instance.InstanceBoundingSphereTransformer;
import com.jozufozu.flywheel.api.instance.InstanceHandle;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.InstanceVertexTransformer;
import com.jozufozu.flywheel.api.instance.InstanceWriter;
import com.jozufozu.flywheel.api.layout.BufferLayout;
import com.jozufozu.flywheel.lib.layout.CommonItems;
import com.jozufozu.flywheel.lib.math.RenderMath;
import com.jozufozu.flywheel.lib.vertex.VertexTransformations;

import net.minecraft.resources.ResourceLocation;

public class OrientedType implements InstanceType<OrientedInstance> {
	private static final BufferLayout LAYOUT = BufferLayout.builder()
			.addItem(CommonItems.LIGHT_COORD, "light")
			.addItem(CommonItems.UNORM_4x8, "color")
			.addItem(CommonItems.VEC3, "position")
			.addItem(CommonItems.VEC3, "pivot")
			.addItem(CommonItems.VEC4, "rotation")
			.build();

	private static final ResourceLocation VERTEX_SHADER = Flywheel.rl("instance/oriented.vert");
	private static final ResourceLocation CULL_SHADER = Flywheel.rl("instance/cull/oriented.glsl");

	@Override
	public OrientedInstance create(InstanceHandle handle) {
		return new OrientedInstance(this, handle);
	}

	@Override
	public BufferLayout getLayout() {
		return LAYOUT;
	}

	@Override
	public InstanceWriter<OrientedInstance> getWriter() {
		return OrientedWriter.INSTANCE;
	}

	@Override
	public ResourceLocation vertexShader() {
		return VERTEX_SHADER;
	}

	@Override
	public ResourceLocation cullShader() {
		return CULL_SHADER;
	}

	@Override
	public InstanceVertexTransformer<OrientedInstance> getVertexTransformer() {
		return (vertexList, instance) -> {
			Matrix4f modelMatrix = new Matrix4f();
			modelMatrix.translate(instance.posX + instance.pivotX, instance.posY + instance.pivotY, instance.posZ + instance.pivotZ);
			modelMatrix.rotate(instance.rotation);
			modelMatrix.translate(-instance.pivotX, -instance.pivotY, -instance.pivotZ);

			Matrix3f normalMatrix = new Matrix3f();
			normalMatrix.set(instance.rotation);

			float r = RenderMath.uf(instance.r);
			float g = RenderMath.uf(instance.g);
			float b = RenderMath.uf(instance.b);
			float a = RenderMath.uf(instance.a);
			int light = instance.getPackedLight();

			for (int i = 0; i < vertexList.vertexCount(); i++) {
				VertexTransformations.transformPos(vertexList, i, modelMatrix);
				VertexTransformations.transformNormal(vertexList, i, normalMatrix);

				vertexList.r(i, r);
				vertexList.g(i, g);
				vertexList.b(i, b);
				vertexList.a(i, a);
				vertexList.light(i, light);
			}
		};
	}

	@Override
	public InstanceBoundingSphereTransformer<OrientedInstance> getBoundingSphereTransformer() {
		return (boundingSphere, instance) -> {
			boundingSphere.sub(instance.pivotX, instance.pivotY, instance.pivotZ, 0);
			boundingSphere.rotate(instance.rotation);
			boundingSphere.add(instance.posX + instance.pivotX, instance.posY + instance.pivotY, instance.posZ + instance.pivotZ, 0);
		};
	}
}
