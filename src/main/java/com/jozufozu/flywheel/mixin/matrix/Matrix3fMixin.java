package com.jozufozu.flywheel.mixin.matrix;

import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.util.MatrixWrite;
import com.mojang.math.Matrix3f;

@Mixin(Matrix3f.class)
public abstract class Matrix3fMixin implements MatrixWrite {

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
	public void flywheel$writeUnsafe(long ptr) {
		MemoryUtil.memPutFloat(ptr, m00);
		MemoryUtil.memPutFloat(ptr + 4, m10);
		MemoryUtil.memPutFloat(ptr + 8, m20);
		MemoryUtil.memPutFloat(ptr + 12, m01);
		MemoryUtil.memPutFloat(ptr + 16, m11);
		MemoryUtil.memPutFloat(ptr + 20, m21);
		MemoryUtil.memPutFloat(ptr + 24, m02);
		MemoryUtil.memPutFloat(ptr + 28, m12);
		MemoryUtil.memPutFloat(ptr + 32, m22);
	}

	@Override
	public void flywheel$write(VecBuffer buffer) {
		buffer.putFloat(m00);
		buffer.putFloat(m10);
		buffer.putFloat(m20);
		buffer.putFloat(m01);
		buffer.putFloat(m11);
		buffer.putFloat(m21);
		buffer.putFloat(m02);
		buffer.putFloat(m12);
		buffer.putFloat(m22);
	}
}
