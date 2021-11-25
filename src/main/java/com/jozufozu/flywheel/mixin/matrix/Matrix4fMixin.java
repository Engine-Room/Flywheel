package com.jozufozu.flywheel.mixin.matrix;

import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.util.WriteSafe;
import com.jozufozu.flywheel.util.WriteUnsafe;
import com.mojang.math.Matrix4f;

@Mixin(Matrix4f.class)
public abstract class Matrix4fMixin implements WriteUnsafe, WriteSafe {

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
	public void writeUnsafe(long addr) {
		MemoryUtil.memPutFloat(addr, m00);
		MemoryUtil.memPutFloat(addr += 4L, m10);
		MemoryUtil.memPutFloat(addr += 4L, m20);
		MemoryUtil.memPutFloat(addr += 4L, m30);
		MemoryUtil.memPutFloat(addr += 4L, m01);
		MemoryUtil.memPutFloat(addr += 4L, m11);
		MemoryUtil.memPutFloat(addr += 4L, m21);
		MemoryUtil.memPutFloat(addr += 4L, m31);
		MemoryUtil.memPutFloat(addr += 4L, m02);
		MemoryUtil.memPutFloat(addr += 4L, m12);
		MemoryUtil.memPutFloat(addr += 4L, m22);
		MemoryUtil.memPutFloat(addr += 4L, m32);
		MemoryUtil.memPutFloat(addr += 4L, m03);
		MemoryUtil.memPutFloat(addr += 4L, m13);
		MemoryUtil.memPutFloat(addr += 4L, m23);
		MemoryUtil.memPutFloat(addr += 4L, m33);
	}

	@Override
	public void write(VecBuffer buf) {
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
