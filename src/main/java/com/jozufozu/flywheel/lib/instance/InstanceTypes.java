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
					.vector("light", IntegerRepr.SHORT, 2)
					.vector("overlay", IntegerRepr.SHORT, 2)
					.matrix("pose", FloatRepr.FLOAT, 4)
					.matrix("normal", FloatRepr.FLOAT, 3)
					.build())
			.writer((ptr, instance) -> {
				MemoryUtil.memPutByte(ptr, instance.r);
				MemoryUtil.memPutByte(ptr + 1, instance.g);
				MemoryUtil.memPutByte(ptr + 2, instance.b);
				MemoryUtil.memPutByte(ptr + 3, instance.a);
				MemoryUtil.memPutShort(ptr + 4, instance.blockLight);
				MemoryUtil.memPutShort(ptr + 6, instance.skyLight);
				MemoryUtil.memPutInt(ptr + 8, instance.overlay);
				MatrixMath.writeUnsafe(instance.model, ptr + 12);
				MatrixMath.writeUnsafe(instance.normal, ptr + 76);
			})
			.vertexShader(Flywheel.rl("instance/transformed.vert"))
			.cullShader(Flywheel.rl("instance/cull/transformed.glsl"))
			.register();

	public static final InstanceType<OrientedInstance> ORIENTED = SimpleInstanceType.builder(OrientedInstance::new)
			.layout(LayoutBuilder.create()
					.vector("color", FloatRepr.NORMALIZED_UNSIGNED_BYTE, 4)
					.vector("light", IntegerRepr.SHORT, 2)
					.vector("overlay", IntegerRepr.SHORT, 2)
					.vector("position", FloatRepr.FLOAT, 3)
					.vector("pivot", FloatRepr.FLOAT, 3)
					.vector("rotation", FloatRepr.FLOAT, 4)
					.build())
			.writer((ptr, instance) -> {
				MemoryUtil.memPutByte(ptr, instance.r);
				MemoryUtil.memPutByte(ptr + 1, instance.g);
				MemoryUtil.memPutByte(ptr + 2, instance.b);
				MemoryUtil.memPutByte(ptr + 3, instance.a);
				MemoryUtil.memPutShort(ptr + 4, instance.blockLight);
				MemoryUtil.memPutShort(ptr + 6, instance.skyLight);
				MemoryUtil.memPutInt(ptr + 8, instance.overlay);
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

	private InstanceTypes() {
	}

	@ApiStatus.Internal
	public static void init() {
	}
}
