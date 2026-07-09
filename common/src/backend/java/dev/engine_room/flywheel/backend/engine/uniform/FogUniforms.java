package dev.engine_room.flywheel.backend.engine.uniform;

public final class FogUniforms extends UniformWriter {
	private static final int SIZE = 4 * 7;
	static final UniformBuffer BUFFER = new UniformBuffer(Uniforms.FOG_INDEX, SIZE);

	public static void update() {
		long ptr = BUFFER.ptr();

		ptr = writeFloat(ptr, 0);
		ptr = writeFloat(ptr, 0);
		ptr = writeFloat(ptr, 0);
		ptr = writeFloat(ptr, 0);
		ptr = writeFloat(ptr, 0);
		ptr = writeFloat(ptr, Float.POSITIVE_INFINITY);
		ptr = writeInt(ptr, 0);

		BUFFER.markDirty();
	}
}
