package com.jozufozu.flywheel.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.jozufozu.flywheel.fabric.extension.Matrix4fExtension;
import com.mojang.math.Matrix4f;

@Mixin(Matrix4f.class)
public abstract class Matrix4fMixin implements Matrix4fExtension {
	@Shadow protected float m00;
	@Shadow protected float m03;
	@Shadow protected float m11;
	@Shadow protected float m13;
	@Shadow protected float m22;
	@Shadow protected float m23;
	@Shadow protected float m33;

	@Override
	public void setTranslation(float x, float y, float z) {
		m00 = 1.0f;
		m11 = 1.0f;
		m22 = 1.0f;
		m33 = 1.0f;
		m03 = x;
		m13 = y;
		m23 = z;
	}
}
