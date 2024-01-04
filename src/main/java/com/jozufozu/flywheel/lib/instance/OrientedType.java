package com.jozufozu.flywheel.lib.instance;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.instance.InstanceHandle;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.InstanceWriter;
import com.jozufozu.flywheel.api.layout.FloatRepr;
import com.jozufozu.flywheel.api.layout.IntegerRepr;
import com.jozufozu.flywheel.api.layout.Layout;
import com.jozufozu.flywheel.api.layout.LayoutBuilder;
import com.jozufozu.flywheel.lib.layout.BufferLayout;
import com.jozufozu.flywheel.lib.layout.CommonItems;

import net.minecraft.resources.ResourceLocation;

public class OrientedType implements InstanceType<OrientedInstance> {
	@Deprecated
	private static final BufferLayout OLD_LAYOUT = BufferLayout.builder()
			.addItem(CommonItems.LIGHT_COORD, "light")
			.addItem(CommonItems.UNORM_4x8, "color")
			.addItem(CommonItems.VEC3, "position")
			.addItem(CommonItems.VEC3, "pivot")
			.addItem(CommonItems.VEC4, "rotation")
			.build();

	private static final Layout LAYOUT = LayoutBuilder.create()
			.vector("light", IntegerRepr.SHORT, 2)
			.vector("color", FloatRepr.NORMALIZED_UNSIGNED_BYTE, 4)
			.vector("position", FloatRepr.FLOAT, 3)
			.vector("pivot", FloatRepr.FLOAT, 3)
			.vector("rotation", FloatRepr.FLOAT, 4)
			.build();

	private static final ResourceLocation VERTEX_SHADER = Flywheel.rl("instance/oriented.vert");
	private static final ResourceLocation CULL_SHADER = Flywheel.rl("instance/cull/oriented.glsl");

	@Override
	public OrientedInstance create(InstanceHandle handle) {
		return new OrientedInstance(this, handle);
	}

	@Override
	@Deprecated
	public BufferLayout oldLayout() {
		return OLD_LAYOUT;
	}

	@Override
	public Layout layout() {
		return LAYOUT;
	}

	@Override
	public InstanceWriter<OrientedInstance> writer() {
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
}
