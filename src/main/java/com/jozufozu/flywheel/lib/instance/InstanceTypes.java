package com.jozufozu.flywheel.lib.instance;

import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.layout.FloatRepr;
import com.jozufozu.flywheel.api.layout.IntegerRepr;
import com.jozufozu.flywheel.api.layout.LayoutBuilder;
import com.jozufozu.flywheel.lib.math.MatrixMath;

public final class InstanceTypes {
	public static final InstanceType<TransformedInstance> TRANSFORMED = SimpleInstanceType.builder(TransformedInstance::new)
			.layout(LayoutBuilder.create()
					.vector("color", FloatRepr.NORMALIZED_UNSIGNED_BYTE, 4)
					.vector("overlay", IntegerRepr.SHORT, 2)
					.vector("light", FloatRepr.UNSIGNED_SHORT, 2)
					.matrix("pose", FloatRepr.FLOAT, 4)
					.matrix("normal", FloatRepr.FLOAT, 3)
					.build())
			.writer((ptr, instance) -> {
				MemoryUtil.memPutByte(ptr, instance.r);
				MemoryUtil.memPutByte(ptr + 1, instance.g);
				MemoryUtil.memPutByte(ptr + 2, instance.b);
				MemoryUtil.memPutByte(ptr + 3, instance.a);
				MemoryUtil.memPutInt(ptr + 4, instance.overlay);
				MemoryUtil.memPutInt(ptr + 8, (int) instance.blockLight | (int) instance.skyLight << 16);
				MatrixMath.writeUnsafe(instance.model, ptr + 12);
				MatrixMath.writeUnsafe(instance.normal, ptr + 76);
			})
			.vertexShader(Flywheel.rl("instance/transformed.vert"))
			.cullShader(Flywheel.rl("instance/cull/transformed.glsl"))
			.register();

	public static final InstanceType<OrientedInstance> ORIENTED = SimpleInstanceType.builder(OrientedInstance::new)
			.layout(LayoutBuilder.create()
					.vector("color", FloatRepr.NORMALIZED_UNSIGNED_BYTE, 4)
					.vector("overlay", IntegerRepr.SHORT, 2)
					.vector("light", FloatRepr.UNSIGNED_SHORT, 2)
					.vector("position", FloatRepr.FLOAT, 3)
					.vector("pivot", FloatRepr.FLOAT, 3)
					.vector("rotation", FloatRepr.FLOAT, 4)
					.build())
			.writer((ptr, instance) -> {
				MemoryUtil.memPutByte(ptr, instance.r);
				MemoryUtil.memPutByte(ptr + 1, instance.g);
				MemoryUtil.memPutByte(ptr + 2, instance.b);
				MemoryUtil.memPutByte(ptr + 3, instance.a);
				MemoryUtil.memPutInt(ptr + 4, instance.overlay);
				MemoryUtil.memPutInt(ptr + 8, (int) instance.blockLight | (int) instance.skyLight << 16);
				MemoryUtil.memPutFloat(ptr + 12, instance.posX);
				MemoryUtil.memPutFloat(ptr + 16, instance.posY);
				MemoryUtil.memPutFloat(ptr + 20, instance.posZ);
				MemoryUtil.memPutFloat(ptr + 24, instance.pivotX);
				MemoryUtil.memPutFloat(ptr + 28, instance.pivotY);
				MemoryUtil.memPutFloat(ptr + 32, instance.pivotZ);
				MemoryUtil.memPutFloat(ptr + 36, instance.rotation.x);
				MemoryUtil.memPutFloat(ptr + 40, instance.rotation.y);
				MemoryUtil.memPutFloat(ptr + 44, instance.rotation.z);
				MemoryUtil.memPutFloat(ptr + 48, instance.rotation.w);
			})
			.vertexShader(Flywheel.rl("instance/oriented.vert"))
			.cullShader(Flywheel.rl("instance/cull/oriented.glsl"))
			.register();

	public static final InstanceType<ShadowInstance> SHADOW = SimpleInstanceType.builder(ShadowInstance::new)
			.layout(LayoutBuilder.create()
					.vector("pos", FloatRepr.FLOAT, 3)
					.vector("entityPosXZ", FloatRepr.FLOAT, 2)
					.vector("size", FloatRepr.FLOAT, 2)
					.scalar("alpha", FloatRepr.FLOAT)
					.scalar("radius", FloatRepr.FLOAT)
					.build())
			.writer((ptr, instance) -> {
				MemoryUtil.memPutFloat(ptr, instance.x);
				MemoryUtil.memPutFloat(ptr + 4, instance.y);
				MemoryUtil.memPutFloat(ptr + 8, instance.z);
				MemoryUtil.memPutFloat(ptr + 12, instance.entityX);
				MemoryUtil.memPutFloat(ptr + 16, instance.entityZ);
				MemoryUtil.memPutFloat(ptr + 20, instance.sizeX);
				MemoryUtil.memPutFloat(ptr + 24, instance.sizeZ);
				MemoryUtil.memPutFloat(ptr + 28, instance.alpha);
				MemoryUtil.memPutFloat(ptr + 32, instance.radius);
			})
			.vertexShader(Flywheel.rl("instance/shadow.vert"))
			.cullShader(Flywheel.rl("instance/cull/shadow.glsl"))
			.register();

	private InstanceTypes() {
	}

	@ApiStatus.Internal
	public static void init() {
	}
}
