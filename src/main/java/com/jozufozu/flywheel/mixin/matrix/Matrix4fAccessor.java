package com.jozufozu.flywheel.mixin.matrix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.math.Matrix4f;

@Mixin(Matrix4f.class)
public interface Matrix4fAccessor {
	@Accessor("m00")
	float flywheel$m00();

	@Accessor("m01")
	float flywheel$m01();

	@Accessor("m02")
	float flywheel$m02();

	@Accessor("m03")
	float flywheel$m03();

	@Accessor("m10")
	float flywheel$m10();

	@Accessor("m11")
	float flywheel$m11();

	@Accessor("m12")
	float flywheel$m12();

	@Accessor("m13")
	float flywheel$m13();

	@Accessor("m20")
	float flywheel$m20();

	@Accessor("m21")
	float flywheel$m21();

	@Accessor("m22")
	float flywheel$m22();

	@Accessor("m23")
	float flywheel$m23();

	@Accessor("m30")
	float flywheel$m30();

	@Accessor("m31")
	float flywheel$m31();

	@Accessor("m32")
	float flywheel$m32();

	@Accessor("m33")
	float flywheel$m33();
}
