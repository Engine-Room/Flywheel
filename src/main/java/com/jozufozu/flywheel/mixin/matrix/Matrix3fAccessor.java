package com.jozufozu.flywheel.mixin.matrix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.math.Matrix3f;

@Mixin(Matrix3f.class)
public interface Matrix3fAccessor {
	@Accessor("m00")
	float flywheel$m00();

	@Accessor("m01")
	float flywheel$m01();

	@Accessor("m02")
	float flywheel$m02();

	@Accessor("m10")
	float flywheel$m10();

	@Accessor("m11")
	float flywheel$m11();

	@Accessor("m12")
	float flywheel$m12();

	@Accessor("m20")
	float flywheel$m20();

	@Accessor("m21")
	float flywheel$m21();

	@Accessor("m22")
	float flywheel$m22();
}
