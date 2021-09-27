package com.jozufozu.flywheel.mixin.matrix;

import java.nio.FloatBuffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.jozufozu.flywheel.util.Attribute;
import com.mojang.math.Matrix3f;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(Matrix3f.class)
public abstract class Matrix3fMixin implements Attribute {

	@Shadow protected float m00;
	@Shadow protected float m01;
	@Shadow protected float m02;
	@Shadow protected float m10;
	@Shadow protected float m11;
	@Shadow protected float m12;
	@Shadow protected float m20;
	@Shadow protected float m21;
	@Shadow protected float m22;

	@Override
	public void append(FloatBuffer buffer) {
		buffer.put(m00);
		buffer.put(m10);
		buffer.put(m20);
		buffer.put(m01);
		buffer.put(m11);
		buffer.put(m21);
		buffer.put(m02);
		buffer.put(m12);
		buffer.put(m22);
	}
}
