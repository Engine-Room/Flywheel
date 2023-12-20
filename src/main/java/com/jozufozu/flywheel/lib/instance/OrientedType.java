package com.jozufozu.flywheel.lib.instance;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.instance.InstanceHandle;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.InstanceWriter;
import com.jozufozu.flywheel.api.layout.FloatType;
import com.jozufozu.flywheel.api.layout.IntegerType;
import com.jozufozu.flywheel.api.layout.Layout;
import com.jozufozu.flywheel.api.layout.VectorSize;
import com.jozufozu.flywheel.lib.layout.BufferLayout;
import com.jozufozu.flywheel.lib.layout.CommonItems;
import com.jozufozu.flywheel.lib.layout.LayoutBuilder;

import net.minecraft.resources.ResourceLocation;

public class OrientedType implements InstanceType<OrientedInstance> {
	private static final BufferLayout OLD_LAYOUT = BufferLayout.builder()
			.addItem(CommonItems.LIGHT_COORD, "light")
			.addItem(CommonItems.UNORM_4x8, "color")
			.addItem(CommonItems.VEC3, "position")
			.addItem(CommonItems.VEC3, "pivot")
			.addItem(CommonItems.VEC4, "rotation")
			.build();

	private static final Layout LAYOUT = LayoutBuilder.of()
			.integer("light", IntegerType.SHORT, VectorSize.TWO)
			.normalized("color", IntegerType.BYTE, VectorSize.FOUR)
			.vector("position", FloatType.FLOAT, VectorSize.THREE)
			.vector("pivot", FloatType.FLOAT, VectorSize.THREE)
			.vector("rotation", FloatType.FLOAT, VectorSize.FOUR)
			.build();

	private static final ResourceLocation VERTEX_SHADER = Flywheel.rl("instance/oriented.vert");
	private static final ResourceLocation CULL_SHADER = Flywheel.rl("instance/cull/oriented.glsl");

	@Override
	public OrientedInstance create(InstanceHandle handle) {
		return new OrientedInstance(this, handle);
	}

	@Override
	public BufferLayout getLayout() {
		return OLD_LAYOUT;
	}

	@Override
	public Layout layout() {
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

}
