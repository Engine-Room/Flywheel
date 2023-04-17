package com.jozufozu.flywheel.lib.instance;

import org.joml.Quaternionf;

import com.jozufozu.flywheel.api.instance.InstanceBoundingSphereTransformer;
import com.jozufozu.flywheel.api.instance.InstanceHandle;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.InstanceVertexTransformer;
import com.jozufozu.flywheel.api.instance.InstanceWriter;
import com.jozufozu.flywheel.api.layout.BufferLayout;
import com.jozufozu.flywheel.lib.layout.CommonItems;
import com.jozufozu.flywheel.lib.math.RenderMath;
import com.jozufozu.flywheel.lib.vertex.VertexTransformations;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;

import net.minecraft.resources.ResourceLocation;

public class OrientedType implements InstanceType<OrientedInstance> {
	public static final BufferLayout FORMAT = BufferLayout.builder()
			.addItem(CommonItems.LIGHT_COORD, "light")
			.addItem(CommonItems.UNORM_4x8, "color")
			.addItem(CommonItems.VEC3, "position")
			.addItem(CommonItems.VEC3, "pivot")
			.addItem(CommonItems.VEC4, "rotation")
			.build();

	@Override
	public OrientedInstance create(InstanceHandle handle) {
		return new OrientedInstance(this, handle);
	}

	@Override
	public BufferLayout getLayout() {
		return FORMAT;
	}

	@Override
	public InstanceWriter<OrientedInstance> getWriter() {
		return OrientedWriter.INSTANCE;
	}

	@Override
	public ResourceLocation instanceShader() {
		return InstanceTypes.Files.ORIENTED;
	}

	@Override
	public InstanceVertexTransformer<OrientedInstance> getVertexTransformer() {
		return (vertexList, instance, level) -> {
			Quaternion q = new Quaternion(instance.qX, instance.qY, instance.qZ, instance.qW);

			Matrix4f modelMatrix = new Matrix4f();
			modelMatrix.setIdentity();
			modelMatrix.multiplyWithTranslation(instance.posX + instance.pivotX, instance.posY + instance.pivotY, instance.posZ + instance.pivotZ);
			modelMatrix.multiply(q);
			modelMatrix.multiplyWithTranslation(-instance.pivotX, -instance.pivotY, -instance.pivotZ);

			Matrix3f normalMatrix = new Matrix3f(q);

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
			boundingSphere.rotate(new Quaternionf(instance.qX, instance.qY, instance.qZ, instance.qW));
			boundingSphere.add(instance.posX + instance.pivotX, instance.posY + instance.pivotY, instance.posZ + instance.pivotZ, 0);
		};
	}
}
