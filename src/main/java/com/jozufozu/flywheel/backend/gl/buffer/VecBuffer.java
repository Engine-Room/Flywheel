package com.jozufozu.flywheel.backend.gl.buffer;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class VecBuffer {

	protected ByteBuffer internal;

	public VecBuffer() {
	}

	public VecBuffer(ByteBuffer internal) {
		this.internal = internal;
	}

	public static VecBuffer allocate(int bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(bytes);
		buffer.order(ByteOrder.nativeOrder());
		return new VecBuffer(buffer);
	}

	public ByteBuffer unwrap() {
		return internal;
	}

	public VecBuffer rewind() {
		((Buffer) internal).rewind();

		return this;
	}

	public VecBuffer putFloatArray(float[] floats) {

		internal.asFloatBuffer().put(floats);
		internal.position(internal.position() + floats.length * 4);

		return this;
	}

	public VecBuffer putByteArray(byte[] bytes) {
		internal.put(bytes);
		return this;
	}

	public VecBuffer put(FloatBuffer floats) {

		int remainingBytes = floats.remaining() * 4;
		internal.asFloatBuffer().put(floats);
		internal.position(internal.position() + remainingBytes);

		return this;
	}

	public int position() {
		return internal.position();
	}

	/**
	 * Position this buffer relative to the 0-index in GPU memory.
	 *
	 * @return This buffer.
	 */
	public VecBuffer position(int p) {
		internal.position(p);
		return this;
	}

	public VecBuffer putFloat(float f) {
		internal.putFloat(f);
		return this;
	}

	public VecBuffer putInt(int i) {
		internal.putInt(i);
		return this;
	}

	public VecBuffer putShort(short s) {
		internal.putShort(s);
		return this;
	}

	public VecBuffer put(byte b) {
		internal.put(b);
		return this;
	}

	public VecBuffer put(ByteBuffer b) {
		internal.put(b);
		return this;
	}

	public VecBuffer putVec4(float x, float y, float z, float w) {
		internal.putFloat(x);
		internal.putFloat(y);
		internal.putFloat(z);
		internal.putFloat(w);
		return this;
	}

	public VecBuffer putColor(int r, int g, int b, int a) {
		internal.put((byte) r);
		internal.put((byte) g);
		internal.put((byte) b);
		internal.put((byte) a);
		return this;
	}

	public VecBuffer putColor(byte r, byte g, byte b, byte a) {
		internal.put(r);
		internal.put(g);
		internal.put(b);
		internal.put(a);
		return this;
	}

	public VecBuffer putVec3(float x, float y, float z) {
		internal.putFloat(x);
		internal.putFloat(y);
		internal.putFloat(z);
		return this;
	}

	public VecBuffer putVec2(float x, float y) {
		internal.putFloat(x);
		internal.putFloat(y);
		return this;
	}

	public VecBuffer putVec3(byte x, byte y, byte z) {
		internal.put(x);
		internal.put(y);
		internal.put(z);
		return this;
	}

	public VecBuffer putVec2(byte x, byte y) {
		internal.put(x);
		internal.put(y);
		return this;
	}
}
