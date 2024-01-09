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
					.vector("light", IntegerRepr.SHORT, 2)
					.vector("color", FloatRepr.NORMALIZED_UNSIGNED_BYTE, 4)
					.matrix("pose", FloatRepr.FLOAT, 4)
					.matrix("normal", FloatRepr.FLOAT, 3)
					.build())
			.writer((ptr, instance) -> {
				MemoryUtil.memPutShort(ptr, instance.blockLight);
				MemoryUtil.memPutShort(ptr + 2, instance.skyLight);
				MemoryUtil.memPutByte(ptr + 4, instance.r);
				MemoryUtil.memPutByte(ptr + 5, instance.g);
				MemoryUtil.memPutByte(ptr + 6, instance.b);
				MemoryUtil.memPutByte(ptr + 7, instance.a);
				MatrixMath.writeUnsafe(instance.model, ptr + 8);
				MatrixMath.writeUnsafe(instance.normal, ptr + 72);
			})
			.vertexShader(Flywheel.rl("instance/transformed.vert"))
			.cullShader(Flywheel.rl("instance/cull/transformed.glsl"))
			.register();

	public static final InstanceType<OrientedInstance> ORIENTED = SimpleInstanceType.builder(OrientedInstance::new)
			.layout(LayoutBuilder.create()
					.vector("light", IntegerRepr.SHORT, 2)
					.vector("color", FloatRepr.NORMALIZED_UNSIGNED_BYTE, 4)
					.vector("position", FloatRepr.FLOAT, 3)
					.vector("pivot", FloatRepr.FLOAT, 3)
					.vector("rotation", FloatRepr.FLOAT, 4)
					.build())
			.writer((ptr, instance) -> {
				MemoryUtil.memPutShort(ptr, instance.blockLight);
				MemoryUtil.memPutShort(ptr + 2, instance.skyLight);
				MemoryUtil.memPutByte(ptr + 4, instance.r);
				MemoryUtil.memPutByte(ptr + 5, instance.g);
				MemoryUtil.memPutByte(ptr + 6, instance.b);
				MemoryUtil.memPutByte(ptr + 7, instance.a);
				MemoryUtil.memPutFloat(ptr + 8, instance.posX);
				MemoryUtil.memPutFloat(ptr + 12, instance.posY);
				MemoryUtil.memPutFloat(ptr + 16, instance.posZ);
				MemoryUtil.memPutFloat(ptr + 20, instance.pivotX);
				MemoryUtil.memPutFloat(ptr + 24, instance.pivotY);
				MemoryUtil.memPutFloat(ptr + 28, instance.pivotZ);
				MemoryUtil.memPutFloat(ptr + 32, instance.rotation.x);
				MemoryUtil.memPutFloat(ptr + 36, instance.rotation.y);
				MemoryUtil.memPutFloat(ptr + 40, instance.rotation.z);
				MemoryUtil.memPutFloat(ptr + 44, instance.rotation.w);
			})
			.vertexShader(Flywheel.rl("instance/oriented.vert"))
			.cullShader(Flywheel.rl("instance/cull/oriented.glsl"))
			.register();

	private InstanceTypes() {
	}

	@ApiStatus.Internal
	public static void init() {
	}
}
