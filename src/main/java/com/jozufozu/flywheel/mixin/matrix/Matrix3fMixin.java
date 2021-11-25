package com.jozufozu.flywheel.mixin.matrix;

import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.util.WriteSafe;
import com.jozufozu.flywheel.util.WriteUnsafe;
import com.mojang.math.Matrix3f;

@Mixin(Matrix3f.class)
public abstract class Matrix3fMixin implements WriteUnsafe, WriteSafe {

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
	public void writeUnsafe(long addr) {
		MemoryUtil.memPutFloat(addr, m00);
		MemoryUtil.memPutFloat(addr += 4L, m10);
		MemoryUtil.memPutFloat(addr += 4L, m20);
		MemoryUtil.memPutFloat(addr += 4L, m01);
		MemoryUtil.memPutFloat(addr += 4L, m11);
		MemoryUtil.memPutFloat(addr += 4L, m21);
		MemoryUtil.memPutFloat(addr += 4L, m02);
		MemoryUtil.memPutFloat(addr += 4L, m12);
		MemoryUtil.memPutFloat(addr += 4L, m22);
	}

	@Override
	public void write(VecBuffer buffer) {
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
