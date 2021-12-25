package com.jozufozu.flywheel.mixin.matrix;

import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.util.MatrixWrite;
import com.mojang.math.Matrix4f;

@Mixin(Matrix4f.class)
public abstract class Matrix4fMixin implements MatrixWrite {

	@Shadow protected float m00;
	@Shadow protected float m01;
	@Shadow protected float m02;
	@Shadow protected float m03;
	@Shadow protected float m10;
	@Shadow protected float m11;
	@Shadow protected float m12;
	@Shadow protected float m13;
	@Shadow protected float m20;
	@Shadow protected float m21;
	@Shadow protected float m22;
	@Shadow protected float m23;
	@Shadow protected float m30;
	@Shadow protected float m31;
	@Shadow protected float m32;
	@Shadow protected float m33;

	@Override
	public void flywheel$writeUnsafe(long ptr) {
		MemoryUtil.memPutFloat(ptr, m00);
		MemoryUtil.memPutFloat(ptr + 4, m10);
		MemoryUtil.memPutFloat(ptr + 8, m20);
		MemoryUtil.memPutFloat(ptr + 12, m30);
		MemoryUtil.memPutFloat(ptr + 16, m01);
		MemoryUtil.memPutFloat(ptr + 20, m11);
		MemoryUtil.memPutFloat(ptr + 24, m21);
		MemoryUtil.memPutFloat(ptr + 28, m31);
		MemoryUtil.memPutFloat(ptr + 32, m02);
		MemoryUtil.memPutFloat(ptr + 36, m12);
		MemoryUtil.memPutFloat(ptr + 40, m22);
		MemoryUtil.memPutFloat(ptr + 44, m32);
		MemoryUtil.memPutFloat(ptr + 48, m03);
		MemoryUtil.memPutFloat(ptr + 52, m13);
		MemoryUtil.memPutFloat(ptr + 56, m23);
		MemoryUtil.memPutFloat(ptr + 60, m33);
	}

	@Override
	public void flywheel$write(VecBuffer buf) {
		buf.putFloat(m00);
		buf.putFloat(m10);
		buf.putFloat(m20);
		buf.putFloat(m30);
		buf.putFloat(m01);
		buf.putFloat(m11);
		buf.putFloat(m21);
		buf.putFloat(m31);
		buf.putFloat(m02);
		buf.putFloat(m12);
		buf.putFloat(m22);
		buf.putFloat(m32);
		buf.putFloat(m03);
		buf.putFloat(m13);
		buf.putFloat(m23);
		buf.putFloat(m33);
	}
}
