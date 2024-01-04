package com.jozufozu.flywheel.lib.instance;

import org.jetbrains.annotations.ApiStatus;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.layout.FloatRepr;
import com.jozufozu.flywheel.api.layout.IntegerRepr;
import com.jozufozu.flywheel.api.layout.LayoutBuilder;
import com.jozufozu.flywheel.lib.layout.BufferLayout;
import com.jozufozu.flywheel.lib.layout.CommonItems;

public final class InstanceTypes {
	public static final InstanceType<TransformedInstance> TRANSFORMED = SimpleInstanceType.builder(TransformedInstance::new)
			.bufferLayout(BufferLayout.builder()
					.addItem(CommonItems.LIGHT_COORD, "light")
					.addItem(CommonItems.UNORM_4x8, "color")
					.addItem(CommonItems.MAT4, "pose")
					.addItem(CommonItems.MAT3, "normal")
					.build())
			.layout(LayoutBuilder.create()
					.vector("light", IntegerRepr.SHORT, 2)
					.vector("color", FloatRepr.NORMALIZED_UNSIGNED_BYTE, 4)
					.matrix("pose", FloatRepr.FLOAT, 4)
					.matrix("normal", FloatRepr.FLOAT, 3)
					.build())
			.writer(TransformedWriter.INSTANCE)
			.vertexShader(Flywheel.rl("instance/transformed.vert"))
			.cullShader(Flywheel.rl("instance/cull/transformed.glsl"))
			.register();

	public static final InstanceType<OrientedInstance> ORIENTED = SimpleInstanceType.builder(OrientedInstance::new)
			.bufferLayout(BufferLayout.builder()
					.addItem(CommonItems.LIGHT_COORD, "light")
					.addItem(CommonItems.UNORM_4x8, "color")
					.addItem(CommonItems.VEC3, "position")
					.addItem(CommonItems.VEC3, "pivot")
					.addItem(CommonItems.VEC4, "rotation")
					.build())
			.layout(LayoutBuilder.create()
					.vector("light", IntegerRepr.SHORT, 2)
					.vector("color", FloatRepr.NORMALIZED_UNSIGNED_BYTE, 4)
					.vector("position", FloatRepr.FLOAT, 3)
					.vector("pivot", FloatRepr.FLOAT, 3)
					.vector("rotation", FloatRepr.FLOAT, 4)
					.build())
			.writer(OrientedWriter.INSTANCE)
			.vertexShader(Flywheel.rl("instance/oriented.vert"))
			.cullShader(Flywheel.rl("instance/cull/oriented.glsl"))
			.register();

	private InstanceTypes() {
	}

	@ApiStatus.Internal
	public static void init() {
	}
}
