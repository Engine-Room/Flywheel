package dev.engine_room.flywheel.backend.engine.uniform;

import org.joml.Vector4f;

public final class FogUniforms extends UniformWriter {
	private static final int SIZE = 4 * 8;
	static final UniformBuffer BUFFER = new UniformBuffer(Uniforms.FOG_INDEX, SIZE);

	public static void update(Vector4f color, float environmentalStart, float environmentalEnd,
							  float renderDistanceStart, float renderDistanceEnd) {
		long ptr = BUFFER.ptr();

		ptr = writeVec4(ptr, color);
		ptr = writeFloat(ptr, environmentalStart);
		ptr = writeFloat(ptr, environmentalEnd);
		ptr = writeFloat(ptr, renderDistanceStart);
		ptr = writeFloat(ptr, renderDistanceEnd);

		BUFFER.markDirty();
	}
}
